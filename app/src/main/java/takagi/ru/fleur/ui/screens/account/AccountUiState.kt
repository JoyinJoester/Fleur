package takagi.ru.fleur.ui.screens.account

import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.FleurError

/**
 * 账户管理界面状态
 * @property accounts 账户列表
 * @property isLoading 是否正在加载
 * @property error 错误信息
 * @property showDeleteDialog 是否显示删除确认对话框
 * @property accountToDelete 待删除的账户
 */
data class AccountUiState(
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = false,
    val error: FleurError? = null,
    val showDeleteDialog: Boolean = false,
    val accountToDelete: Account? = null
) {
    /**
     * 获取默认账户
     */
    fun getDefaultAccount(): Account? {
        return accounts.firstOrNull { it.isDefault }
    }
    
    /**
     * 是否有账户
     */
    fun hasAccounts(): Boolean = accounts.isNotEmpty()
}
