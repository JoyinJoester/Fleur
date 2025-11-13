# Requirements Document

## Introduction

本文档定义了 Fleur 邮件应用底栏 Chat 页面的需求。Chat 页面采用类似 Telegram 的聊天界面设计，将传统邮件交流转换为更直观、便捷的即时通讯风格对话。该页面支持文字、图片、文件等多种内容类型，提供流畅的用户体验，使邮件交流像聊天一样自然。

## Glossary

- **Chat_System**: Fleur 应用中的聊天页面系统，负责以对话形式展示和管理邮件
- **Conversation_Thread**: 与特定联系人或邮件主题相关的完整对话线程
- **Message_Bubble**: 单条消息的视觉容器，类似 Telegram 的消息气泡
- **Bottom_Navigation**: 应用底部导航栏，包含 Chat 页面入口
- **Attachment_Handler**: 处理图片、文件等附件的上传、下载和预览功能
- **Input_Composer**: 消息输入区域，支持文字输入和附件添加
- **Contact_Avatar**: 联系人头像显示组件
- **Timestamp_Display**: 消息时间戳显示组件
- **Swipe_Actions**: 消息项的滑动操作功能
- **Long_Press_Menu**: 长按消息触发的上下文菜单

## Requirements

### Requirement 1

**User Story:** 作为用户，我希望看到所有对话列表，以便快速找到并进入特定对话

#### Acceptance Criteria

1. THE Chat_System SHALL display all Conversation_Threads in a scrollable list ordered by most recent activity
2. WHEN a new message arrives, THE Chat_System SHALL move the corresponding Conversation_Thread to the top of the list
3. THE Chat_System SHALL display Contact_Avatar, contact name, last message preview, and Timestamp_Display for each conversation
4. THE Chat_System SHALL show unread message count badge on conversations with unread messages
5. WHEN the user taps a conversation item, THE Chat_System SHALL open the conversation detail screen within 200ms
6. THE Chat_System SHALL display a visual indicator for conversations with attachments in the last message

### Requirement 2

**User Story:** 作为用户，我希望在对话详情页看到 Telegram 风格的消息气泡，以便清晰区分发送和接收的消息

#### Acceptance Criteria

1. THE Chat_System SHALL display sent messages as Message_Bubbles aligned to the right with primary color background
2. THE Chat_System SHALL display received messages as Message_Bubbles aligned to the left with surface color background
3. THE Message_Bubble SHALL include message content, Timestamp_Display, and delivery status indicator
4. THE Chat_System SHALL group consecutive messages from the same sender within 5 minutes
5. THE Message_Bubble SHALL adapt its width based on content length with maximum 80% screen width
6. THE Chat_System SHALL display Contact_Avatar for received messages at the start of each message group

### Requirement 3

**User Story:** 作为用户，我希望能够发送文字消息，以便与联系人进行文字交流

#### Acceptance Criteria

1. THE Input_Composer SHALL display a text input field at the bottom of the conversation screen
2. THE Input_Composer SHALL expand vertically up to 5 lines as the user types multi-line text
3. WHEN the user taps the send button, THE Chat_System SHALL send the message and display it in the conversation within 500ms
4. THE Chat_System SHALL show a sending indicator while the message is being sent
5. IF message sending fails, THEN THE Chat_System SHALL display an error indicator and provide a retry option
6. THE Input_Composer SHALL remain fixed at the bottom while the keyboard is visible

### Requirement 4

**User Story:** 作为用户，我希望能够发送图片，以便在对话中分享视觉内容

#### Acceptance Criteria

1. THE Input_Composer SHALL display an attachment button that opens the Attachment_Handler
2. WHEN the user selects images, THE Attachment_Handler SHALL display image thumbnails in the Input_Composer
3. THE Chat_System SHALL allow the user to select up to 10 images at once
4. THE Message_Bubble SHALL display image attachments with appropriate aspect ratio and maximum 300dp width
5. WHEN the user taps an image in Message_Bubble, THE Chat_System SHALL open a full-screen image viewer
6. THE Chat_System SHALL compress images larger than 5MB before sending while maintaining acceptable quality

### Requirement 5

**User Story:** 作为用户，我希望能够发送文件，以便在对话中分享文档和其他文件类型

#### Acceptance Criteria

1. THE Attachment_Handler SHALL allow the user to select files from device storage
2. THE Message_Bubble SHALL display file attachments with file name, size, and type icon
3. THE Chat_System SHALL support common file types including PDF, DOC, XLS, ZIP, and TXT
4. WHEN the user taps a file attachment, THE Chat_System SHALL download and open the file with appropriate application
5. THE Chat_System SHALL display download progress indicator for file attachments being downloaded
6. THE Chat_System SHALL limit individual file size to 25MB and display error message for larger files

### Requirement 6

**User Story:** 作为用户，我希望能够对消息进行操作，以便管理对话内容

#### Acceptance Criteria

1. WHEN the user long-presses a Message_Bubble, THE Chat_System SHALL display the Long_Press_Menu with available actions
2. THE Long_Press_Menu SHALL include options for copy, forward, delete, and reply
3. WHEN the user selects copy, THE Chat_System SHALL copy the message text to clipboard
4. WHEN the user selects delete, THE Chat_System SHALL remove the message from the conversation after confirmation
5. WHEN the user selects reply, THE Input_Composer SHALL show the quoted message and focus the text input
6. THE Chat_System SHALL support Swipe_Actions for quick reply and delete on message items

### Requirement 7

**User Story:** 作为用户，我希望看到消息的发送状态，以便了解消息是否成功送达

#### Acceptance Criteria

1. THE Message_Bubble SHALL display a sending indicator (single checkmark) while the message is being sent
2. WHEN the message is successfully sent, THE Message_Bubble SHALL display a sent indicator (double checkmark)
3. IF the message fails to send, THEN THE Message_Bubble SHALL display an error icon in red color
4. THE Chat_System SHALL allow the user to tap the error icon to retry sending the failed message
5. THE Message_Bubble SHALL display read status indicator when the recipient has read the message
6. THE status indicators SHALL be positioned at the bottom-right corner of sent Message_Bubbles

### Requirement 8

**User Story:** 作为用户，我希望能够搜索对话内容，以便快速找到特定消息

#### Acceptance Criteria

1. THE Chat_System SHALL display a search icon in the conversation screen toolbar
2. WHEN the user taps the search icon, THE Chat_System SHALL display a search input field
3. THE Chat_System SHALL highlight matching text in Message_Bubbles as the user types
4. THE Chat_System SHALL display navigation controls to jump between search results
5. THE Chat_System SHALL show the total count of search results
6. WHEN the user clears the search, THE Chat_System SHALL return to normal conversation view

### Requirement 9

**User Story:** 作为用户，我希望界面流畅且响应迅速，以便获得类似 Telegram 的优质体验

#### Acceptance Criteria

1. THE Chat_System SHALL load and display the conversation list within 500ms of navigation
2. THE Chat_System SHALL render new messages with smooth animation within 200ms
3. THE Chat_System SHALL maintain 60fps scrolling performance with up to 1000 messages loaded
4. THE Chat_System SHALL implement lazy loading to load older messages when scrolling to the top
5. THE Chat_System SHALL cache conversation data locally for offline viewing
6. THE Input_Composer SHALL respond to user input with less than 50ms latency
