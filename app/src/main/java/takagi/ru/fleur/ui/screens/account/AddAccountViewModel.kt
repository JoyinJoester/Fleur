package takagi.ru.fleur.ui.screens.account

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.FleurError
import takagi.ru.fleur.domain.model.WebDAVConfig
import takagi.ru.fleur.domain.usecase.ManageAccountUseCase
import javax.inject.Inject

/**
 * 添加账户 ViewModel
 * 管理添加账户界面的状态和业务逻辑
 */
@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val manageAccountUseCase: ManageAccountUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()
    
    /**
     * 更新邮箱地址
     */
    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, validationSuccess = null) }
    }
    
    /**
     * 更新密码
     */
    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, validationSuccess = null) }
    }
    
    /**
     * 更新显示名称
     */
    fun updateDisplayName(displayName: String) {
        _uiState.update { it.copy(displayName = displayName) }
    }
    
    /**
     * 更新 WebDAV 服务器
     */
    fun updateWebdavServer(server: String) {
        _uiState.update { it.copy(webdavServer = server, validationSuccess = null) }
    }
    
    /**
     * 更新 WebDAV 端口
     */
    fun updateWebdavPort(port: String) {
        _uiState.update { it.copy(webdavPort = port, validationSuccess = null) }
    }
    
    /**
     * 选择颜色
     */
    fun selectColor(color: Color) {
        _uiState.update { it.copy(selectedColor = color) }
    }
    
    /**
     * 验证账户
     */
    fun validateAccount() {
        val currentState = _uiState.value
        
        if (!currentState.isFormValid()) {
            _uiState.update {
                it.copy(
                    error = FleurError.ValidationError(
                        field = "表单",
                        errorMessage = "请填写所有必填字段"
                    )
                )
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isValidating = true, error = null) }
            
            val webdavConfig = WebDAVConfig(
                serverUrl = currentState.webdavServer,
                port = currentState.webdavPort.toInt(),
                username = currentState.email,
                useSsl = true
            )
            
            val result = manageAccountUseCase.verifyAccount(
                config = webdavConfig,
                password = currentState.password
            )
            
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isValidating = false,
                            validationSuccess = true
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isValidating = false,
                            validationSuccess = false,
                            error = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "验证失败")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 保存账户
     */
    fun saveAccount(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        
        if (!currentState.canSave()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            val webdavConfig = WebDAVConfig(
                serverUrl = currentState.webdavServer,
                port = currentState.webdavPort.toInt(),
                username = currentState.email,
                useSsl = true
            )
            
            val account = Account(
                id = java.util.UUID.randomUUID().toString(),
                email = currentState.email,
                displayName = currentState.displayName,
                color = currentState.selectedColor,
                webdavConfig = webdavConfig
            )
            
            val result = manageAccountUseCase.addAccount(
                account = account,
                password = currentState.password
            )
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error as? FleurError
                                ?: FleurError.UnknownError(error.message ?: "保存账户失败")
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
