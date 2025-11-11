package takagi.ru.fleur.domain.usecase

import takagi.ru.fleur.domain.repository.EmailRepository
import javax.inject.Inject

/**
 * 归档邮件用例
 */
class ArchiveEmailUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 归档单个邮件
     */
    suspend operator fun invoke(emailId: String): Result<Unit> {
        return emailRepository.archiveEmail(emailId)
    }
    
    /**
     * 批量归档邮件
     */
    suspend fun archiveMultiple(emailIds: List<String>): Result<Unit> {
        return emailRepository.archiveEmails(emailIds)
    }
}
