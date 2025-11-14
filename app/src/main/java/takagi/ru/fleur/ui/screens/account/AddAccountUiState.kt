package takagi.ru.fleur.ui.screens.account

import androidx.compose.ui.graphics.Color
import takagi.ru.fleur.domain.model.FleurError

/**
 * 添加账户界面 UI 状态
 * @property email 邮箱地址
 * @property password 密码
 * @property displayName 显示名称
 * @property selectedColor 选中的颜色
 * @property isValidating 是否正在验证
 * @property validationSuccess 验证是否成功（null 表示未验证）
 * @property isSaving 是否正在保存
 * @property error 错误信息
 * @property emailError 邮箱格式错误
 * @property passwordVisible 密码是否可见
 */
data class AddAccountUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val selectedColor: Color = Color(0xFF6200EE),
    val isValidating: Boolean = false,
    val validationSuccess: Boolean? = null,
    val isSaving: Boolean = false,
    val error: FleurError? = null,
    val emailError: String? = null,
    val passwordVisible: Boolean = false
) {
    /**
     * 表单是否有效
     */
    fun isFormValid(): Boolean {
        return email.isNotBlank() &&
                password.isNotBlank() &&
                displayName.isNotBlank() &&
                emailError == null
    }
    
    /**
     * 是否可以保存
     */
    fun canSave(): Boolean {
        return isFormValid() && validationSuccess == true && !isSaving
    }
}
