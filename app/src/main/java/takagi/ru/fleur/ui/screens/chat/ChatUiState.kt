package takagi.ru.fleur.ui.screens.chat

import takagi.ru.fleur.ui.model.ConversationUiModel

/**
 * Chat 页面 UI 状态
 * 
 * @property conversations 对话列表
 * @property isLoading 是否正在加载
 * @property isRefreshing 是否正在刷新
 * @property isLoadingMore 是否正在加载更多
 * @property error 错误信息
 * @property hasMore 是否还有更多数据
 * @property currentPage 当前页码
 */
data class ChatUiState(
    val conversations: List<ConversationUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val selectedConversationIds: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false
)
