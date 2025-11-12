package takagi.ru.fleur.ui.screens.inbox

import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.FleurError
import takagi.ru.fleur.domain.model.SyncResult

/**
 * 收件箱 UI 状态
 */
data class InboxUiState(
    val emails: List<Email> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: FleurError? = null,
    val viewMode: ViewMode = ViewMode.LIST,
    val selectedAccountId: String? = null,
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
    val isMultiSelectMode: Boolean = false,
    val selectedEmailIds: Set<String> = emptySet(),
    val isSyncing: Boolean = false,
    val lastSyncResult: List<SyncResult>? = null,
    val syncError: FleurError? = null,
    val isOffline: Boolean = false,
    val pendingOperationCount: Int = 0, // 已废弃，使用 pendingSyncCount 代替
    val pendingSyncCount: Int = 0, // 待同步操作数量（本地优先架构）
    val swipeRightAction: takagi.ru.fleur.domain.model.SwipeAction = takagi.ru.fleur.domain.model.SwipeAction.ARCHIVE,
    val swipeLeftAction: takagi.ru.fleur.domain.model.SwipeAction = takagi.ru.fleur.domain.model.SwipeAction.DELETE,
    val searchQuery: String = "",
    val allEmails: List<Email> = emptyList() // 保存未过滤的邮件列表
)

/**
 * 视图模式
 */
enum class ViewMode {
    LIST,    // 传统列表视图
    CHAT     // 聊天视图
}
