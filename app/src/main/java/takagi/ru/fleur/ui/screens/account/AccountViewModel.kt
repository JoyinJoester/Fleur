package takagi.ru.fleur.ui.screens.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.FleurError
import takagi.ru.fleur.domain.usecase.ManageAccountUseCase
import javax.inject.Inject

/**
 * 账户管理 ViewModel
 * 管理账户管理界面的状态和业务逻辑
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val manageAccountUseCase: ManageAccountUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AccountManagementUiState())
    val uiState: StateFlow<AccountManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadAccounts()
    }
    
    /**
     * 加载账户列表
     */
    fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            manageAccountUseCase.getAccounts()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = FleurError.UnknownError(e.message ?: "加载账户失败", e)
                        )
                    }
                }
                .collect { accounts ->
                    _uiState.update {
                        it.copy(
                            accounts = accounts,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    /**
     * 设置默认账户
     */
    fun setDefaultAccount(accountId: String) {
        viewModelScope.launch {
            val result = manageAccountUseCase.setDefaultAccount(accountId)
            
            result.fold(
                onSuccess = {
                    // 重新加载账户列表
                    loadAccounts()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "设置默认账户失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 显示添加账户底部表单
     */
    fun showAddAccountSheet() {
        _uiState.update { it.copy(showAddAccountSheet = true) }
    }
    
    /**
     * 隐藏添加账户底部表单
     */
    fun hideAddAccountSheet() {
        _uiState.update { it.copy(showAddAccountSheet = false) }
    }
    
    /**
     * 显示删除确认对话框
     */
    fun showDeleteDialog(account: Account) {
        _uiState.update {
            it.copy(
                showDeleteDialog = true,
                accountToDelete = account
            )
        }
    }
    
    /**
     * 隐藏删除确认对话框
     */
    fun hideDeleteDialog() {
        _uiState.update {
            it.copy(
                showDeleteDialog = false,
                accountToDelete = null
            )
        }
    }
    
    /**
     * 删除账户
     */
    fun deleteAccount() {
        val account = _uiState.value.accountToDelete ?: return
        
        viewModelScope.launch {
            hideDeleteDialog()
            
            // 标记账户为正在删除状态，触发退出动画
            _uiState.update {
                it.copy(deletingAccountIds = it.deletingAccountIds + account.id)
            }
            
            // 等待动画完成（200ms）
            kotlinx.coroutines.delay(200)
            
            val result = manageAccountUseCase.deleteAccount(account.id)
            
            result.fold(
                onSuccess = {
                    // 重新加载账户列表
                    loadAccounts()
                    // 清除删除状态
                    _uiState.update {
                        it.copy(deletingAccountIds = it.deletingAccountIds - account.id)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            deletingAccountIds = it.deletingAccountIds - account.id,
                            error = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "删除账户失败")
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
}
