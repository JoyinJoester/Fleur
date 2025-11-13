package takagi.ru.fleur.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import takagi.ru.fleur.ui.model.AttachmentUiModel
import takagi.ru.fleur.ui.screens.chat.components.AttachmentBottomSheet
import takagi.ru.fleur.ui.screens.chat.components.DateDivider
import takagi.ru.fleur.ui.screens.chat.components.EnhancedMessageBubble
import takagi.ru.fleur.ui.screens.chat.components.FilePreviewRow
import takagi.ru.fleur.ui.screens.chat.components.ImagePreviewRow
import takagi.ru.fleur.ui.screens.chat.components.MessageActionsBottomSheet
import takagi.ru.fleur.ui.screens.chat.components.MessageInputBar
import takagi.ru.fleur.ui.screens.chat.components.SearchBar
import takagi.ru.fleur.ui.screens.chat.components.isSameDay
import takagi.ru.fleur.util.AttachmentUploader
import takagi.ru.fleur.util.ImageCompressor
import java.util.UUID

/**
 * ChatDetail 页面
 * 
 * 显示对话详情，包括：
 * - 消息列表（按日期分组）
 * - 消息输入框
 * - 下拉刷新
 * - 滚动加载更多历史消息
 * 
 * @param onNavigateBack 返回回调
 * @param onNavigateToImageViewer 导航到图片查看器的回调
 * @param onNavigateToCompose 导航到撰写页面的回调
 * @param viewModel ViewModel
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ChatDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToImageViewer: (String, Int) -> Unit = { _, _ -> },
    onNavigateToCompose: (String, String) -> Unit = { _, _ -> },
    viewModel: ChatDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    
    // 消息操作底部弹窗状态
    var showMessageActions by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<takagi.ru.fleur.ui.model.MessageUiModel?>(null) }
    
    // 附件选择底部弹窗状态
    var showAttachmentSheet by remember { mutableStateOf(false) }
    
    // 搜索状态
    var showSearch by remember { mutableStateOf(false) }
    
    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            // 转换 URI 为 AttachmentUiModel
            val attachments = uris.mapIndexed { index, uri ->
                // 获取文件大小
                val fileSize = try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        ImageCompressor.formatFileSize(inputStream.available().toLong())
                    } ?: "未知"
                } catch (e: Exception) {
                    "未知"
                }
                
                AttachmentUiModel(
                    id = UUID.randomUUID().toString(),
                    fileName = "image_${System.currentTimeMillis()}_$index.jpg",
                    fileSize = fileSize,
                    mimeType = "image/jpeg",
                    thumbnailUrl = null,
                    downloadUrl = null,
                    localPath = uri.toString(),
                    downloadProgress = null,
                    isImage = true,
                    isDownloaded = true
                )
            }
            viewModel.addAttachments(attachments)
        }
    }
    
    // 相机拍照
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedImageUri != null) {
            val attachment = AttachmentUiModel(
                id = UUID.randomUUID().toString(),
                fileName = "camera_${System.currentTimeMillis()}.jpg",
                fileSize = "未知",
                mimeType = "image/jpeg",
                thumbnailUrl = null,
                downloadUrl = null,
                localPath = capturedImageUri.toString(),
                downloadProgress = null,
                isImage = true,
                isDownloaded = true
            )
            viewModel.addAttachment(attachment)
        }
    }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 获取文件信息
            val fileName = try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                } ?: "unknown_file"
            } catch (e: Exception) {
                "unknown_file"
            }
            
            // 获取文件大小
            val fileSize = try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val size = inputStream.available().toLong()
                    
                    // 检查文件大小限制 (25MB)
                    if (size > 25 * 1024 * 1024) {
                        viewModel.clearError()
                        scope.launch {
                            snackbarHostState.showSnackbar("文件大小不能超过 25MB")
                        }
                        return@rememberLauncherForActivityResult
                    }
                    
                    ImageCompressor.formatFileSize(size)
                } ?: "未知"
            } catch (e: Exception) {
                "未知"
            }
            
            // 获取 MIME 类型
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            
            val attachment = AttachmentUiModel(
                id = UUID.randomUUID().toString(),
                fileName = fileName,
                fileSize = fileSize,
                mimeType = mimeType,
                thumbnailUrl = null,
                downloadUrl = null,
                localPath = uri.toString(),
                downloadProgress = null,
                isImage = false,
                isDownloaded = true
            )
            viewModel.addAttachment(attachment)
        }
    }
    
    // 显示错误信息
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "重试",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(uiState.contactName)
                        if (uiState.contactEmail.isNotEmpty()) {
                            Text(
                                text = uiState.contactEmail,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 搜索按钮
                    IconButton(onClick = { showSearch = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    }
                    
                    // 更多菜单按钮
                    IconButton(onClick = { /* TODO: 显示菜单 */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索栏
            SearchBar(
                visible = showSearch,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { query ->
                    viewModel.searchMessages(query)
                },
                currentResultIndex = uiState.currentSearchResultIndex,
                totalResults = uiState.searchResults.size,
                onPreviousResult = {
                    viewModel.navigateToPreviousSearchResult()
                },
                onNextResult = {
                    viewModel.navigateToNextSearchResult()
                },
                onClose = {
                    showSearch = false
                    viewModel.closeSearch()
                }
            )
            
            // 消息列表
            Box(
                modifier = Modifier.weight(1f)
            ) {
                when {
                    // 初始加载状态
                    uiState.isLoading && uiState.messages.isEmpty() -> {
                        LoadingState()
                    }
                    
                    // 空状态
                    uiState.messages.isEmpty() && !uiState.isLoading -> {
                        EmptyState()
                    }
                    
                    // 消息列表
                    else -> {
                        MessageList(
                            messages = uiState.messages,
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = { viewModel.refresh() },
                            onMessageClick = { /* TODO: 处理消息点击 */ },
                            onMessageLongClick = { message ->
                                selectedMessage = message
                                showMessageActions = true
                            },
                            onNavigateToImageViewer = onNavigateToImageViewer,
                            listState = listState
                        )
                    }
                }
            }
            
            // 图片预览行
            ImagePreviewRow(
                attachments = uiState.attachments,
                onRemove = { attachmentId ->
                    viewModel.removeAttachment(attachmentId)
                }
            )
            
            // 文件预览行
            FilePreviewRow(
                attachments = uiState.attachments,
                onRemove = { attachmentId ->
                    viewModel.removeAttachment(attachmentId)
                }
            )
            
            // 消息输入栏
            MessageInputBar(
                text = uiState.inputText,
                onTextChange = { viewModel.updateInputText(it) },
                onSend = { viewModel.sendMessage() },
                onAttachmentClick = { showAttachmentSheet = true },
                replyTo = uiState.replyTo,
                onCancelReply = { viewModel.setReplyTo(null) },
                enabled = !uiState.isSending
            )
        }
        
        // 消息操作底部弹窗
        MessageActionsBottomSheet(
            visible = showMessageActions,
            message = selectedMessage,
            onDismiss = { showMessageActions = false },
            onCopy = { message ->
                // 复制到剪贴板
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message.content))
                scope.launch {
                    snackbarHostState.showSnackbar("已复制到剪贴板")
                }
            },
            onReply = { message ->
                viewModel.setReplyTo(message)
            },
            onForward = { message ->
                // TODO: 导航到撰写页面，预填充转发内容
                onNavigateToCompose(message.id, "FORWARD")
            },
            onDelete = { message ->
                // TODO: 实现删除消息功能
            }
        )
        
        // 附件选择底部弹窗
        if (showAttachmentSheet) {
            AttachmentBottomSheet(
                onDismiss = { showAttachmentSheet = false },
                onCameraCapture = {
                    // TODO: 创建临时文件 URI 用于拍照
                    // capturedImageUri = createImageUri(context)
                    // cameraLauncher.launch(capturedImageUri!!)
                },
                onImageSelect = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onFileSelect = {
                    filePickerLauncher.launch("*/*")
                }
            )
        }
    }
}

/**
 * 消息列表组件
 * 
 * 使用 reverseLayout 使最新消息显示在底部
 * 按日期分组显示消息
 * 
 * @param messages 消息列表
 * @param isRefreshing 是否正在刷新
 * @param onRefresh 刷新回调
 * @param onMessageClick 消息点击回调
 * @param onMessageLongClick 消息长按回调
 * @param listState 列表状态
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MessageList(
    messages: List<takagi.ru.fleur.ui.model.MessageUiModel>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onMessageClick: (takagi.ru.fleur.ui.model.MessageUiModel) -> Unit,
    onMessageLongClick: (takagi.ru.fleur.ui.model.MessageUiModel) -> Unit,
    onNavigateToImageViewer: (String, Int) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    // 下拉刷新
    androidx.compose.material.pullrefresh.PullRefreshIndicator(
        refreshing = isRefreshing,
        state = androidx.compose.material.pullrefresh.rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = onRefresh
        ),
        modifier = Modifier.fillMaxSize()
    )
    
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        reverseLayout = true, // 最新消息在底部
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // 反向遍历消息列表（因为使用了 reverseLayout）
        val reversedMessages = messages.reversed()
        
        reversedMessages.forEachIndexed { index, message ->
            // 判断是否需要显示日期分隔线
            val showDateDivider = if (index == 0) {
                // 第一条消息总是显示日期
                true
            } else {
                // 如果与上一条消息不在同一天，显示日期
                val previousMessage = reversedMessages[index - 1]
                !isSameDay(message.timestamp, previousMessage.timestamp)
            }
            
            // 日期分隔线
            if (showDateDivider) {
                item(key = "date_${message.timestamp}") {
                    DateDivider(timestamp = message.timestamp)
                }
            }
            
            // 消息气泡
            item(key = message.id) {
                // 判断是否为发送的消息
                // TODO: 从当前用户信息判断
                val isSent = false // 暂时默认为接收的消息
                
                // 判断是否显示头像
                // 如果下一条消息是同一个发送者，则不显示头像
                val showAvatar = if (index < reversedMessages.size - 1) {
                    val nextMessage = reversedMessages[index + 1]
                    nextMessage.senderId != message.senderId
                } else {
                    true
                }
                
                EnhancedMessageBubble(
                    message = message,
                    isSent = isSent,
                    showAvatar = showAvatar,
                    onClick = { onMessageClick(message) },
                    onLongClick = { onMessageLongClick(message) },
                    onImageClick = { imageIndex ->
                        // 导航到图片查看器
                        onNavigateToImageViewer(message.id, imageIndex)
                    }
                )
            }
        }
    }
}

/**
 * 加载状态组件
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 空状态组件
 */
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Chat,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "暂无消息",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "发送第一条消息开始对话",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
