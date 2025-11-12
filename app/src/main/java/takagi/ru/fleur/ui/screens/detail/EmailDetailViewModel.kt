package takagi.ru.fleur.ui.screens.detail

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
import takagi.ru.fleur.domain.model.ComposeMode
import takagi.ru.fleur.domain.model.FleurError
import takagi.ru.fleur.domain.usecase.ArchiveEmailUseCase
import takagi.ru.fleur.domain.usecase.DeleteEmailUseCase
import javax.inject.Inject

/**
 * 邮件详情 ViewModel
 * 管理邮件详情页面的状态和业务逻辑
 */
@HiltViewModel
class EmailDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEmailByIdUseCase: takagi.ru.fleur.domain.usecase.GetEmailsUseCase,
    private val deleteEmailUseCase: DeleteEmailUseCase,
    private val archiveEmailUseCase: ArchiveEmailUseCase,
    private val markAsReadUseCase: takagi.ru.fleur.domain.usecase.MarkAsReadUseCase,
    private val emailRepository: takagi.ru.fleur.domain.repository.EmailRepository
) : ViewModel() {
    
    private val emailId: String = checkNotNull(savedStateHandle["emailId"])
    
    private val _uiState = MutableStateFlow(EmailDetailUiState())
    val uiState: StateFlow<EmailDetailUiState> = _uiState.asStateFlow()
    
    /**
     * 导航到撰写页面的回调
     * 参数：emailId (引用邮件ID), mode (撰写模式)
     */
    private var onNavigateToCompose: ((String, ComposeMode) -> Unit)? = null
    
    init {
        loadEmail()
    }
    
    /**
     * 加载邮件详情
     */
    fun loadEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            emailRepository.getEmailById(emailId)
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
                        onSuccess = { email ->
                            _uiState.update {
                                it.copy(
                                    email = email,
                                    isLoading = false,
                                    isStarred = email.isStarred
                                )
                            }
                            
                            // 标记为已读
                            if (!email.isRead) {
                                markAsRead()
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
     * 设置导航回调
     * 
     * @param callback 导航回调函数，参数为 (emailId, mode)
     */
    fun setNavigationCallback(callback: (String, ComposeMode) -> Unit) {
        onNavigateToCompose = callback
    }
    
    /**
     * 回复邮件
     * 
     * 触发导航到撰写页面，使用回复模式
     */
    fun reply() {
        val email = _uiState.value.email
        if (email != null) {
            onNavigateToCompose?.invoke(email.id, ComposeMode.REPLY)
        } else {
            android.util.Log.w("EmailDetailViewModel", "无法回复：邮件为空")
        }
    }
    
    /**
     * 全部回复
     * 
     * 触发导航到撰写页面，使用全部回复模式
     */
    fun replyAll() {
        val email = _uiState.value.email
        if (email != null) {
            onNavigateToCompose?.invoke(email.id, ComposeMode.REPLY_ALL)
        } else {
            android.util.Log.w("EmailDetailViewModel", "无法全部回复：邮件为空")
        }
    }
    
    /**
     * 转发邮件
     * 
     * 触发导航到撰写页面，使用转发模式
     */
    fun forward() {
        val email = _uiState.value.email
        if (email != null) {
            onNavigateToCompose?.invoke(email.id, ComposeMode.FORWARD)
        } else {
            android.util.Log.w("EmailDetailViewModel", "无法转发：邮件为空")
        }
    }
    
    /**
     * 删除邮件（本地优先）
     * 使用乐观UI更新策略：立即执行回调，不等待操作完成
     */
    fun delete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // 立即执行成功回调（本地优先：操作会立即在本地生效）
            onSuccess()
            
            // 异步执行删除操作
            val result = deleteEmailUseCase(emailId)
            result.fold(
                onSuccess = {
                    android.util.Log.d("EmailDetailViewModel", "删除邮件成功: emailId=$emailId")
                },
                onFailure = { error ->
                    android.util.Log.e("EmailDetailViewModel", "删除邮件失败: emailId=$emailId, error=${error.message}")
                    // 本地优先架构：即使远程同步失败，本地操作已完成
                }
            )
        }
    }
    
    /**
     * 归档邮件（本地优先）
     * 使用乐观UI更新策略：立即执行回调，不等待操作完成
     */
    fun archive(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // 立即执行成功回调（本地优先：操作会立即在本地生效）
            onSuccess()
            
            // 异步执行归档操作
            val result = archiveEmailUseCase(emailId)
            result.fold(
                onSuccess = {
                    android.util.Log.d("EmailDetailViewModel", "归档邮件成功: emailId=$emailId")
                },
                onFailure = { error ->
                    android.util.Log.e("EmailDetailViewModel", "归档邮件失败: emailId=$emailId, error=${error.message}")
                    // 本地优先架构：即使远程同步失败，本地操作已完成
                }
            )
        }
    }
    
    /**
     * 切换星标状态（本地优先）
     * 使用乐观UI更新策略：立即更新UI状态
     */
    fun toggleStar() {
        viewModelScope.launch {
            val currentEmail = _uiState.value.email ?: return@launch
            val newStarredState = !currentEmail.isStarred
            
            // 立即更新 UI 状态
            _uiState.update {
                it.copy(
                    email = currentEmail.copy(isStarred = newStarredState),
                    isStarred = newStarredState
                )
            }
            
            // 异步执行星标操作
            val result = emailRepository.toggleStar(emailId, newStarredState)
            result.fold(
                onSuccess = {
                    android.util.Log.d("EmailDetailViewModel", "切换星标成功: emailId=$emailId, isStarred=$newStarredState")
                },
                onFailure = { error ->
                    android.util.Log.e("EmailDetailViewModel", "切换星标失败: emailId=$emailId, error=${error.message}")
                    // 本地优先架构：即使远程同步失败，本地操作已完成，不需要回滚UI
                }
            )
        }
    }
    
    /**
     * 标记为已读
     */
    private fun markAsRead() {
        viewModelScope.launch {
            markAsReadUseCase(emailId, true)
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
