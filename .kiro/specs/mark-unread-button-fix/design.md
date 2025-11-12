# Design Document

## Overview

This design addresses the bug where the "标为未读" (mark as unread) button in the multi-select toolbar is not functional. The root cause is that the `InboxScreen` component does not pass the `onMarkAsUnread` callback to the `MultiSelectToolbar` component, even though the ViewModel already has the necessary method (`markSelectedAsRead(isRead: Boolean)`) to handle this operation.

## Architecture

The fix involves a simple callback wiring in the UI layer:

```
User Click → MultiSelectToolbar.onMarkAsUnread 
           → InboxScreen.onMarkAsUnread callback 
           → InboxViewModel.markSelectedAsRead(false)
           → MarkAsReadUseCase.markMultiple(selectedIds, false)
           → UI State Update
```

## Components and Interfaces

### 1. InboxScreen (Modification Required)

**Current State:**
- The `MultiSelectToolbar` is called with `onMarkAsRead` callback but missing `onMarkAsUnread`
- Line 122 in InboxScreen.kt shows: `onMarkAsRead = { viewModel.markSelectedAsRead(true) }`
- No `onMarkAsUnread` parameter is passed

**Required Change:**
- Add `onMarkAsUnread` parameter to the `MultiSelectToolbar` call
- Wire it to `viewModel.markSelectedAsRead(false)`

```kotlin
MultiSelectToolbar(
    visible = true,
    selectedCount = uiState.selectedEmailIds.size,
    onClose = { viewModel.exitMultiSelectMode() },
    onDelete = { viewModel.deleteSelectedEmails() },
    onArchive = { viewModel.archiveSelectedEmails() },
    onMarkAsRead = { viewModel.markSelectedAsRead(true) },
    onMarkAsUnread = { viewModel.markSelectedAsRead(false) }  // ADD THIS LINE
)
```

### 2. MultiSelectToolbar (No Changes Required)

**Current State:**
- Already has `onMarkAsUnread` parameter with default empty lambda
- Already has the IconButton wired to `onMarkAsUnread`
- The UI component is correctly implemented

### 3. InboxViewModel (No Changes Required)

**Current State:**
- Already has `markSelectedAsRead(isRead: Boolean)` method
- Method handles both marking as read (isRead=true) and unread (isRead=false)
- Properly updates UI state and handles errors
- Exits multi-select mode after successful operation

## Data Models

No data model changes required. The existing `InboxUiState` already tracks:
- `selectedEmailIds: Set<String>` - The set of selected email IDs
- `isMultiSelectMode: Boolean` - Whether multi-select mode is active
- `error: FleurError?` - Error state for displaying error messages

## Error Handling

The existing error handling in `InboxViewModel.markSelectedAsRead()` is sufficient:

1. On failure, the method updates the UI state with a `FleurError`
2. The error message is "批量标记失败" (Batch mark failed)
3. The `InboxScreen` already observes `uiState.error` and displays it via snackbar
4. The error is cleared after being shown via `viewModel.clearError()`

No additional error handling is required.

## Testing Strategy

### Manual Testing
1. Open the Inbox screen
2. Long-press an email to enter multi-select mode
3. Select multiple emails (some read, some unread)
4. Tap the "标为未读" button
5. Verify that all selected emails are marked as unread
6. Verify that multi-select mode exits
7. Verify that the selection is cleared

### Edge Cases to Test
1. Mark already unread emails as unread (should be idempotent)
2. Mark a mix of read and unread emails as unread
3. Test with network offline (should queue the operation)
4. Test with a large number of selected emails (50+)

### Error Scenarios
1. Simulate a failure in the use case and verify error snackbar appears
2. Verify that on error, the multi-select mode remains active
3. Verify that the selection is preserved on error

## Implementation Notes

This is a minimal fix that requires only one line of code to be added. The existing architecture already supports this functionality - it was simply not wired up in the UI layer.

The fix maintains consistency with how `onMarkAsRead` is implemented, using the same pattern and calling the same ViewModel method with a different parameter value.
