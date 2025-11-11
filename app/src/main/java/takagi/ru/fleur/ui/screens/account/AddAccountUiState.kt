package takagi.ru.fleur.ui.screens.account

import androidx.compose.ui.graphics.Color
import takagi.ru.fleur.domain.model.FleurError

/**
 * 添加账户界面状态
 * @property email 邮箱地址
 * @property password 密码
 * @property displayName 显示名称
 * @property webdavServer WebDAV 服务器地址
 * @property webdavPort WebDAV 端口
 * @property selectedColor 选中的颜色
 * @property isValidating 是否正在验证
 * @property validationSuccess 验证是否成功
 * @property isSaving 是否正在保存
 * @property error 错误信息
 */
data class AddAccountUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val webdavServer: String = "",
    val webdavPort: String = "443",
    val selectedColor: Color = Color(0xFF1976D2),
    val isValidating: Boolean = false,
    val validationSuccess: Boolean? = null,
    val isSaving: Boolean = false,
    val error: FleurError? = null
) {
    /**
     * 验证表单是否完整
     */
    fun isFormValid(): Boolean {
        return email.isNotBlank() &&
                password.isNotBlank() &&
                displayName.isNotBlank() &&
                webdavServer.isNotBlank() &&
                webdavPort.isNotBlank() &&
                isValidEmail(email) &&
                isValidPort(webdavPort)
    }
    
    /**
     * 验证邮箱格式
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }
    
    /**
     * 验证端口号
     */
    private fun isValidPort(port: String): Boolean {
        val portNumber = port.toIntOrNull() ?: return false
        return portNumber in 1..65535
    }
    
    /**
     * 是否可以保存
     */
    fun canSave(): Boolean {
        return isFormValid() && validationSuccess == true && !isSaving
    }
}
