package takagi.ru.fleur.domain.usecase

import takagi.ru.fleur.domain.repository.EmailRepository
import javax.inject.Inject

/**
 * 删除邮件用例
 */
class DeleteEmailUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 删除单个邮件
     */
    suspend operator fun invoke(emailId: String): Result<Unit> {
        return emailRepository.deleteEmail(emailId)
    }
    
    /**
     * 批量删除邮件
     */
    suspend fun deleteMultiple(emailIds: List<String>): Result<Unit> {
        return emailRepository.deleteEmails(emailIds)
    }
}
