package takagi.ru.fleur.ui.screens.folder

import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.FleurError

/**
 * 文件夹页面 UI 状态
 * 
 * @property emails 邮件列表
 * @property isLoading 是否正在加载
 * @property isRefreshing 是否正在刷新
 * @property error 错误信息
 * @property currentPage 当前页码
 * @property hasMorePages 是否还有更多页
 * @property isMultiSelectMode 是否处于多选模式
 * @property selectedEmailIds 已选中的邮件ID集合
 * @property lastAction 最后执行的操作结果
 * @property showUndoSnackbar 是否显示撤销 Snackbar
 */
data class FolderUiState(
    val emails: List<Email> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: FleurError? = null,
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
    
    // 多选模式
    val isMultiSelectMode: Boolean = false,
    val selectedEmailIds: Set<String> = emptySet(),
    
    // 操作反馈
    val lastAction: ActionResult? = null,
    val showUndoSnackbar: Boolean = false
)

/**
 * 操作结果
 * 
 * @property action 执行的操作类型
 * @property emailIds 受影响的邮件ID列表
 * @property timestamp 操作时间戳
 * @property canUndo 是否可以撤销
 */
data class ActionResult(
    val action: EmailAction,
    val emailIds: List<String>,
    val timestamp: Long,
    val canUndo: Boolean = true
)
