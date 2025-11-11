package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.first
import takagi.ru.fleur.domain.model.SyncResult
import takagi.ru.fleur.domain.repository.AccountRepository
import takagi.ru.fleur.domain.repository.EmailRepository
import javax.inject.Inject

/**
 * 同步邮件用例
 * 同步所有账户的邮件
 */
class SyncEmailsUseCase @Inject constructor(
    private val emailRepository: EmailRepository,
    private val accountRepository: AccountRepository
) {
    /**
     * 执行用例
     * 同步所有账户的邮件
     */
    suspend operator fun invoke(): Result<List<SyncResult>> {
        return try {
            val accounts = accountRepository.getAccounts().first()
            val results = mutableListOf<SyncResult>()
            
            accounts.forEach { account ->
                val result = emailRepository.syncEmails(account.id)
                if (result.isSuccess) {
                    results.add(result.getOrThrow())
                } else {
                    // 记录失败但继续同步其他账户
                    results.add(
                        SyncResult(
                            accountId = account.id,
                            success = false,
                            error = result.exceptionOrNull() as? takagi.ru.fleur.domain.model.FleurError
                        )
                    )
                }
            }
            
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 同步指定账户的邮件
     * @param accountId 账户ID
     */
    suspend fun syncAccount(accountId: String): Result<SyncResult> {
        return emailRepository.syncEmails(accountId)
    }
}
