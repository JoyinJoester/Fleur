# 联系人数据源说明

## 当前实现

目前联系人功能使用 `GetContactsUseCase`，它从 **邮件中提取联系人**:

```kotlin
// GetContactsUseCase.kt
class GetContactsUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    operator fun invoke(accountId: String? = null): Flow<Result<List<ContactUiModel>>> {
        return emailRepository.getEmails(accountId = accountId, page = 0, pageSize = 500)
            .map { result ->
                result.map { emails ->
                    ContactMapper.extractContactsFromEmails(emails)
                }
            }
    }
}
```

## 显示内容

- **"往来过的邮箱"**: 从邮件中提取的邮箱地址(未保存为联系人)
- **"往来过的联系人"**: 从邮件的发件人/收件人中提取的所有联系人

## 需要的改进

### 方案 1: 创建真正的联系人存储

创建独立的 `ContactRepository` 和数据库表:

```kotlin
@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String? = null,
    val avatarUrl: String? = null,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long
)

interface ContactRepository {
    fun getContacts(): Flow<Result<List<Contact>>>
    suspend fun addContact(contact: Contact): Result<Unit>
    suspend fun updateContact(contact: Contact): Result<Unit>
    suspend fun deleteContact(contactId: String): Result<Unit>
}
```

### 方案 2: 区分两种联系人

- **已保存联系人**: 用户手动添加的联系人(存储在数据库)
- **往来联系人**: 从邮件中提取的临时联系人(不存储,每次从邮件读取)

## UI 调整

联系人页面应该显示:

1. **往来过的邮箱** (已实现) - 从邮件提取,排除已保存联系人的邮箱
2. **已保存联系人** (待实现) - 从 ContactRepository 读取
3. ~~往来过的联系人~~ (当前错误地显示为"已保存联系人")

## 添加联系人功能

需要实现:

1. 添加联系人表单页面
2. ContactRepository 和数据库
3. 联系人 CRUD 操作
4. 从"往来邮箱"快速添加联系人功能
