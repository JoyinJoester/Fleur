package takagi.ru.fleur.ui.screens.inbox

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
import takagi.ru.fleur.domain.usecase.GetEmailsUseCase
import takagi.ru.fleur.domain.usecase.SyncEmailsUseCase
import takagi.ru.fleur.util.TestDataGenerator
import javax.inject.Inject

/**
 * 收件箱 ViewModel
 * 管理收件箱界面的状态和业务逻辑
 */
@HiltViewModel
class InboxViewModel @Inject constructor(
    private val getEmailsUseCase: GetEmailsUseCase,
    private val syncEmailsUseCase: SyncEmailsUseCase,
    private val archiveEmailUseCase: takagi.ru.fleur.domain.usecase.ArchiveEmailUseCase,
    private val deleteEmailUseCase: takagi.ru.fleur.domain.usecase.DeleteEmailUseCase,
    private val markAsReadUseCase: takagi.ru.fleur.domain.usecase.MarkAsReadUseCase,
    private val toggleStarUseCase: takagi.ru.fleur.domain.usecase.ToggleStarUseCase,
    private val syncStatusRepository: takagi.ru.fleur.domain.repository.SyncStatusRepository,
    private val networkMonitor: takagi.ru.fleur.util.NetworkMonitor,
    private val offlineOperationManager: takagi.ru.fleur.data.offline.OfflineOperationManager,
    private val preferencesRepository: takagi.ru.fleur.domain.repository.PreferencesRepository,
    private val emailDao: takagi.ru.fleur.data.local.dao.EmailDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val PAGE_SIZE = 50
    }
    
    init {
        loadEmails()
        observeBackgroundSync()
        observeNetworkStatus()
        observePendingOperations()
        observeUserPreferences()
    }
    
    /**
     * 观察用户偏好设置
     */
    private fun observeUserPreferences() {
        viewModelScope.launch {
            preferencesRepository.getUserPreferences()
                .collect { preferences ->
                    _uiState.update { 
                        it.copy(
                            swipeRightAction = preferences.swipeRightAction,
                            swipeLeftAction = preferences.swipeLeftAction
                        )
                    }
                }
        }
    }
    
    /**
     * 观察后台同步状态
     */
    private fun observeBackgroundSync() {
        viewModelScope.launch {
            syncStatusRepository.observeSyncStatus()
                .collect { isSyncing ->
                    _uiState.update { it.copy(isSyncing = isSyncing) }
                    
                    // 如果后台同步完成，静默刷新邮件列表
                    if (!isSyncing && _uiState.value.emails.isNotEmpty()) {
                        loadEmails(reset = true)
                    }
                }
        }
    }
    
    /**
     * 观察网络状态
     */
    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.observeNetworkStatus()
                .collect { isConnected ->
                    _uiState.update { it.copy(isOffline = !isConnected) }
                }
        }
    }
    
    /**
     * 观察待处理操作数量
     */
    private fun observePendingOperations() {
        viewModelScope.launch {
            while (true) {
                val count = offlineOperationManager.getPendingOperationCount()
                _uiState.update { it.copy(pendingOperationCount = count) }
                kotlinx.coroutines.delay(5000) // 每 5 秒更新一次
            }
        }
    }
    
    /**
     * 加载邮件列表
     */
    fun loadEmails(reset: Boolean = false) {
        viewModelScope.launch {
            if (reset) {
                _uiState.update { it.copy(currentPage = 0, emails = emptyList()) }
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val currentState = _uiState.value
            getEmailsUseCase(
                accountId = currentState.selectedAccountId,
                page = currentState.currentPage,
                pageSize = PAGE_SIZE
            )
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = FleurError.UnknownError(e.message ?: "加载失败", e)
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { newEmails ->
                            _uiState.update { state ->
                                val updatedEmails = if (reset) {
                                    newEmails
                                } else {
                                    state.emails + newEmails
                                }
                                state.copy(
                                    emails = updatedEmails,
                                    isLoading = false,
                                    hasMorePages = newEmails.size >= PAGE_SIZE
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = error as? FleurError
                                        ?: FleurError.UnknownError(error.message ?: "加载失败")
                                )
                            }
                        }
                    )
                }
        }
    }
    
    /**
     * 刷新邮件列表
     * 同步远程邮件并重新加载
     */
    fun refreshEmails() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isRefreshing = true, 
                    isSyncing = true,
                    error = null,
                    syncError = null
                ) 
            }
            
            // 使用指数退避重试策略同步邮件
            val syncResult = retryWithExponentialBackoff(
                maxRetries = 3,
                initialDelay = 1000L,
                maxDelay = 10000L,
                factor = 2.0
            ) {
                syncEmailsUseCase()
            }
            
            syncResult.fold(
                onSuccess = { results ->
                    // 同步成功后重新加载邮件
                    _uiState.update { 
                        it.copy(
                            isSyncing = false,
                            lastSyncResult = results
                        ) 
                    }
                    loadEmails(reset = true)
                    _uiState.update { it.copy(isRefreshing = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            isSyncing = false,
                            syncError = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "同步失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 手动触发同步（不刷新列表）
     */
    fun syncEmails() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isSyncing = true,
                    syncError = null
                ) 
            }
            
            // 使用指数退避重试策略同步邮件
            val syncResult = retryWithExponentialBackoff(
                maxRetries = 3,
                initialDelay = 1000L,
                maxDelay = 10000L,
                factor = 2.0
            ) {
                syncEmailsUseCase()
            }
            
            syncResult.fold(
                onSuccess = { results ->
                    _uiState.update { 
                        it.copy(
                            isSyncing = false,
                            lastSyncResult = results
                        ) 
                    }
                    // 静默刷新邮件列表
                    loadEmails(reset = true)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            syncError = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "同步失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 使用指数退避策略重试
     */
    private suspend fun <T> retryWithExponentialBackoff(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 10000,
        factor: Double = 2.0,
        block: suspend () -> Result<T>
    ): Result<T> {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            val result = block()
            if (result.isSuccess) {
                return result
            }
            
            // 如果不是最后一次尝试，等待后重试
            if (attempt < maxRetries - 1) {
                kotlinx.coroutines.delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
        
        // 所有重试都失败，返回最后一次的结果
        return block()
    }
    
    /**
     * 加载下一页
     */
    fun loadNextPage() {
        val currentState = _uiState.value
        if (!currentState.isLoading && currentState.hasMorePages) {
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
            loadEmails()
        }
    }
    
    /**
     * 切换视图模式
     */
    fun switchViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }
    
    /**
     * 按账户过滤
     */
    fun filterByAccount(accountId: String?) {
        _uiState.update {
            it.copy(
                selectedAccountId = accountId,
                currentPage = 0,
                emails = emptyList()
            )
        }
        loadEmails()
    }
    
    /**
     * 归档邮件
     */
    fun archiveEmail(emailId: String) {
        viewModelScope.launch {
            val result = archiveEmailUseCase(emailId)
            result.fold(
                onSuccess = {
                    // 从列表中移除已归档的邮件
                    _uiState.update { state ->
                        state.copy(emails = state.emails.filter { it.id != emailId })
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "归档失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 删除邮件
     */
    fun deleteEmail(emailId: String) {
        viewModelScope.launch {
            val result = deleteEmailUseCase(emailId)
            result.fold(
                onSuccess = {
                    // 从列表中移除已删除的邮件
                    _uiState.update { state ->
                        state.copy(emails = state.emails.filter { it.id != emailId })
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "删除失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 切换星标
     */
    fun toggleStar(emailId: String) {
        viewModelScope.launch {
            val email = _uiState.value.emails.find { it.id == emailId }
            if (email != null) {
                val result = toggleStarUseCase(emailId, !email.isStarred)
                result.fold(
                    onSuccess = {
                        // 更新列表中的邮件状态
                        _uiState.update { state ->
                            state.copy(
                                emails = state.emails.map {
                                    if (it.id == emailId) it.copy(isStarred = !it.isStarred) else it
                                }
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                error = error as? FleurError
                                    ?: FleurError.UnknownError(error.message ?: "星标操作失败")
                            )
                        }
                    }
                )
            }
        }
    }
    
    /**
     * 标记为已读
     */
    fun markAsRead(emailId: String) {
        viewModelScope.launch {
            val result = markAsReadUseCase(emailId, true)
            result.fold(
                onSuccess = {
                    // 更新列表中的邮件状态
                    _uiState.update { state ->
                        state.copy(
                            emails = state.emails.map {
                                if (it.id == emailId) it.copy(isRead = true) else it
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "标记已读失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 标记为未读
     */
    fun markAsUnread(emailId: String) {
        viewModelScope.launch {
            val result = markAsReadUseCase(emailId, false)
            result.fold(
                onSuccess = {
                    // 更新列表中的邮件状态
                    _uiState.update { state ->
                        state.copy(
                            emails = state.emails.map {
                                if (it.id == emailId) it.copy(isRead = false) else it
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "标记未读失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 进入多选模式
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
     */
    fun toggleEmailSelection(emailId: String) {
        _uiState.update { state ->
            val newSelection = if (emailId in state.selectedEmailIds) {
                state.selectedEmailIds - emailId
            } else {
                state.selectedEmailIds + emailId
            }
            
            // 如果没有选中的邮件，退出多选模式
            if (newSelection.isEmpty()) {
                state.copy(
                    isMultiSelectMode = false,
                    selectedEmailIds = emptySet()
                )
            } else {
                state.copy(selectedEmailIds = newSelection)
            }
        }
    }
    
    /**
     * 批量删除选中的邮件
     */
    fun deleteSelectedEmails() {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedEmailIds.toList()
            val result = deleteEmailUseCase.deleteMultiple(selectedIds)
            
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            emails = state.emails.filter { it.id !in selectedIds },
                            isMultiSelectMode = false,
                            selectedEmailIds = emptySet()
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "批量删除失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 批量归档选中的邮件
     */
    fun archiveSelectedEmails() {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedEmailIds.toList()
            val result = archiveEmailUseCase.archiveMultiple(selectedIds)
            
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            emails = state.emails.filter { it.id !in selectedIds },
                            isMultiSelectMode = false,
                            selectedEmailIds = emptySet()
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "批量归档失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 批量标记为已读/未读
     */
    fun markSelectedAsRead(isRead: Boolean) {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedEmailIds.toList()
            val result = markAsReadUseCase.markMultiple(selectedIds, isRead)
            
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            emails = state.emails.map { email ->
                                if (email.id in selectedIds) {
                                    email.copy(isRead = isRead)
                                } else {
                                    email
                                }
                            },
                            isMultiSelectMode = false,
                            selectedEmailIds = emptySet()
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "批量标记失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 插入测试数据（仅 DEBUG 模式）
     * 用于快速生成测试邮件
     */
    fun insertTestData(accountId: String) {
        viewModelScope.launch {
            try {
                // 生成并插入测试邮件
                val testEmails = TestDataGenerator.generateTestEmails(accountId, 20)
                emailDao.insertEmails(testEmails)
                
                // 重新加载邮件列表
                loadEmails(reset = true)
            } catch (e: Exception) {
                _uiState.update { currentState -> 
                    currentState.copy(error = FleurError.UnknownError(e.message ?: "Unknown error", e))
                }
            }
        }
    }
}
