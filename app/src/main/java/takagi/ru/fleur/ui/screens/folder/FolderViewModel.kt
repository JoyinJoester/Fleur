package takagi.ru.fleur.ui.screens.folder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import takagi.ru.fleur.domain.model.FleurError
import takagi.ru.fleur.domain.usecase.ArchiveEmailUseCase
import takagi.ru.fleur.domain.usecase.DeleteEmailUseCase
import takagi.ru.fleur.domain.usecase.GetFolderEmailsUseCase
import takagi.ru.fleur.domain.usecase.MarkAsReadUseCase
import takagi.ru.fleur.domain.usecase.RestoreEmailUseCase
import takagi.ru.fleur.domain.usecase.ToggleStarUseCase
import javax.inject.Inject

/**
 * 文件夹页面 ViewModel
 * 管理所有文件夹页面（已发送、草稿箱、星标、归档、垃圾箱）的状态和业务逻辑
 * 
 * @property getFolderEmailsUseCase 获取文件夹邮件用例
 * @property deleteEmailUseCase 删除邮件用例
 * @property archiveEmailUseCase 归档邮件用例
 * @property restoreEmailUseCase 恢复邮件用例
 * @property toggleStarUseCase 切换星标用例
 * @property markAsReadUseCase 标记已读用例
 * @property savedStateHandle 保存状态句柄
 */
@HiltViewModel
class FolderViewModel @Inject constructor(
    private val getFolderEmailsUseCase: GetFolderEmailsUseCase,
    private val deleteEmailUseCase: DeleteEmailUseCase,
    private val archiveEmailUseCase: ArchiveEmailUseCase,
    private val restoreEmailUseCase: RestoreEmailUseCase,
    private val toggleStarUseCase: ToggleStarUseCase,
    private val markAsReadUseCase: MarkAsReadUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FolderUiState())
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()
    
    // 当前文件夹类型
    private var currentFolderType: FolderType? = null
    
    // 当前账户ID（从导航参数或默认账户获取）
    private val accountId: String
        get() = savedStateHandle.get<String>("accountId") ?: "default_account"
    
    companion object {
        private const val PAGE_SIZE = 50
        private const val UNDO_TIMEOUT_MS = 5000L // 撤销超时时间：5秒
    }
    
    /**
     * 初始化文件夹类型并加载邮件
     * @param folderType 文件夹类型
     */
    fun initialize(folderType: FolderType) {
        if (currentFolderType != folderType) {
            currentFolderType = folderType
            loadEmails(reset = true)
        }
    }
    
    /**
     * 加载邮件列表
     * @param reset 是否重置列表（从第一页开始）
     */
    fun loadEmails(reset: Boolean = false) {
        val folderType = currentFolderType ?: return
        
        viewModelScope.launch {
            if (reset) {
                _uiState.update { 
                    it.copy(
                        currentPage = 0, 
                        emails = emptyList(),
                        hasMorePages = true
                    ) 
                }
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val currentPage = _uiState.value.currentPage
            
            getFolderEmailsUseCase(
                folderType = folderType,
                accountId = accountId,
                page = currentPage,
                pageSize = PAGE_SIZE
            )
                .catch { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = FleurError.DatabaseError(e.message ?: "加载失败")
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { newEmails ->
                            _uiState.update { currentState ->
                                val updatedEmails = if (reset) {
                                    newEmails
                                } else {
                                    currentState.emails + newEmails
                                }
                                
                                currentState.copy(
                                    emails = updatedEmails,
                                    isLoading = false,
                                    hasMorePages = newEmails.size >= PAGE_SIZE,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = error as? FleurError 
                                        ?: FleurError.UnknownError(error.message ?: "未知错误")
                                )
                            }
                        }
                    )
                }
        }
    }
    
    /**
     * 刷新邮件列表（下拉刷新）
     */
    fun refreshEmails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            // 重置到第一页
            _uiState.update { it.copy(currentPage = 0) }
            
            loadEmails(reset = true)
            
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
    
    /**
     * 加载下一页（分页加载）
     */
    fun loadNextPage() {
        // 如果正在加载或没有更多页，则不执行
        if (_uiState.value.isLoading || !_uiState.value.hasMorePages) {
            return
        }
        
        _uiState.update { it.copy(currentPage = it.currentPage + 1) }
        loadEmails(reset = false)
    }
    
    /**
     * 执行单个邮件操作
     * @param emailId 邮件ID
     * @param action 操作类型
     */
    fun performAction(emailId: String, action: EmailAction) {
        viewModelScope.launch {
            val result = when (action) {
                EmailAction.DELETE -> deleteEmailUseCase(emailId)
                EmailAction.ARCHIVE -> archiveEmailUseCase(emailId)
                EmailAction.UNARCHIVE -> {
                    // 取消归档实际上是恢复到收件箱
                    restoreEmailUseCase(emailId)
                }
                EmailAction.STAR -> toggleStarUseCase(emailId, true)
                EmailAction.UNSTAR -> toggleStarUseCase(emailId, false)
                EmailAction.RESTORE -> restoreEmailUseCase(emailId)
                EmailAction.MARK_READ -> markAsReadUseCase(emailId, true)
                EmailAction.MARK_UNREAD -> markAsReadUseCase(emailId, false)
            }
            
            result.fold(
                onSuccess = {
                    // 记录操作结果，用于撤销
                    val actionResult = ActionResult(
                        action = action,
                        emailIds = listOf(emailId),
                        timestamp = System.currentTimeMillis(),
                        canUndo = canUndoAction(action)
                    )
                    
                    _uiState.update { 
                        it.copy(
                            lastAction = actionResult,
                            showUndoSnackbar = actionResult.canUndo
                        )
                    }
                    
                    // 根据操作类型更新 UI
                    when (action) {
                        EmailAction.STAR, EmailAction.UNSTAR -> {
                            // 标星/取消标星：更新邮件状态而不是移除
                            val newStarredState = action == EmailAction.STAR
                            _uiState.update { currentState ->
                                currentState.copy(
                                    emails = currentState.emails.map { email ->
                                        if (email.id == emailId) {
                                            email.copy(isStarred = newStarredState)
                                        } else {
                                            email
                                        }
                                    }
                                )
                            }
                        }
                        EmailAction.MARK_READ, EmailAction.MARK_UNREAD -> {
                            // 标记已读/未读：更新邮件状态而不是移除
                            val newReadState = action == EmailAction.MARK_READ
                            _uiState.update { currentState ->
                                currentState.copy(
                                    emails = currentState.emails.map { email ->
                                        if (email.id == emailId) {
                                            email.copy(isRead = newReadState)
                                        } else {
                                            email
                                        }
                                    }
                                )
                            }
                        }
                        else -> {
                            // 其他操作（删除、归档、恢复等）：从列表中移除邮件
                            _uiState.update { currentState ->
                                currentState.copy(
                                    emails = currentState.emails.filter { it.id != emailId }
                                )
                            }
                        }
                    }
                    
                    // 如果显示撤销 Snackbar，设置自动隐藏
                    if (actionResult.canUndo) {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(UNDO_TIMEOUT_MS)
                            _uiState.update { it.copy(showUndoSnackbar = false) }
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            error = error as? FleurError 
                                ?: FleurError.UnknownError(error.message ?: "操作失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 执行批量邮件操作
     * @param emailIds 邮件ID列表
     * @param action 操作类型
     */
    fun performBatchAction(emailIds: List<String>, action: EmailAction) {
        viewModelScope.launch {
            val result = when (action) {
                EmailAction.DELETE -> deleteEmailUseCase.deleteMultiple(emailIds)
                EmailAction.ARCHIVE -> archiveEmailUseCase.archiveMultiple(emailIds)
                EmailAction.UNARCHIVE -> restoreEmailUseCase.restoreMultiple(emailIds)
                EmailAction.STAR -> toggleStarUseCase.toggleMultiple(emailIds, true)
                EmailAction.UNSTAR -> toggleStarUseCase.toggleMultiple(emailIds, false)
                EmailAction.RESTORE -> restoreEmailUseCase.restoreMultiple(emailIds)
                EmailAction.MARK_READ -> markAsReadUseCase.markMultiple(emailIds, true)
                EmailAction.MARK_UNREAD -> markAsReadUseCase.markMultiple(emailIds, false)
            }
            
            result.fold(
                onSuccess = {
                    // 记录操作结果
                    val actionResult = ActionResult(
                        action = action,
                        emailIds = emailIds,
                        timestamp = System.currentTimeMillis(),
                        canUndo = canUndoAction(action)
                    )
                    
                    _uiState.update { 
                        it.copy(
                            lastAction = actionResult,
                            showUndoSnackbar = actionResult.canUndo,
                            isMultiSelectMode = false,
                            selectedEmailIds = emptySet()
                        )
                    }
                    
                    // 根据操作类型更新 UI
                    when (action) {
                        EmailAction.STAR, EmailAction.UNSTAR -> {
                            // 标星/取消标星：更新邮件状态而不是移除
                            val newStarredState = action == EmailAction.STAR
                            _uiState.update { currentState ->
                                currentState.copy(
                                    emails = currentState.emails.map { email ->
                                        if (email.id in emailIds) {
                                            email.copy(isStarred = newStarredState)
                                        } else {
                                            email
                                        }
                                    }
                                )
                            }
                        }
                        EmailAction.MARK_READ, EmailAction.MARK_UNREAD -> {
                            // 标记已读/未读：更新邮件状态而不是移除
                            val newReadState = action == EmailAction.MARK_READ
                            _uiState.update { currentState ->
                                currentState.copy(
                                    emails = currentState.emails.map { email ->
                                        if (email.id in emailIds) {
                                            email.copy(isRead = newReadState)
                                        } else {
                                            email
                                        }
                                    }
                                )
                            }
                        }
                        else -> {
                            // 其他操作（删除、归档、恢复等）：从列表中移除邮件
                            _uiState.update { currentState ->
                                currentState.copy(
                                    emails = currentState.emails.filter { it.id !in emailIds }
                                )
                            }
                        }
                    }
                    
                    // 设置自动隐藏 Snackbar
                    if (actionResult.canUndo) {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(UNDO_TIMEOUT_MS)
                            _uiState.update { it.copy(showUndoSnackbar = false) }
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            error = error as? FleurError 
                                ?: FleurError.UnknownError(error.message ?: "批量操作失败")
                        )
                    }
                }
            )
        }
    }

    
    /**
     * 撤销最后的操作
     */
    fun undoLastAction() {
        val lastAction = _uiState.value.lastAction ?: return
        
        // 隐藏 Snackbar
        _uiState.update { it.copy(showUndoSnackbar = false) }
        
        viewModelScope.launch {
            // 执行相反的操作
            val undoAction = getUndoAction(lastAction.action)
            
            if (undoAction != null) {
                val result = if (lastAction.emailIds.size == 1) {
                    performUndoAction(lastAction.emailIds.first(), undoAction)
                } else {
                    performBatchUndoAction(lastAction.emailIds, undoAction)
                }
                
                result.fold(
                    onSuccess = {
                        // 清除最后操作记录
                        _uiState.update { it.copy(lastAction = null) }
                        
                        // 刷新列表
                        refreshEmails()
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                error = error as? FleurError 
                                    ?: FleurError.UnknownError(error.message ?: "撤销失败")
                            )
                        }
                    }
                )
            }
        }
    }
    
    /**
     * 进入多选模式
     * @param emailId 初始选中的邮件ID
     */
    fun enterMultiSelectMode(emailId: String) {
        _uiState.update { 
            it.copy(
                isMultiSelectMode = true,
                selectedEmailIds = setOf(emailId)
            )
        }
    }
    
    /**
     * 退出多选模式
     */
    fun exitMultiSelectMode() {
        _uiState.update { 
            it.copy(
                isMultiSelectMode = false,
                selectedEmailIds = emptySet()
            )
        }
    }
    
    /**
     * 切换邮件选中状态
     * @param emailId 邮件ID
     */
    fun toggleEmailSelection(emailId: String) {
        _uiState.update { currentState ->
            val selectedIds = currentState.selectedEmailIds.toMutableSet()
            
            if (emailId in selectedIds) {
                selectedIds.remove(emailId)
            } else {
                selectedIds.add(emailId)
            }
            
            // 如果没有选中任何邮件，退出多选模式
            if (selectedIds.isEmpty()) {
                currentState.copy(
                    isMultiSelectMode = false,
                    selectedEmailIds = emptySet()
                )
            } else {
                currentState.copy(selectedEmailIds = selectedIds)
            }
        }
    }
    
    /**
     * 全选邮件
     */
    fun selectAllEmails() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedEmailIds = currentState.emails.map { it.id }.toSet()
            )
        }
    }
    
    /**
     * 取消全选
     */
    fun deselectAllEmails() {
        _uiState.update { 
            it.copy(selectedEmailIds = emptySet())
        }
    }
    
    /**
     * 隐藏错误提示
     */
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 隐藏撤销 Snackbar
     */
    fun dismissUndoSnackbar() {
        _uiState.update { it.copy(showUndoSnackbar = false) }
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 判断操作是否可以撤销
     */
    private fun canUndoAction(action: EmailAction): Boolean {
        return when (action) {
            EmailAction.DELETE,
            EmailAction.ARCHIVE,
            EmailAction.UNARCHIVE,
            EmailAction.STAR,
            EmailAction.UNSTAR,
            EmailAction.RESTORE -> true
            EmailAction.MARK_READ,
            EmailAction.MARK_UNREAD -> false
        }
    }
    
    /**
     * 获取撤销操作
     */
    private fun getUndoAction(action: EmailAction): EmailAction? {
        return when (action) {
            EmailAction.DELETE -> EmailAction.RESTORE
            EmailAction.ARCHIVE -> EmailAction.UNARCHIVE
            EmailAction.UNARCHIVE -> EmailAction.ARCHIVE
            EmailAction.STAR -> EmailAction.UNSTAR
            EmailAction.UNSTAR -> EmailAction.STAR
            EmailAction.RESTORE -> EmailAction.DELETE
            else -> null
        }
    }
    
    /**
     * 执行单个邮件的撤销操作
     */
    private suspend fun performUndoAction(emailId: String, action: EmailAction): Result<Unit> {
        return when (action) {
            EmailAction.DELETE -> deleteEmailUseCase(emailId)
            EmailAction.ARCHIVE -> archiveEmailUseCase(emailId)
            EmailAction.UNARCHIVE -> restoreEmailUseCase(emailId)
            EmailAction.STAR -> toggleStarUseCase(emailId, true)
            EmailAction.UNSTAR -> toggleStarUseCase(emailId, false)
            EmailAction.RESTORE -> restoreEmailUseCase(emailId)
            EmailAction.MARK_READ -> markAsReadUseCase(emailId, true)
            EmailAction.MARK_UNREAD -> markAsReadUseCase(emailId, false)
        }
    }
    
    /**
     * 执行批量邮件的撤销操作
     */
    private suspend fun performBatchUndoAction(emailIds: List<String>, action: EmailAction): Result<Unit> {
        return when (action) {
            EmailAction.DELETE -> deleteEmailUseCase.deleteMultiple(emailIds)
            EmailAction.ARCHIVE -> archiveEmailUseCase.archiveMultiple(emailIds)
            EmailAction.UNARCHIVE -> restoreEmailUseCase.restoreMultiple(emailIds)
            EmailAction.STAR -> toggleStarUseCase.toggleMultiple(emailIds, true)
            EmailAction.UNSTAR -> toggleStarUseCase.toggleMultiple(emailIds, false)
            EmailAction.RESTORE -> restoreEmailUseCase.restoreMultiple(emailIds)
            EmailAction.MARK_READ -> markAsReadUseCase.markMultiple(emailIds, true)
            EmailAction.MARK_UNREAD -> markAsReadUseCase.markMultiple(emailIds, false)
        }
    }
}
