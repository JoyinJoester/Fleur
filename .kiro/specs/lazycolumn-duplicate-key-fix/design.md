# Design Document

## Overview

This design addresses the critical crash caused by duplicate keys in LazyColumn when scrolling the email list. The solution involves multiple layers of defense:

1. **Data Layer**: Ensure database constraints and proper conflict resolution
2. **Repository Layer**: Deduplicate data before returning to ViewModels
3. **ViewModel Layer**: Validate and deduplicate data before updating UI state
4. **UI Layer**: Add defensive deduplication as a final safeguard

## Architecture

### Root Cause Analysis

The crash occurs because:
1. Test data generation may create emails with duplicate IDs
2. Multiple insertions of test data without clearing previous data
3. LazyColumn's `items()` function uses `email.id` as the key, which must be unique
4. When duplicate IDs exist, Compose throws `IllegalArgumentException`

### Solution Strategy

**Multi-Layer Defense Approach:**
- Layer 1 (Database): Enforce uniqueness at the schema level
- Layer 2 (DAO): Use REPLACE strategy for conflict resolution
- Layer 3 (Repository): Deduplicate before returning data
- Layer 4 (ViewModel): Validate data integrity
- Layer 5 (UI): Final safety check before rendering

## Components and Interfaces

### 1. Database Schema Enhancement

**EmailEntity Primary Key:**
```kotlin
@Entity(
    tableName = "emails",
    indices = [
        Index(value = ["id"], unique = true),  // Enforce uniqueness
        Index(value = ["accountId"]),
        Index(value = ["threadId"])
    ]
)
```

### 2. EmailDao Modifications

**Conflict Strategy:**
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertEmails(emails: List<EmailEntity>)

// Add diagnostic query
@Query("SELECT id, COUNT(*) as count FROM emails GROUP BY id HAVING count > 1")
suspend fun findDuplicateIds(): List<DuplicateIdInfo>
```

### 3. EmailRepository Deduplication

**Method:**
```kotlin
fun getEmails(accountId: String?, page: Int, pageSize: Int): Flow<Result<List<Email>>> {
    return flow {
        val entities = emailDao.getEmailsByAccount(accountId, page * pageSize, pageSize)
        
        // Deduplicate by ID
        val uniqueEmails = entities.distinctBy { it.id }
        
        // Log warning if duplicates found
        if (uniqueEmails.size < entities.size) {
            Log.w(TAG, "Found ${entities.size - uniqueEmails.size} duplicate emails")
        }
        
        emit(Result.success(uniqueEmails.map { it.toDomain() }))
    }
}
```

### 4. InboxViewModel Validation

**State Update Logic:**
```kotlin
private fun updateEmailList(newEmails: List<Email>) {
    // Deduplicate before updating state
    val uniqueEmails = newEmails.distinctBy { it.id }
    
    if (uniqueEmails.size < newEmails.size) {
        Log.w(TAG, "Removed ${newEmails.size - uniqueEmails.size} duplicate emails in ViewModel")
    }
    
    _uiState.update { state ->
        state.copy(emails = uniqueEmails)
    }
}
```

### 5. EmailListView Safety Check

**Rendering Logic:**
```kotlin
@Composable
fun EmailListView(emails: List<Email>, ...) {
    // Final safety check
    val uniqueEmails = remember(emails) {
        val unique = emails.distinctBy { it.id }
        if (unique.size < emails.size) {
            Log.e(TAG, "UI layer found ${emails.size - unique.size} duplicate emails!")
        }
        unique
    }
    
    LazyColumn {
        items(
            items = uniqueEmails,
            key = { email -> email.id }  // Now guaranteed unique
        ) { email ->
            // Render email item
        }
    }
}
```

### 6. Test Data Generator Fix

**Unique ID Generation:**
```kotlin
object TestEmailGenerator {
    private val idCounter = AtomicInteger(0)
    
    fun generateEmail(accountId: String, id: String? = null): EmailEntity {
        val uniqueId = id ?: generateUniqueId()
        // ... rest of implementation
    }
    
    private fun generateUniqueId(): String {
        val timestamp = System.currentTimeMillis()
        val counter = idCounter.incrementAndGet()
        val random = Random.nextInt(1000, 9999)
        return "test_email_${timestamp}_${counter}_${random}"
    }
}
```

### 7. TestDataInserter Enhancement

**Clear Before Insert:**
```kotlin
class TestDataInserter(private val context: Context) {
    suspend fun insertTestEmails(accountId: String, count: Int): Int {
        // Clear existing test emails first
        emailDao.deleteTestEmails()
        
        // Generate and insert new test emails
        val emails = TestEmailGenerator.generateEmails(accountId, count)
        emailDao.insertEmails(emails)
        
        return emails.size
    }
    
    // Add method to EmailDao
    @Query("DELETE FROM emails WHERE id LIKE 'test_email_%'")
    suspend fun deleteTestEmails()
}
```

## Data Models

### DuplicateIdInfo (Diagnostic)

```kotlin
data class DuplicateIdInfo(
    val id: String,
    val count: Int
)
```

## Error Handling

### Detection and Logging

1. **Database Level**: Constraint violations logged
2. **Repository Level**: Deduplication count logged as warning
3. **ViewModel Level**: Validation failures logged as warning
4. **UI Level**: Critical errors logged as error

### Recovery Strategy

- **Automatic**: Deduplication happens transparently
- **User Impact**: None - duplicates are silently removed
- **Developer Feedback**: Logs provide diagnostic information

## Testing Strategy

### Unit Tests

1. **TestEmailGenerator Tests**:
   - Verify unique ID generation
   - Test concurrent ID generation
   - Validate ID format

2. **Repository Tests**:
   - Test deduplication logic
   - Verify logging behavior
   - Test with duplicate data

3. **ViewModel Tests**:
   - Test state updates with duplicates
   - Verify deduplication in state

### Integration Tests

1. **Database Tests**:
   - Test REPLACE conflict strategy
   - Verify unique constraint enforcement
   - Test duplicate detection query

2. **UI Tests**:
   - Test LazyColumn with duplicate data
   - Verify no crashes occur
   - Test scrolling performance

### Manual Testing

1. Insert test data multiple times
2. Scroll through email list
3. Verify no crashes
4. Check logs for duplicate warnings

## Performance Considerations

### Deduplication Cost

- `distinctBy` is O(n) operation
- Acceptable for typical email list sizes (< 1000 items)
- Happens in memory, no database overhead

### Optimization

- Deduplication at repository level prevents duplicates from reaching ViewModel
- UI layer deduplication is a safety net, should rarely trigger
- Use `remember` to avoid recomputing on every recomposition

## Migration Strategy

### Database Migration

No schema changes required if unique index already exists. If not:

```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Remove duplicates first
        database.execSQL("""
            DELETE FROM emails 
            WHERE rowid NOT IN (
                SELECT MIN(rowid) 
                FROM emails 
                GROUP BY id
            )
        """)
        
        // Add unique index
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_emails_id ON emails(id)")
    }
}
```

## Monitoring and Diagnostics

### Logging Strategy

```kotlin
object EmailListLogger {
    fun logDuplicates(layer: String, originalCount: Int, uniqueCount: Int) {
        if (originalCount > uniqueCount) {
            Log.w(TAG, "[$layer] Removed ${originalCount - uniqueCount} duplicates")
        }
    }
    
    fun logEmailList(layer: String, emails: List<Email>) {
        Log.d(TAG, "[$layer] Email list: ${emails.size} items")
        if (BuildConfig.DEBUG) {
            val ids = emails.map { it.id }
            val duplicates = ids.groupingBy { it }.eachCount().filter { it.value > 1 }
            if (duplicates.isNotEmpty()) {
                Log.e(TAG, "[$layer] Duplicate IDs found: $duplicates")
            }
        }
    }
}
```

## Security Considerations

- No security implications
- Deduplication is a data integrity measure
- No user data is exposed or modified

## Future Enhancements

1. **Proactive Duplicate Detection**: Background job to scan and report duplicates
2. **Analytics**: Track duplicate occurrence frequency
3. **User Notification**: Optional notification when duplicates are detected and removed
4. **Database Cleanup**: Periodic cleanup of orphaned or duplicate data
