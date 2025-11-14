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
import takagi.ru.fleur.domain.service.EmailServerProvider
import takagi.ru.fleur.domain.usecase.ManageAccountUseCase
import takagi.ru.fleur.util.EmailValidator
import javax.inject.Inject

/**
 * 添加账户 ViewModel
 * 管理添加账户界面的状态和业务逻辑
 * 集成邮件服务器自动检测和账户验证功能
 */
@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val manageAccountUseCase: ManageAccountUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()
    
    /**
     * 更新邮箱地址
     * 自动验证邮箱格式
     */
    fun updateEmail(email: String) {
        val emailError = if (email.isNotBlank() && !EmailValidator.isValidEmail(email)) {
            "邮箱格式不正确"
        } else {
            null
        }
        
        _uiState.update { 
            it.copy(
                email = email, 
                emailError = emailError,
                validationSuccess = null
            ) 
        }
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
     * 切换密码可见性
     */
    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }
    
    /**
     * 选择颜色
     */
    fun selectColor(color: Color) {
        _uiState.update { it.copy(selectedColor = color) }
    }
    
    /**
     * 验证账户
     * 自动检测邮件服务器配置并验证 IMAP/SMTP 连接
     */
    fun validateAccount() {
        val currentState = _uiState.value
        
        // 验证表单
        if (!currentState.isFormValid()) {
            _uiState.update {
                it.copy(
                    error = FleurError.ValidationError(
                        field = "表单",
                        errorMessage = "请填写所有必填字段并确保邮箱格式正确"
                    )
                )
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isValidating = true, error = null) }
            
            // 自动检测服务器配置
            val serverConfig = EmailServerProvider.detectServerConfig(currentState.email)
            
            if (serverConfig == null) {
                _uiState.update {
                    it.copy(
                        isValidating = false,
                        validationSuccess = false,
                        error = FleurError.ValidationError(
                            field = "邮箱",
                            errorMessage = "暂不支持该邮箱服务商，请手动配置服务器"
                        )
                    )
                }
                return@launch
            }
            
            // 验证 IMAP 和 SMTP 连接
            // TODO: 实现实际的 IMAP/SMTP 连接验证
            // 这里暂时模拟验证成功
            val result = manageAccountUseCase.verifyEmailAccount(
                imapConfig = serverConfig.imapConfig,
                smtpConfig = serverConfig.smtpConfig,
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
                                ?: FleurError.UnknownError(error.message ?: "验证失败，请检查邮箱和密码")
                        )
                    }
                }
            )
        }
    }
    
    /**
     * 保存账户
     * 使用自动检测的服务器配置保存账户
     */
    fun saveAccount(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        
        if (!currentState.canSave()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            // 获取服务器配置
            val serverConfig = EmailServerProvider.detectServerConfig(currentState.email)
            
            if (serverConfig == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = FleurError.ValidationError(
                            field = "邮箱",
                            errorMessage = "无法获取服务器配置"
                        )
                    )
                }
                return@launch
            }
            
            // 创建账户
            val account = Account(
                id = java.util.UUID.randomUUID().toString(),
                email = currentState.email,
                displayName = currentState.displayName,
                color = currentState.selectedColor,
                imapConfig = serverConfig.imapConfig,
                smtpConfig = serverConfig.smtpConfig,
                createdAt = System.currentTimeMillis()
            )
            
            // 保存账户
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
