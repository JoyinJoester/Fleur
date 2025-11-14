package takagi.ru.fleur.domain.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.WebDAVConfig

/**
 * 账户仓库接口
 * 定义账户相关的数据操作
 */
interface AccountRepository {
    
    /**
     * 获取所有账户
     * @return Flow<List<Account>> 账户列表流
     */
    fun getAccounts(): Flow<List<Account>>
    
    /**
     * 根据ID获取账户
     * @param accountId 账户ID
     * @return Flow<Account?> 账户流，不存在时为null
     */
    fun getAccountById(accountId: String): Flow<Account?>
    
    /**
     * 获取默认账户
     * @return Flow<Account?> 默认账户流，不存在时为null
     */
    fun getDefaultAccount(): Flow<Account?>
    
    /**
     * 添加账户
     * @param account 账户信息
     * @param password 密码（将被加密存储）
     * @return Result<Unit> 添加结果
     */
    suspend fun addAccount(account: Account, password: String): Result<Unit>
    
    /**
     * 更新账户
     * @param account 账户信息
     * @return Result<Unit> 更新结果
     */
    suspend fun updateAccount(account: Account): Result<Unit>
    
    /**
     * 删除账户
     * @param accountId 账户ID
     * @return Result<Unit> 删除结果
     */
    suspend fun deleteAccount(accountId: String): Result<Unit>
    
    /**
     * 设置默认账户
     * @param accountId 账户ID
     * @return Result<Unit> 设置结果
     */
    suspend fun setDefaultAccount(accountId: String): Result<Unit>
    
    /**
     * 验证邮件账户配置
     * 测试 IMAP 和 SMTP 连接是否有效
     * @param imapConfig IMAP 配置
     * @param smtpConfig SMTP 配置
     * @param password 密码
     * @return Result<Boolean> 验证结果，true表示验证成功
     */
    suspend fun verifyEmailAccount(
        imapConfig: takagi.ru.fleur.domain.model.ImapConfig,
        smtpConfig: takagi.ru.fleur.domain.model.SmtpConfig,
        password: String
    ): Result<Boolean>
    
    /**
     * 验证账户配置（已废弃）
     * 测试WebDAV连接是否有效
     * @param config WebDAV配置
     * @param password 密码
     * @return Result<Boolean> 验证结果，true表示验证成功
     */
    @Deprecated("使用 verifyEmailAccount 代替")
    suspend fun verifyAccount(config: WebDAVConfig, password: String): Result<Boolean>
    
    /**
     * 获取账户密码
     * @param accountId 账户ID
     * @return Result<String> 密码（解密后）
     */
    suspend fun getAccountPassword(accountId: String): Result<String>
    
    /**
     * 更新账户密码
     * @param accountId 账户ID
     * @param newPassword 新密码
     * @return Result<Unit> 更新结果
     */
    suspend fun updateAccountPassword(accountId: String, newPassword: String): Result<Unit>
}
