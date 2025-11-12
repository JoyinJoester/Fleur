# Implementation Plan

- [x] 1. Wire up the mark as unread callback in InboxScreen





  - Add the `onMarkAsUnread` parameter to the `MultiSelectToolbar` call in `InboxScreen.kt`
  - Connect it to `viewModel.markSelectedAsRead(false)` to mark selected emails as unread
  - Ensure the callback is placed alongside the existing `onMarkAsRead` callback for consistency
  - _Requirements: 1.1, 1.2_

- [ ]* 2. Verify the fix with manual testing
  - Test marking multiple emails as unread in multi-select mode
  - Verify that the UI updates correctly and multi-select mode exits
  - Test edge cases including already unread emails and mixed read/unread selections
  - _Requirements: 1.3, 1.4, 2.1, 2.2, 2.3_
