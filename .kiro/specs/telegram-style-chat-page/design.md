# Design Document

## Overview

本设计文档描述了 Fleur 邮件应用中 Telegram 风格 Chat 页面的详细设计。Chat 页面将传统邮件交流转换为即时通讯风格的对话界面，提供更直观、流畅的用户体验。

### 设计目标

1. **直观性**: 采用用户熟悉的聊天界面模式，降低学习成本
2. **高性能**: 实现 60fps 流畅滚动，快速响应用户操作
3. **功能完整**: 支持文字、图片、文件等所有邮件功能
4. **一致性**: 与应用现有的 Material 3 设计语言保持一致

### 技术栈

- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **State Management**: StateFlow + Compose State
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil
- **File Handling**: Android Storage Access Framework

## Architecture

### 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
├─────────────────────────────────────────────────────────┤
│  ChatScreen (Conversation List)                         │
│  ChatDetailScreen (Message Thread)                      │
│  ChatViewModel / ChatDetailViewModel                    │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
├─────────────────────────────────────────────────────────┤
│  GetConversationsUseCase                                │
│  GetConversationMessagesUseCase                         │
│  SendMessageUseCase                                     │
│  SearchMessagesUseCase                                  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
├─────────────────────────────────────────────────────────┤
│  EmailRepository (existing)                             │
│  ConversationMapper                                     │
└─────────────────────────────────────────────────────────┘
```

### 导航结构

Chat 页面通过底部导航栏的第二个按钮访问，导航流程如下：

```
Bottom Navigation (Chat Tab)
    │
    ├─> ChatScreen (对话列表)
    │       │
    │       └─> ChatDetailScreen (对话详情)
    │               │
    │               ├─> ImageViewer (图片全屏查看)
    │               ├─> FileViewer (文件查看)
    │               └─> ComposeScreen (回复/转发)
    │
    └─> SearchScreen (搜索对话)
```


## Components and Interfaces

### 1. ChatScreen (对话列表页面)

对话列表页面显示所有邮件对话，按最新消息时间排序。

#### UI 组件结构

```
ChatScreen
├── TopAppBar
│   ├── Title: "Chat"
│   ├── SearchIcon
│   └── MenuIcon
├── ConversationList (LazyColumn)
│   └── ConversationItem (多个)
│       ├── Avatar
│       ├── ConversationInfo
│       │   ├── ContactName
│       │   ├── LastMessagePreview
│       │   └── Timestamp
│       └── UnreadBadge (可选)
└── FloatingActionButton (新建对话)
```

#### ConversationItem 组件

```kotlin
@Composable
fun ConversationItem(
    conversation: ConversationUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**属性:**
- `conversation`: 对话 UI 模型
- `onClick`: 点击回调
- `modifier`: 修饰符

**视觉设计:**
- 左侧: 圆形头像 (48dp)
- 中间: 联系人名称 + 最后消息预览 (2行最大)
- 右侧: 时间戳 + 未读徽章
- 高度: 72dp
- 分隔线: 1dp, onSurfaceVariant 颜色


### 2. ChatDetailScreen (对话详情页面)

对话详情页面显示与特定联系人的完整消息线程，采用 Telegram 风格的气泡布局。

#### UI 组件结构

```
ChatDetailScreen
├── TopAppBar
│   ├── BackButton
│   ├── ContactInfo
│   │   ├── Avatar
│   │   └── Name
│   ├── SearchIcon
│   └── MoreIcon
├── MessageList (LazyColumn - reverseLayout)
│   └── MessageBubbleGroup (多个)
│       ├── DateDivider (可选)
│       └── MessageBubble (多个)
│           ├── Avatar (接收消息)
│           ├── BubbleContent
│           │   ├── SenderName (接收消息)
│           │   ├── MessageContent
│           │   │   ├── TextContent
│           │   │   ├── ImageContent (可选)
│           │   │   └── FileContent (可选)
│           │   └── MessageFooter
│           │       ├── Timestamp
│           │       └── StatusIndicator (发送消息)
│           └── Avatar (发送消息)
└── MessageInputBar
    ├── AttachmentButton
    ├── TextInput (可扩展)
    └── SendButton
```

#### MessageBubble 组件 (增强版)

基于现有的 `MessageBubble.kt`，需要增强以下功能:

```kotlin
@Composable
fun EnhancedMessageBubble(
    message: MessageUiModel,
    isSent: Boolean,
    showAvatar: Boolean,
    showSenderName: Boolean,
    onLongPress: () -> Unit,
    onImageClick: (String) -> Unit,
    onFileClick: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

**新增功能:**
- 长按手势支持
- 图片附件显示和点击
- 文件附件显示和下载
- 发送状态指示器
- 回复引用显示


#### MessageInputBar 组件

```kotlin
@Composable
fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachmentClick: () -> Unit,
    attachments: List<AttachmentPreview>,
    onRemoveAttachment: (String) -> Unit,
    replyTo: MessageUiModel?,
    onCancelReply: () -> Unit,
    modifier: Modifier = Modifier
)
```

**视觉设计:**
- 背景: Surface 颜色，顶部 1dp 分隔线
- 高度: 最小 56dp，最大 120dp (多行文本)
- 圆角: TextField 使用 24dp 圆角
- 按钮: IconButton，24dp 图标
- 附件预览: 水平滚动列表，显示在输入框上方

**交互行为:**
- 文本输入时自动扩展高度
- 有文本或附件时显示发送按钮
- 无内容时发送按钮禁用
- 回复模式时显示引用消息卡片

### 3. AttachmentBottomSheet (附件选择器)

```kotlin
@Composable
fun AttachmentBottomSheet(
    onImageSelect: () -> Unit,
    onFileSelect: () -> Unit,
    onCameraCapture: () -> Unit,
    onDismiss: () -> Unit
)
```

**选项:**
1. 📷 拍照
2. 🖼️ 图片
3. 📁 文件

**视觉设计:**
- 使用 Material 3 ModalBottomSheet
- 每个选项 56dp 高度
- 图标 + 文字布局
- 圆角: 28dp (顶部)


### 4. MessageActionsBottomSheet (消息操作菜单)

```kotlin
@Composable
fun MessageActionsBottomSheet(
    message: MessageUiModel,
    onCopy: () -> Unit,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
)
```

**操作选项:**
1. 📋 复制文本
2. ↩️ 回复
3. ➡️ 转发
4. 🗑️ 删除

**条件显示:**
- 复制: 仅当消息包含文本时
- 删除: 仅对自己发送的消息

### 5. ImageViewer (图片全屏查看器)

```kotlin
@Composable
fun ImageViewer(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit
)
```

**功能:**
- 全屏显示图片
- 支持缩放和平移
- 左右滑动切换图片
- 顶部工具栏: 关闭、分享、下载
- 底部指示器: 当前图片索引

**实现:**
- 使用 `HorizontalPager` 实现滑动
- 使用 `Modifier.transformable()` 实现缩放
- 使用 Coil 加载图片


## Data Models

### ConversationUiModel

```kotlin
data class ConversationUiModel(
    val id: String,                    // 对话ID (threadId)
    val contactName: String,           // 联系人名称
    val contactEmail: String,          // 联系人邮箱
    val contactAvatar: String?,        // 头像URL
    val lastMessage: String,           // 最后一条消息预览
    val lastMessageTime: Instant,      // 最后消息时间
    val unreadCount: Int,              // 未读消息数
    val hasAttachment: Boolean,        // 是否包含附件
    val isPinned: Boolean = false      // 是否置顶
)
```

### MessageUiModel

```kotlin
data class MessageUiModel(
    val id: String,                    // 消息ID
    val conversationId: String,        // 所属对话ID
    val senderId: String,              // 发送者ID
    val senderName: String,            // 发送者名称
    val senderAvatar: String?,         // 发送者头像
    val content: String,               // 消息文本内容
    val timestamp: Instant,            // 发送时间
    val status: MessageStatus,         // 消息状态
    val attachments: List<AttachmentUiModel>, // 附件列表
    val replyTo: MessageUiModel?,      // 回复的消息
    val isRead: Boolean                // 是否已读
)
```

### MessageStatus

```kotlin
enum class MessageStatus {
    SENDING,      // 发送中
    SENT,         // 已发送
    DELIVERED,    // 已送达
    READ,         // 已读
    FAILED        // 发送失败
}
```

### AttachmentUiModel

```kotlin
data class AttachmentUiModel(
    val id: String,
    val fileName: String,
    val fileSize: String,              // 格式化后的大小
    val mimeType: String,
    val thumbnailUrl: String?,         // 缩略图URL (图片)
    val downloadUrl: String?,          // 下载URL
    val localPath: String?,            // 本地路径
    val downloadProgress: Float?,      // 下载进度 (0-1)
    val isImage: Boolean,
    val isDownloaded: Boolean
)
```


### ChatUiState

```kotlin
data class ChatUiState(
    val conversations: List<ConversationUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true
)
```

### ChatDetailUiState

```kotlin
data class ChatDetailUiState(
    val conversationId: String,
    val contactName: String,
    val contactAvatar: String?,
    val messages: List<MessageUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val inputText: String = "",
    val attachments: List<AttachmentUiModel> = emptyList(),
    val replyTo: MessageUiModel? = null,
    val searchQuery: String = "",
    val searchResults: List<MessageUiModel> = emptyList()
)
```

## Domain Layer

### Use Cases

#### 1. GetConversationsUseCase

```kotlin
class GetConversationsUseCase(
    private val emailRepository: EmailRepository
) {
    operator fun invoke(
        accountId: String?,
        page: Int = 0
    ): Flow<Result<List<ConversationUiModel>>> {
        // 1. 从 EmailRepository 获取邮件
        // 2. 按 threadId 分组
        // 3. 每个线程取最新邮件作为对话预览
        // 4. 转换为 ConversationUiModel
        // 5. 按最新消息时间排序
    }
}
```


#### 2. GetConversationMessagesUseCase

```kotlin
class GetConversationMessagesUseCase(
    private val emailRepository: EmailRepository
) {
    operator fun invoke(
        threadId: String,
        page: Int = 0
    ): Flow<Result<List<MessageUiModel>>> {
        // 1. 从 EmailRepository 获取线程中的所有邮件
        // 2. 按时间排序
        // 3. 转换为 MessageUiModel
        // 4. 处理回复关系
    }
}
```

#### 3. SendMessageUseCase

```kotlin
class SendMessageUseCase(
    private val emailRepository: EmailRepository
) {
    suspend operator fun invoke(
        to: EmailAddress,
        subject: String,
        content: String,
        attachments: List<Attachment>,
        replyToId: String?
    ): Result<Unit> {
        // 1. 构建 Email 对象
        // 2. 调用 emailRepository.sendEmail()
        // 3. 处理发送结果
    }
}
```

#### 4. SearchMessagesUseCase

```kotlin
class SearchMessagesUseCase(
    private val emailRepository: EmailRepository
) {
    operator fun invoke(
        conversationId: String,
        query: String
    ): Flow<Result<List<MessageUiModel>>> {
        // 1. 在指定对话中搜索消息
        // 2. 高亮匹配文本
        // 3. 返回搜索结果
    }
}
```


### Mappers

#### ConversationMapper

```kotlin
object ConversationMapper {
    fun fromEmailThread(
        threadId: String,
        emails: List<Email>
    ): ConversationUiModel {
        val latestEmail = emails.maxByOrNull { it.timestamp }!!
        val unreadCount = emails.count { !it.isRead }
        
        return ConversationUiModel(
            id = threadId,
            contactName = latestEmail.from.name ?: latestEmail.from.address,
            contactEmail = latestEmail.from.address,
            contactAvatar = null, // TODO: 从联系人系统获取
            lastMessage = latestEmail.bodyPreview,
            lastMessageTime = latestEmail.timestamp,
            unreadCount = unreadCount,
            hasAttachment = emails.any { it.hasAttachments() }
        )
    }
}
```

#### MessageMapper

```kotlin
object MessageMapper {
    fun fromEmail(
        email: Email,
        currentUserEmail: String
    ): MessageUiModel {
        val isSent = email.from.address == currentUserEmail
        
        return MessageUiModel(
            id = email.id,
            conversationId = email.threadId,
            senderId = email.from.address,
            senderName = email.from.name ?: email.from.address,
            senderAvatar = null,
            content = email.bodyPlain,
            timestamp = email.timestamp,
            status = determineStatus(email, isSent),
            attachments = email.attachments.map { AttachmentMapper.fromAttachment(it) },
            replyTo = null, // TODO: 解析回复关系
            isRead = email.isRead
        )
    }
    
    private fun determineStatus(email: Email, isSent: Boolean): MessageStatus {
        return if (isSent) {
            MessageStatus.SENT // 简化处理，实际需要更复杂的逻辑
        } else {
            if (email.isRead) MessageStatus.READ else MessageStatus.DELIVERED
        }
    }
}
```


## ViewModels

### ChatViewModel

```kotlin
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var currentPage = 0
    
    init {
        loadConversations()
    }
    
    fun loadConversations(refresh: Boolean = false) {
        if (refresh) currentPage = 0
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !refresh, isRefreshing = refresh) }
            
            getConversationsUseCase(
                accountId = null, // 所有账户
                page = currentPage
            ).collect { result ->
                result.fold(
                    onSuccess = { conversations ->
                        _uiState.update { state ->
                            val newList = if (refresh) {
                                conversations
                            } else {
                                state.conversations + conversations
                            }
                            state.copy(
                                conversations = newList,
                                isLoading = false,
                                isRefreshing = false,
                                hasMore = conversations.isNotEmpty()
                            )
                        }
                        currentPage++
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = error.message
                            )
                        }
                    }
                )
            }
        }
    }
    
    fun loadMore() {
        if (!_uiState.value.isLoading && _uiState.value.hasMore) {
            loadConversations(refresh = false)
        }
    }
}
```


### ChatDetailViewModel

```kotlin
@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val getConversationMessagesUseCase: GetConversationMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val searchMessagesUseCase: SearchMessagesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val conversationId: String = savedStateHandle["conversationId"]!!
    
    private val _uiState = MutableStateFlow(
        ChatDetailUiState(conversationId = conversationId)
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()
    
    private var currentPage = 0
    
    init {
        loadMessages()
    }
    
    fun loadMessages(refresh: Boolean = false) {
        if (refresh) currentPage = 0
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !refresh, isRefreshing = refresh) }
            
            getConversationMessagesUseCase(
                threadId = conversationId,
                page = currentPage
            ).collect { result ->
                result.fold(
                    onSuccess = { messages ->
                        _uiState.update { state ->
                            val newList = if (refresh) {
                                messages
                            } else {
                                messages + state.messages // 旧消息在后
                            }
                            state.copy(
                                messages = newList,
                                isLoading = false,
                                isRefreshing = false,
                                hasMore = messages.isNotEmpty()
                            )
                        }
                        currentPage++
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = error.message
                            )
                        }
                    }
                )
            }
        }
    }
    
    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }
    
    fun addAttachment(attachment: AttachmentUiModel) {
        _uiState.update { 
            it.copy(attachments = it.attachments + attachment) 
        }
    }
    
    fun removeAttachment(attachmentId: String) {
        _uiState.update {
            it.copy(attachments = it.attachments.filter { a -> a.id != attachmentId })
        }
    }
    
    fun setReplyTo(message: MessageUiModel?) {
        _uiState.update { it.copy(replyTo = message) }
    }
    
    fun sendMessage() {
        val state = _uiState.value
        if (state.inputText.isBlank() && state.attachments.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            
            // TODO: 获取收件人信息
            val result = sendMessageUseCase(
                to = EmailAddress(""), // 从对话中获取
                subject = "", // 从线程中获取
                content = state.inputText,
                attachments = emptyList(), // 转换 attachments
                replyToId = state.replyTo?.id
            )
            
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            inputText = "",
                            attachments = emptyList(),
                            replyTo = null,
                            isSending = false
                        )
                    }
                    loadMessages(refresh = true)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }
    
    fun searchMessages(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        
        viewModelScope.launch {
            searchMessagesUseCase(conversationId, query).collect { result ->
                result.fold(
                    onSuccess = { results ->
                        _uiState.update { it.copy(searchResults = results) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
            }
        }
    }
}
```


## Error Handling

### 错误类型

1. **网络错误**: 无法连接到邮件服务器
2. **加载错误**: 获取对话或消息失败
3. **发送错误**: 消息发送失败
4. **附件错误**: 附件上传/下载失败
5. **权限错误**: 缺少存储或相机权限

### 错误处理策略

#### 1. 网络错误

```kotlin
// 显示 Snackbar 提示
Snackbar.make(view, "网络连接失败，请检查网络设置", Snackbar.LENGTH_LONG)
    .setAction("重试") { viewModel.loadMessages(refresh = true) }
    .show()
```

#### 2. 加载错误

```kotlin
// 在列表底部显示错误状态
if (uiState.error != null) {
    ErrorDisplay(
        message = uiState.error,
        onRetry = { viewModel.loadMessages(refresh = true) }
    )
}
```

#### 3. 发送错误

```kotlin
// 消息气泡显示错误图标，点击重试
MessageBubble(
    message = message,
    status = MessageStatus.FAILED,
    onRetryClick = { viewModel.retrySendMessage(message.id) }
)
```

#### 4. 附件错误

```kotlin
// 附件卡片显示错误状态
AttachmentCard(
    attachment = attachment,
    error = "下载失败",
    onRetry = { viewModel.retryDownload(attachment.id) }
)
```

#### 5. 权限错误

```kotlin
// 显示权限请求对话框
PermissionDialog(
    permission = Manifest.permission.READ_EXTERNAL_STORAGE,
    onGranted = { viewModel.selectImage() },
    onDenied = { /* 显示说明 */ }
)
```


## Testing Strategy

### 单元测试

#### 1. ViewModel 测试

```kotlin
@Test
fun `loadConversations should update state with conversations`() = runTest {
    // Given
    val mockConversations = listOf(/* mock data */)
    coEvery { getConversationsUseCase(any(), any()) } returns flowOf(Result.success(mockConversations))
    
    // When
    viewModel.loadConversations()
    
    // Then
    assertEquals(mockConversations, viewModel.uiState.value.conversations)
    assertFalse(viewModel.uiState.value.isLoading)
}

@Test
fun `sendMessage should clear input after success`() = runTest {
    // Given
    coEvery { sendMessageUseCase(any(), any(), any(), any(), any()) } returns Result.success(Unit)
    viewModel.updateInputText("Test message")
    
    // When
    viewModel.sendMessage()
    
    // Then
    assertEquals("", viewModel.uiState.value.inputText)
    assertFalse(viewModel.uiState.value.isSending)
}
```

#### 2. UseCase 测试

```kotlin
@Test
fun `GetConversationsUseCase should group emails by thread`() = runTest {
    // Given
    val mockEmails = listOf(/* emails with same threadId */)
    coEvery { emailRepository.getEmails(any(), any(), any()) } returns flowOf(Result.success(mockEmails))
    
    // When
    val result = getConversationsUseCase(null, 0).first()
    
    // Then
    assertTrue(result.isSuccess)
    assertEquals(1, result.getOrNull()?.size) // 应该合并为一个对话
}
```

#### 3. Mapper 测试

```kotlin
@Test
fun `ConversationMapper should map email thread correctly`() {
    // Given
    val emails = listOf(
        Email(/* ... */),
        Email(/* ... */)
    )
    
    // When
    val conversation = ConversationMapper.fromEmailThread("thread1", emails)
    
    // Then
    assertEquals("thread1", conversation.id)
    assertEquals(emails.last().bodyPreview, conversation.lastMessage)
}
```


### UI 测试

#### 1. 组件测试

```kotlin
@Test
fun `ConversationItem should display unread badge when unread count is greater than 0`() {
    composeTestRule.setContent {
        ConversationItem(
            conversation = ConversationUiModel(
                id = "1",
                contactName = "Test User",
                unreadCount = 5,
                /* ... */
            ),
            onClick = {}
        )
    }
    
    composeTestRule.onNodeWithText("5").assertIsDisplayed()
}

@Test
fun `MessageBubble should align right when isSent is true`() {
    composeTestRule.setContent {
        EnhancedMessageBubble(
            message = MessageUiModel(/* ... */),
            isSent = true,
            /* ... */
        )
    }
    
    // 验证布局对齐
    composeTestRule.onNode(hasTestTag("message_bubble"))
        .assertPositionInRootIsEqualTo(/* right aligned */)
}
```

#### 2. 集成测试

```kotlin
@Test
fun `clicking conversation should navigate to detail screen`() {
    // Given
    val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    
    composeTestRule.setContent {
        ChatScreen(
            navController = navController,
            /* ... */
        )
    }
    
    // When
    composeTestRule.onNodeWithText("Test Conversation").performClick()
    
    // Then
    assertEquals(
        "chat_detail/thread1",
        navController.currentBackStackEntry?.destination?.route
    )
}
```

### 性能测试

#### 1. 滚动性能

```kotlin
@Test
fun `message list should maintain 60fps with 1000 messages`() {
    val messages = List(1000) { MessageUiModel(/* ... */) }
    
    composeTestRule.setContent {
        MessageList(messages = messages)
    }
    
    // 测量滚动帧率
    val frameMetrics = measureScrollPerformance {
        composeTestRule.onNode(hasScrollAction())
            .performScrollToIndex(999)
    }
    
    assertTrue(frameMetrics.averageFps >= 60)
}
```

#### 2. 内存测试

```kotlin
@Test
fun `loading 100 conversations should not exceed memory threshold`() {
    val initialMemory = Runtime.getRuntime().totalMemory()
    
    viewModel.loadConversations()
    // 等待加载完成
    
    val finalMemory = Runtime.getRuntime().totalMemory()
    val memoryIncrease = finalMemory - initialMemory
    
    assertTrue(memoryIncrease < 50 * 1024 * 1024) // 小于 50MB
}
```


## Performance Optimization

### 1. LazyColumn 优化

```kotlin
LazyColumn(
    state = listState,
    // 使用稳定的 key 避免重组
    key = { message -> message.id }
) {
    items(
        items = messages,
        key = { it.id }
    ) { message ->
        EnhancedMessageBubble(
            message = message,
            // 使用 remember 缓存计算结果
            isSent = remember(message.senderId) { 
                message.senderId == currentUserId 
            }
        )
    }
}
```

### 2. 图片加载优化

```kotlin
// 使用 Coil 的内存和磁盘缓存
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .memoryCacheKey(imageUrl)
        .diskCacheKey(imageUrl)
        .crossfade(true)
        .size(Size.ORIGINAL) // 或指定大小
        .build(),
    contentDescription = null,
    modifier = Modifier.size(200.dp)
)
```

### 3. 分页加载

```kotlin
// 检测滚动到顶部，加载更多历史消息
val shouldLoadMore by remember {
    derivedStateOf {
        val firstVisibleItem = listState.firstVisibleItemIndex
        firstVisibleItem <= 5 && !uiState.isLoading && uiState.hasMore
    }
}

LaunchedEffect(shouldLoadMore) {
    if (shouldLoadMore) {
        viewModel.loadMore()
    }
}
```

### 4. 状态优化

```kotlin
// 使用 derivedStateOf 避免不必要的重组
val groupedMessages by remember {
    derivedStateOf {
        messages.groupBy { message ->
            message.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
    }
}
```

### 5. 附件预加载

```kotlin
// 预加载可见范围内的图片缩略图
LaunchedEffect(visibleMessages) {
    visibleMessages
        .flatMap { it.attachments }
        .filter { it.isImage }
        .forEach { attachment ->
            imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(attachment.thumbnailUrl)
                    .build()
            )
        }
}
```


## Accessibility

### 1. 语义化标签

```kotlin
MessageBubble(
    modifier = Modifier.semantics {
        contentDescription = "来自 ${message.senderName} 的消息: ${message.content}"
        role = Role.Button
    }
)
```

### 2. 触摸目标大小

所有可交互元素最小触摸目标为 48dp × 48dp:

```kotlin
IconButton(
    onClick = { /* ... */ },
    modifier = Modifier.size(48.dp) // 最小触摸目标
) {
    Icon(
        imageVector = Icons.Default.Send,
        contentDescription = "发送消息",
        modifier = Modifier.size(24.dp) // 图标大小
    )
}
```

### 3. 颜色对比度

确保文字和背景的对比度符合 WCAG AA 标准 (至少 4.5:1):

```kotlin
// 发送消息气泡
containerColor = MaterialTheme.colorScheme.primaryContainer,
contentColor = MaterialTheme.colorScheme.onPrimaryContainer,

// 接收消息气泡
containerColor = MaterialTheme.colorScheme.surfaceVariant,
contentColor = MaterialTheme.colorScheme.onSurfaceVariant
```

### 4. 屏幕阅读器支持

```kotlin
// 为图片提供描述
Image(
    painter = painterResource(id = R.drawable.attachment),
    contentDescription = "图片附件: ${attachment.fileName}"
)

// 为状态图标提供描述
Icon(
    imageVector = Icons.Default.Done,
    contentDescription = when (status) {
        MessageStatus.SENDING -> "消息发送中"
        MessageStatus.SENT -> "消息已发送"
        MessageStatus.DELIVERED -> "消息已送达"
        MessageStatus.READ -> "消息已读"
        MessageStatus.FAILED -> "消息发送失败"
    }
)
```

### 5. 焦点管理

```kotlin
// 发送消息后自动聚焦到输入框
LaunchedEffect(uiState.isSending) {
    if (!uiState.isSending && uiState.inputText.isEmpty()) {
        focusRequester.requestFocus()
    }
}

TextField(
    value = inputText,
    onValueChange = { /* ... */ },
    modifier = Modifier.focusRequester(focusRequester)
)
```


## Animation and Transitions

### 1. 页面转场动画

```kotlin
// 从对话列表到详情页
val enterTransition = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + fadeIn(animationSpec = tween(300))

val exitTransition = slideOutHorizontally(
    targetOffsetX = { -it / 3 },
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + fadeOut(animationSpec = tween(300))
```

### 2. 消息出现动画

```kotlin
@Composable
fun AnimatedMessageBubble(
    message: MessageUiModel,
    /* ... */
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)) + 
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
    ) {
        EnhancedMessageBubble(message = message, /* ... */)
    }
}
```

### 3. 输入框展开动画

```kotlin
TextField(
    value = text,
    onValueChange = { text = it },
    modifier = Modifier
        .fillMaxWidth()
        .animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
)
```

### 4. 发送按钮动画

```kotlin
val scale by animateFloatAsState(
    targetValue = if (canSend) 1f else 0.8f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
)

IconButton(
    onClick = { /* send */ },
    enabled = canSend,
    modifier = Modifier.scale(scale)
) {
    Icon(Icons.Default.Send, contentDescription = "发送")
}
```

### 5. 加载动画

```kotlin
@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}
```


## Navigation Integration

### 路由定义

```kotlin
// 在 Screen.kt 中添加
sealed class Screen(val route: String) {
    // ... 现有路由
    
    object Chat : Screen("chat")
    
    object ChatDetail : Screen("chat_detail/{conversationId}") {
        fun createRoute(conversationId: String) = "chat_detail/$conversationId"
    }
    
    object ImageViewer : Screen("image_viewer/{messageId}/{imageIndex}") {
        fun createRoute(messageId: String, imageIndex: Int) = 
            "image_viewer/$messageId/$imageIndex"
    }
}
```

### NavGraph 更新

```kotlin
// 在 NavGraph.kt 中添加
composable(Screen.Chat.route) {
    ChatScreen(
        onNavigateToDetail = { conversationId ->
            navController.navigate(Screen.ChatDetail.createRoute(conversationId))
        },
        onNavigateToSearch = {
            navController.navigate(Screen.Search.route)
        }
    )
}

composable(
    route = Screen.ChatDetail.route,
    arguments = listOf(
        navArgument("conversationId") { type = NavType.StringType }
    )
) {
    ChatDetailScreen(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToImageViewer = { messageId, imageIndex ->
            navController.navigate(
                Screen.ImageViewer.createRoute(messageId, imageIndex)
            )
        },
        onNavigateToCompose = { referenceEmailId, mode ->
            navController.navigate(
                Screen.Compose.createRoute(mode, referenceEmailId)
            )
        }
    )
}
```

### 底部导航栏集成

Chat 按钮已存在于 `FleurBottomNavigationBar.kt` 中，索引为 1。当用户点击 Chat 按钮时，`AppScaffold` 应该导航到 `Screen.Chat.route`。

```kotlin
// 在 AppScaffold.kt 中
FleurBottomNavigationBar(
    selectedItem = selectedBottomNavItem,
    onItemSelected = { index ->
        selectedBottomNavItem = index
        when (index) {
            0 -> navController.navigate(Screen.Inbox.route)
            1 -> navController.navigate(Screen.Chat.route) // Chat 页面
            2 -> navController.navigate(Screen.Contacts.route)
            3 -> navController.navigate(Screen.Calendar.route)
        }
    }
)
```


## File Structure

新增文件组织结构:

```
app/src/main/java/takagi/ru/fleur/
├── ui/
│   ├── screens/
│   │   └── chat/
│   │       ├── ChatScreen.kt                    # 对话列表页面
│   │       ├── ChatDetailScreen.kt              # 对话详情页面
│   │       ├── ChatViewModel.kt                 # 对话列表 ViewModel
│   │       ├── ChatDetailViewModel.kt           # 对话详情 ViewModel
│   │       ├── ChatUiState.kt                   # UI 状态定义
│   │       └── components/
│   │           ├── ConversationItem.kt          # 对话列表项
│   │           ├── EnhancedMessageBubble.kt     # 增强消息气泡
│   │           ├── MessageInputBar.kt           # 消息输入栏
│   │           ├── AttachmentBottomSheet.kt     # 附件选择器
│   │           ├── MessageActionsBottomSheet.kt # 消息操作菜单
│   │           ├── ImageViewer.kt               # 图片查看器
│   │           ├── AttachmentCard.kt            # 附件卡片
│   │           └── DateDivider.kt               # 日期分隔线
│   └── model/
│       ├── ConversationUiModel.kt               # 对话 UI 模型
│       ├── MessageUiModel.kt                    # 消息 UI 模型
│       └── AttachmentUiModel.kt                 # 附件 UI 模型
├── domain/
│   └── usecase/
│       ├── GetConversationsUseCase.kt           # 获取对话列表
│       ├── GetConversationMessagesUseCase.kt    # 获取对话消息
│       ├── SendMessageUseCase.kt                # 发送消息
│       └── SearchMessagesUseCase.kt             # 搜索消息
└── data/
    └── mapper/
        ├── ConversationMapper.kt                # 对话映射器
        ├── MessageMapper.kt                     # 消息映射器
        └── AttachmentMapper.kt                  # 附件映射器
```

## Dependencies

需要添加的依赖项:

```kotlin
// build.gradle.kts (app module)

dependencies {
    // 图片加载
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // 图片缩放和手势
    implementation("me.saket.telephoto:zoomable-image-coil:0.7.1")
    
    // 文件选择器
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // 权限处理
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // 已有依赖 (确认)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose")
    implementation("androidx.hilt:hilt-navigation-compose")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime")
}
```


## Implementation Phases

### Phase 1: 基础架构 (1-2 天)
- 创建数据模型 (ConversationUiModel, MessageUiModel, AttachmentUiModel)
- 实现 Mapper (ConversationMapper, MessageMapper)
- 创建 Use Cases (GetConversationsUseCase, GetConversationMessagesUseCase)
- 设置导航路由

### Phase 2: 对话列表 (2-3 天)
- 实现 ChatViewModel
- 创建 ChatScreen UI
- 实现 ConversationItem 组件
- 添加下拉刷新和分页加载
- 集成到底部导航栏

### Phase 3: 对话详情 (3-4 天)
- 实现 ChatDetailViewModel
- 创建 ChatDetailScreen UI
- 增强 MessageBubble 组件
- 实现消息分组和日期分隔
- 添加滚动到底部功能

### Phase 4: 消息输入 (2-3 天)
- 实现 MessageInputBar 组件
- 实现 SendMessageUseCase
- 添加文本输入和自动扩展
- 实现发送功能和状态显示
- 添加回复功能

### Phase 5: 附件支持 (3-4 天)
- 实现 AttachmentBottomSheet
- 添加图片选择和预览
- 添加文件选择
- 实现 AttachmentCard 组件
- 实现图片上传和压缩
- 实现文件下载

### Phase 6: 图片查看器 (1-2 天)
- 实现 ImageViewer 组件
- 添加缩放和平移手势
- 实现图片切换
- 添加分享和下载功能

### Phase 7: 消息操作 (2-3 天)
- 实现长按手势
- 实现 MessageActionsBottomSheet
- 添加复制、回复、转发、删除功能
- 实现滑动操作

### Phase 8: 搜索功能 (2-3 天)
- 实现 SearchMessagesUseCase
- 添加搜索 UI
- 实现搜索结果高亮
- 添加搜索结果导航

### Phase 9: 性能优化 (2-3 天)
- 优化 LazyColumn 性能
- 实现图片预加载
- 优化内存使用
- 添加缓存策略

### Phase 10: 测试和完善 (2-3 天)
- 编写单元测试
- 编写 UI 测试
- 性能测试
- Bug 修复和优化

**总计: 约 20-30 天**


## Design Decisions and Rationale

### 1. 为什么使用邮件线程 (threadId) 作为对话?

**决策**: 将邮件按 `threadId` 分组，每个线程作为一个对话。

**理由**:
- 邮件本身就有线程概念，自然映射到对话
- 保持与传统邮件客户端的兼容性
- 简化数据模型，无需额外的对话表

### 2. 为什么复用现有的 EmailRepository?

**决策**: 不创建新的 ChatRepository，而是通过 Use Cases 转换 EmailRepository 的数据。

**理由**:
- 避免数据重复和同步问题
- 保持单一数据源原则
- 降低实现复杂度

### 3. 为什么使用 reverseLayout 的 LazyColumn?

**决策**: 消息列表使用 `reverseLayout = true`，最新消息在底部。

**理由**:
- 符合聊天应用的用户习惯
- 新消息自动滚动到可见区域
- 加载历史消息时不影响当前位置

### 4. 为什么分离 ChatScreen 和 ChatDetailScreen?

**决策**: 对话列表和对话详情使用两个独立的 Screen。

**理由**:
- 清晰的导航层级
- 更好的性能 (不需要同时渲染两个列表)
- 支持深度链接和状态恢复

### 5. 为什么使用 BottomSheet 而不是 Dialog?

**决策**: 附件选择和消息操作使用 ModalBottomSheet。

**理由**:
- 符合 Material Design 3 规范
- 更好的移动端体验
- 支持手势关闭

### 6. 为什么不实现实时消息推送?

**决策**: 初版使用下拉刷新，不实现 WebSocket 推送。

**理由**:
- 邮件协议 (IMAP) 本身不是实时的
- 降低初版实现复杂度
- 可以在后续版本添加

### 7. 为什么限制附件大小和数量?

**决策**: 图片最多 10 张，文件最大 25MB。

**理由**:
- 避免内存溢出
- 控制上传时间
- 符合大多数邮件服务器限制

