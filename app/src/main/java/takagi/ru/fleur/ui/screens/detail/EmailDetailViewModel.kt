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
     * 回复邮件
     */
    fun reply() {
        // TODO: 导航到撰写页面，预填充回复内容
    }
    
    /**
     * 全部回复
     */
    fun replyAll() {
        // TODO: 导航到撰写页面，预填充全部回复内容
    }
    
    /**
     * 转发邮件
     */
    fun forward() {
        // TODO: 导航到撰写页面，预填充转发内容
    }
    
    /**
     * 删除邮件
     */
    fun delete(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = deleteEmailUseCase(emailId)
            result.fold(
                onSuccess = {
                    onSuccess()
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
     * 归档邮件
     */
    fun archive(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = archiveEmailUseCase(emailId)
            result.fold(
                onSuccess = {
                    onSuccess()
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
     * 切换星标状态
     */
    fun toggleStar() {
        viewModelScope.launch {
            val currentEmail = _uiState.value.email ?: return@launch
            val newStarredState = !currentEmail.isStarred
            
            val result = emailRepository.toggleStar(emailId, newStarredState)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            email = currentEmail.copy(isStarred = newStarredState),
                            isStarred = newStarredState
                        )
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
