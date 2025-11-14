package takagi.ru.fleur.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.data.local.dao.AccountDao
import takagi.ru.fleur.data.local.dao.EmailDao
import takagi.ru.fleur.data.local.mapper.EntityMapper.toDomain
import takagi.ru.fleur.data.local.mapper.EntityMapper.toEntity
import takagi.ru.fleur.data.remote.webdav.WebDAVClient
import takagi.ru.fleur.data.security.SecureCredentialStorage
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.FleurError
import takagi.ru.fleur.domain.model.WebDAVConfig
import takagi.ru.fleur.domain.repository.AccountRepository
import javax.inject.Inject

/**
 * 账户仓库实现
 * 管理账户信息和凭证
 */
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val emailDao: EmailDao,
    private val credentialStorage: SecureCredentialStorage,
    private val webdavClient: WebDAVClient
) : AccountRepository {
    
    /**
     * 获取所有账户
     */
    override fun getAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts()
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    /**
     * 根据ID获取账户
     */
    override fun getAccountById(accountId: String): Flow<Account?> {
        return accountDao.getAccountById(accountId)
            .map { entity -> entity?.toDomain() }
    }
    
    /**
     * 获取默认账户
     */
    override fun getDefaultAccount(): Flow<Account?> {
        return accountDao.getDefaultAccount()
            .map { entity -> entity?.toDomain() }
    }
    
    /**
     * 添加账户
     */
    override suspend fun addAccount(account: Account, password: String): Result<Unit> {
        return try {
            // 验证账户配置（使用新的 IMAP/SMTP 验证）
            val verifyResult = verifyEmailAccount(account.imapConfig, account.smtpConfig, password)
            if (verifyResult.isFailure) {
                return Result.failure(
                    FleurError.AuthenticationError("账户验证失败: ${verifyResult.exceptionOrNull()?.message}")
                )
            }
            
            // 如果是第一个账户，设置为默认账户
            val accountCount = accountDao.getAccountCount()
            val accountToSave = if (accountCount == 0) {
                account.copy(isDefault = true)
            } else {
                account
            }
            
            // 保存账户到数据库
            accountDao.insertAccount(accountToSave.toEntity())
            
            // 保存密码到加密存储
            credentialStorage.savePassword(account.id, password)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FleurError.DatabaseError(e.message ?: "添加账户失败"))
        }
    }
    
    /**
     * 更新账户
     */
    override suspend fun updateAccount(account: Account): Result<Unit> {
        return try {
            accountDao.updateAccount(account.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FleurError.DatabaseError(e.message ?: "更新账户失败"))
        }
    }
    
    /**
     * 删除账户
     */
    override suspend fun deleteAccount(accountId: String): Result<Unit> {
        return try {
            // 删除账户相关的所有邮件
            emailDao.deleteEmailsByAccount(accountId)
            
            // 删除账户
            accountDao.deleteAccountById(accountId)
            
            // 删除密码
            credentialStorage.deletePassword(accountId)
            
            // 如果删除的是默认账户，设置第一个账户为默认
            val remainingAccounts = accountDao.getAccountCount()
            if (remainingAccounts > 0) {
                // 这里简化处理，实际应该检查是否需要设置新的默认账户
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FleurError.DatabaseError(e.message ?: "删除账户失败"))
        }
    }
    
    /**
     * 设置默认账户
     */
    override suspend fun setDefaultAccount(accountId: String): Result<Unit> {
        return try {
            // 清除所有账户的默认状态
            accountDao.clearDefaultAccounts()
            
            // 设置指定账户为默认
            accountDao.setDefaultAccount(accountId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FleurError.DatabaseError(e.message ?: "设置默认账户失败"))
        }
    }
    
    /**
     * 验证邮件账户配置
     * 测试 IMAP 和 SMTP 连接
     */
    override suspend fun verifyEmailAccount(
        imapConfig: takagi.ru.fleur.domain.model.ImapConfig,
        smtpConfig: takagi.ru.fleur.domain.model.SmtpConfig,
        password: String
    ): Result<Boolean> {
        return try {
            // TODO: 实现实际的 IMAP/SMTP 连接验证
            // 这里暂时返回成功，实际应该连接到邮件服务器进行验证
            
            // 示例验证逻辑：
            // 1. 连接 IMAP 服务器
            // 2. 连接 SMTP 服务器
            // 3. 如果两者都成功，返回 true
            
            // 暂时模拟验证成功
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(FleurError.NetworkError(e.message ?: "网络错误"))
        }
    }
    
    /**
     * 验证账户配置（已废弃）
     */
    override suspend fun verifyAccount(config: WebDAVConfig, password: String): Result<Boolean> {
        return try {
            val result = webdavClient.connect(config, password)
            
            if (result.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(
                    FleurError.AuthenticationError(
                        result.exceptionOrNull()?.message ?: "验证失败"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(FleurError.NetworkError(e.message ?: "网络错误"))
        }
    }
    
    /**
     * 获取账户密码
     */
    override suspend fun getAccountPassword(accountId: String): Result<String> {
        return try {
            val password = credentialStorage.getPassword(accountId)
            if (password != null) {
                Result.success(password)
            } else {
                Result.failure(FleurError.NotFoundError("密码不存在"))
            }
        } catch (e: Exception) {
            Result.failure(FleurError.SecurityError(e.message ?: "获取密码失败"))
        }
    }
    
    /**
     * 更新账户密码
     */
    override suspend fun updateAccountPassword(accountId: String, newPassword: String): Result<Unit> {
        return try {
            // 验证新密码是否有效
            val account = accountDao.getAccountById(accountId)
            // 注意：这里需要先获取 Flow 的值
            // 简化处理，实际应该使用 first() 或 collect
            
            credentialStorage.savePassword(accountId, newPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FleurError.SecurityError(e.message ?: "更新密码失败"))
        }
    }
}
