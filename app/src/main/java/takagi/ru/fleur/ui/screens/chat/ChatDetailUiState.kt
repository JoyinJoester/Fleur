package takagi.ru.fleur.ui.screens.chat

import takagi.ru.fleur.ui.model.AttachmentUiModel
import takagi.ru.fleur.ui.model.MessageUiModel

/**
 * ChatDetail 页面 UI 状态
 * 
 * @property messages 消息列表
 * @property inputText 输入框文本
 * @property attachments 待发送的附件列表
 * @property replyTo 回复的消息
 * @property isLoading 是否正在加载
 * @property isRefreshing 是否正在刷新
 * @property isLoadingMore 是否正在加载更多历史消息
 * @property isSending 是否正在发送消息
 * @property error 错误信息
 * @property hasMore 是否还有更多历史消息
 * @property currentPage 当前页码
 * @property conversationId 对话ID
 * @property contactName 联系人名称
 * @property contactEmail 联系人邮箱
 * @property searchQuery 搜索查询
 * @property searchResults 搜索结果列表
 * @property currentSearchResultIndex 当前搜索结果索引
 */
data class ChatDetailUiState(
    val messages: List<MessageUiModel> = emptyList(),
    val inputText: String = "",
    val attachments: List<AttachmentUiModel> = emptyList(),
    val replyTo: MessageUiModel? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val conversationId: String = "",
    val contactName: String = "",
    val contactEmail: String = "",
    val currentUserEmail: String = "",
    val searchQuery: String = "",
    val searchResults: List<MessageUiModel> = emptyList(),
    val currentSearchResultIndex: Int = 0
) {
    /**
     * 是否可以发送消息
     * 
     * 条件：输入文本不为空或有附件，且不在发送中
     */
    val canSend: Boolean
        get() = (inputText.isNotBlank() || attachments.isNotEmpty()) && !isSending
}
