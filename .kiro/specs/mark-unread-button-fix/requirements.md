# Requirements Document

## Introduction

This specification addresses a bug where the "标为未读" (mark as unread) button in the multi-select toolbar of the Inbox screen is not responding when clicked. The button is visible but does not trigger any action because the callback is not properly wired from the UI component to the ViewModel.

## Glossary

- **InboxScreen**: The main email inbox screen component that displays the list of emails
- **MultiSelectToolbar**: The toolbar component that appears when users select multiple emails, providing batch action buttons
- **InboxViewModel**: The ViewModel that manages the inbox state and business logic
- **Mark Unread Action**: The user action to mark selected emails as unread (changing their read status to false)

## Requirements

### Requirement 1

**User Story:** As a user, I want to mark multiple selected emails as unread using the multi-select toolbar button, so that I can quickly change the read status of multiple emails at once

#### Acceptance Criteria

1. WHEN the user clicks the "标为未读" button in the multi-select toolbar, THE InboxScreen SHALL invoke the mark as unread callback
2. WHEN the mark as unread callback is invoked, THE InboxViewModel SHALL call the markSelectedAsRead method with isRead parameter set to false
3. WHEN the markSelectedAsRead method completes successfully, THE InboxScreen SHALL update the UI to reflect the unread status of the selected emails
4. WHEN the markSelectedAsRead method completes successfully, THE InboxScreen SHALL exit multi-select mode and clear the selection

### Requirement 2

**User Story:** As a user, I want visual feedback when the mark as unread operation fails, so that I understand when the action was not successful

#### Acceptance Criteria

1. WHEN the markSelectedAsRead method fails, THE InboxViewModel SHALL update the error state with an appropriate error message
2. WHEN the error state is updated, THE InboxScreen SHALL display a snackbar with the error message
3. THE error message SHALL clearly indicate that the mark as unread operation failed
