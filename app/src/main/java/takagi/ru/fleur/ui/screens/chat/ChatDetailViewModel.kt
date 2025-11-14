package takagi.ru.fleur.ui.screens.chat

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import takagi.ru.fleur.domain.model.Attachment
import takagi.ru.fleur.domain.model.EmailAddress
import takagi.ru.fleur.domain.repository.AccountRepository
import takagi.ru.fleur.domain.usecase.GetConversationMessagesUseCase
import takagi.ru.fleur.domain.usecase.SendMessageUseCase
import takagi.ru.fleur.ui.model.AttachmentUiModel
import takagi.ru.fleur.ui.model.MessageUiModel
import takagi.ru.fleur.util.AttachmentUploader
import javax.inject.Inject

/**
 * ChatDetail 页面 ViewModel
 * 
 * 管理对话详情的状态和业务逻辑
 * 支持消息加载、发送、回复、附件管理等功能
 */
@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getConversationMessagesUseCase: GetConversationMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()
    
    // 从导航参数获取对话ID（联系人邮箱，需要URL解码）
    private val conversationId: String = run {
        val encodedId = savedStateHandle.get<String>("conversationId") ?: ""
        try {
            java.net.URLDecoder.decode(encodedId, "UTF-8")
        } catch (e: Exception) {
            Log.e(TAG, "解码对话ID失败: $encodedId", e)
            encodedId
        }
    }
    
    companion object {
        private const val TAG = "ChatDetailViewModel"
        private const val PAGE_SIZE = 50 // 每页加载 50 条消息
    }
    
    init {
        // 初始化对话ID（即联系人邮箱）
        _uiState.update { it.copy(conversationId = conversationId) }
        
        // 加载消息列表
        loadMessages()
    }
    
    /**
     * 加载消息列表
     * 
     * 首次加载时调用
     * 会重置分页状态并从第一页开始加载
     */
    fun loadMessages() {
        viewModelScope.launch {
            try {
                // 设置加载状态
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // 获取当前账户和邮箱
                val currentAccount = getCurrentAccount()
                val currentUserEmail = currentAccount?.email ?: ""
                val accountId = currentAccount?.id
                _uiState.update { it.copy(currentUserEmail = currentUserEmail) }
                if (currentAccount == null) {
                    Log.w(TAG, "未找到默认账户，使用所有账户的数据源")
                }
                
                Log.d(TAG, "开始加载消息 - 联系人: $conversationId, 账户: $accountId, 当前用户: $currentUserEmail")
                
                // 加载消息（conversationId 就是联系人邮箱）
                getConversationMessagesUseCase(
                    contactEmail = conversationId,
                    accountId = accountId,
                    currentUserEmail = currentUserEmail
                )
                .catch { exception ->
                    Log.e(TAG, "加载消息失败", exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "加载失败"
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { messages ->
                            Log.d(TAG, "加载消息成功: ${messages.size} 条消息")
                            
                            // 联系人信息直接使用 conversationId（即联系人邮箱）
                            // 从消息中查找联系人的显示名称
                            val contactDisplayName = messages.firstOrNull { message ->
                                message.senderId == conversationId
                            }?.senderName ?: conversationId
                            
                            _uiState.update {
                                it.copy(
                                    messages = messages,
                                    isLoading = false,
                                    error = null,
                                    contactName = contactDisplayName,
                                    contactEmail = conversationId, // 使用对话ID作为联系人邮箱
                                    currentUserEmail = currentUserEmail,
                                    currentPage = 0,
                                    hasMore = messages.size >= PAGE_SIZE
                                )
                            }
                        },
                        onFailure = { exception ->
                            Log.e(TAG, "加载消息失败", exception)
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = exception.message ?: "加载失败"
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载消息异常", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }
    
    /**
     * 刷新消息列表
     * 
     * 下拉刷新时调用
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                // 设置刷新状态
                _uiState.update { it.copy(isRefreshing = true, error = null) }
                
                // 获取当前账户和邮箱
                val currentAccount = getCurrentAccount()
                val currentUserEmail = currentAccount?.email ?: ""
                val accountId = currentAccount?.id
                _uiState.update { it.copy(currentUserEmail = currentUserEmail) }
                if (currentAccount == null) {
                    Log.w(TAG, "未找到默认账户，刷新时使用所有账户的数据源")
                }
                
                // 重新加载消息
                getConversationMessagesUseCase(
                    contactEmail = conversationId,
                    accountId = accountId,
                    currentUserEmail = currentUserEmail
                )
                .catch { exception ->
                    Log.e(TAG, "刷新消息失败", exception)
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = exception.message ?: "刷新失败"
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { messages ->
                            Log.d(TAG, "刷新消息成功: ${messages.size} 条消息")
                            _uiState.update {
                                it.copy(
                                    messages = messages,
                                    isRefreshing = false,
                                    error = null,
                                    currentPage = 0,
                                    hasMore = messages.size >= PAGE_SIZE
                                )
                            }
                        },
                        onFailure = { exception ->
                            Log.e(TAG, "刷新消息失败", exception)
                            _uiState.update {
                                it.copy(
                                    isRefreshing = false,
                                    error = exception.message ?: "刷新失败"
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "刷新消息异常", e)
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = e.message ?: "刷新失败"
                    )
                }
            }
        }
    }
    
    /**
     * 加载更多历史消息
     * 
     * 滚动到顶部时调用
     * TODO: 实现分页加载历史消息
     */
    fun loadMore() {
        // 暂不实现，因为 EmailRepository 的 getEmailThread 不支持分页
        Log.d(TAG, "加载更多历史消息（暂未实现）")
    }
    
    /**
     * 更新输入文本
     * 
     * @param text 新的输入文本
     */
    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }
    
    /**
     * 添加附件
     * 
     * @param attachment 附件
     */
    fun addAttachment(attachment: AttachmentUiModel) {
        val currentAttachments = _uiState.value.attachments
        
        // 限制图片数量最多 10 张
        if (attachment.isImage && currentAttachments.count { it.isImage } >= 10) {
            _uiState.update {
                it.copy(error = "最多只能选择 10 张图片")
            }
            return
        }
        
        _uiState.update {
            it.copy(attachments = it.attachments + attachment)
        }
    }
    
    /**
     * 添加多个附件
     * 
     * @param attachments 附件列表
     */
    fun addAttachments(attachments: List<AttachmentUiModel>) {
        val currentAttachments = _uiState.value.attachments
        val imageAttachments = attachments.filter { it.isImage }
        
        // 限制图片数量最多 10 张
        val currentImageCount = currentAttachments.count { it.isImage }
        if (imageAttachments.isNotEmpty() && currentImageCount + imageAttachments.size > 10) {
            _uiState.update {
                it.copy(error = "最多只能选择 10 张图片")
            }
            return
        }
        
        _uiState.update {
            it.copy(attachments = it.attachments + attachments)
        }
    }
    
    /**
     * 更新附件上传进度
     * 
     * @param attachmentId 附件ID
     * @param progress 进度（0-1）
     */
    fun updateAttachmentProgress(attachmentId: String, progress: Float) {
        _uiState.update { state ->
            val updatedAttachments = state.attachments.map { attachment ->
                if (attachment.id == attachmentId) {
                    attachment.copy(downloadProgress = progress)
                } else {
                    attachment
                }
            }
            state.copy(attachments = updatedAttachments)
        }
    }
    
    /**
     * 标记附件上传失败
     * 
     * @param attachmentId 附件ID
     */
    fun markAttachmentFailed(attachmentId: String) {
        _uiState.update { state ->
            state.copy(
                error = "附件上传失败",
                attachments = state.attachments.filter { it.id != attachmentId }
            )
        }
    }
    
    /**
     * 移除附件
     * 
     * @param attachmentId 附件ID
     */
    fun removeAttachment(attachmentId: String) {
        _uiState.update {
            it.copy(attachments = it.attachments.filter { att -> att.id != attachmentId })
        }
    }
    
    /**
     * 设置回复消息
     * 
     * @param message 要回复的消息，null 表示取消回复
     */
    fun setReplyTo(message: MessageUiModel?) {
        _uiState.update { it.copy(replyTo = message) }
    }
    
    /**
     * 发送消息
     */
    fun sendMessage() {
        val currentState = _uiState.value
        
        // 验证是否可以发送
        if (!currentState.canSend) {
            Log.w(TAG, "无法发送消息：输入为空或正在发送中")
            return
        }
        
        viewModelScope.launch {
            try {
                // 设置发送状态
                _uiState.update { it.copy(isSending = true, error = null) }
                
                // 获取当前账户信息
                val currentAccount = getCurrentAccount()
                if (currentAccount == null) {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            error = "请先登录账户"
                        )
                    }
                    return@launch
                }
                
                // 构建收件人列表
                val to = listOf(
                    EmailAddress(
                        name = currentState.contactName,
                        address = currentState.contactEmail
                    )
                )
                
                // 转换附件
                val attachments = currentState.attachments.map { uiAttachment ->
                    Attachment(
                        id = uiAttachment.id,
                        emailId = "", // 将在发送时生成
                        fileName = uiAttachment.fileName,
                        mimeType = uiAttachment.mimeType,
                        size = 0, // TODO: 从实际文件获取大小
                        url = uiAttachment.downloadUrl,
                        localPath = uiAttachment.localPath
                    )
                }
                
                // 发送消息
                val result = sendMessageUseCase(
                    threadId = conversationId,
                    accountId = currentAccount.id,
                    from = EmailAddress(
                        name = currentAccount.displayName,
                        address = currentAccount.email
                    ),
                    to = to,
                    subject = "Re: ${currentState.contactName}", // TODO: 从线程获取正确的主题
                    content = currentState.inputText,
                    attachments = attachments,
                    replyToEmailId = currentState.replyTo?.id
                )
                
                result.fold(
                    onSuccess = { sentEmail ->
                        Log.d(TAG, "消息发送成功")
                        
                        // 清空输入框和附件
                        _uiState.update {
                            it.copy(
                                inputText = "",
                                attachments = emptyList(),
                                replyTo = null,
                                isSending = false,
                                error = null
                            )
                        }
                        
                        // 刷新消息列表以显示新消息
                        refresh()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "消息发送失败", exception)
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                error = exception.message ?: "发送失败"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "发送消息异常", e)
                _uiState.update {
                    it.copy(
                        isSending = false,
                        error = e.message ?: "发送失败"
                    )
                }
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 搜索消息
     * 
     * @param query 搜索查询
     */
    fun searchMessages(query: String) {
        if (query.isBlank()) {
            // 清空搜索结果
            _uiState.update {
                it.copy(
                    searchQuery = "",
                    searchResults = emptyList(),
                    currentSearchResultIndex = 0
                )
            }
            return
        }
        
        // 在消息列表中搜索
        val results = _uiState.value.messages.filter { message ->
            message.content.contains(query, ignoreCase = true)
        }
        
        _uiState.update {
            it.copy(
                searchQuery = query,
                searchResults = results,
                currentSearchResultIndex = if (results.isNotEmpty()) 0 else -1
            )
        }
    }
    
    /**
     * 导航到上一个搜索结果
     */
    fun navigateToPreviousSearchResult() {
        val currentState = _uiState.value
        if (currentState.searchResults.isEmpty()) return
        
        val newIndex = if (currentState.currentSearchResultIndex > 0) {
            currentState.currentSearchResultIndex - 1
        } else {
            currentState.searchResults.size - 1
        }
        
        _uiState.update {
            it.copy(currentSearchResultIndex = newIndex)
        }
    }
    
    /**
     * 导航到下一个搜索结果
     */
    fun navigateToNextSearchResult() {
        val currentState = _uiState.value
        if (currentState.searchResults.isEmpty()) return
        
        val newIndex = if (currentState.currentSearchResultIndex < currentState.searchResults.size - 1) {
            currentState.currentSearchResultIndex + 1
        } else {
            0
        }
        
        _uiState.update {
            it.copy(currentSearchResultIndex = newIndex)
        }
    }
    
    /**
     * 关闭搜索
     */
    fun closeSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                currentSearchResultIndex = 0
            )
        }
    }
    
    /**
     * 获取当前账户
     * 
     * @return 当前账户，如果没有则返回 null
     */
    private suspend fun getCurrentAccount(): takagi.ru.fleur.domain.model.Account? {
        return try {
            accountRepository.getDefaultAccount().firstOrNull()
                ?: accountRepository.getAccounts().firstOrNull()?.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "获取当前账户失败", e)
            null
        }
    }
}
