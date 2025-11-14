package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.WebDAVConfig
import takagi.ru.fleur.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * 管理账户用例
 * 提供账户的增删改查操作
 */
class ManageAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    /**
     * 获取所有账户
     */
    fun getAccounts(): Flow<List<Account>> {
        return accountRepository.getAccounts()
    }
    
    /**
     * 根据ID获取账户
     */
    fun getAccountById(accountId: String): Flow<Account?> {
        return accountRepository.getAccountById(accountId)
    }
    
    /**
     * 获取默认账户
     */
    fun getDefaultAccount(): Flow<Account?> {
        return accountRepository.getDefaultAccount()
    }
    
    /**
     * 添加账户
     */
    suspend fun addAccount(account: Account, password: String): Result<Unit> {
        return accountRepository.addAccount(account, password)
    }
    
    /**
     * 更新账户
     */
    suspend fun updateAccount(account: Account): Result<Unit> {
        return accountRepository.updateAccount(account)
    }
    
    /**
     * 删除账户
     */
    suspend fun deleteAccount(accountId: String): Result<Unit> {
        return accountRepository.deleteAccount(accountId)
    }
    
    /**
     * 设置默认账户
     */
    suspend fun setDefaultAccount(accountId: String): Result<Unit> {
        return accountRepository.setDefaultAccount(accountId)
    }
    
    /**
     * 验证邮件账户配置
     * 验证 IMAP 和 SMTP 服务器连接
     */
    suspend fun verifyEmailAccount(
        imapConfig: takagi.ru.fleur.domain.model.ImapConfig,
        smtpConfig: takagi.ru.fleur.domain.model.SmtpConfig,
        password: String
    ): Result<Boolean> {
        return accountRepository.verifyEmailAccount(imapConfig, smtpConfig, password)
    }
    
    /**
     * 验证账户配置（已废弃，保留用于向后兼容）
     */
    @Deprecated("使用 verifyEmailAccount 代替")
    suspend fun verifyAccount(config: WebDAVConfig, password: String): Result<Boolean> {
        return accountRepository.verifyAccount(config, password)
    }
    
    /**
     * 更新账户密码
     */
    suspend fun updateAccountPassword(accountId: String, newPassword: String): Result<Unit> {
        return accountRepository.updateAccountPassword(accountId, newPassword)
    }
}
