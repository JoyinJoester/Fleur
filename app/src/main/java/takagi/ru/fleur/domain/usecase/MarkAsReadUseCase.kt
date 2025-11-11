package takagi.ru.fleur.domain.usecase

import takagi.ru.fleur.domain.repository.EmailRepository
import javax.inject.Inject

/**
 * 标记邮件为已读/未读用例
 */
class MarkAsReadUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 标记单个邮件为已读/未读
     */
    suspend operator fun invoke(emailId: String, isRead: Boolean): Result<Unit> {
        return emailRepository.markAsRead(emailId, isRead)
    }
    
    /**
     * 批量标记邮件为已读/未读
     */
    suspend fun markMultiple(emailIds: List<String>, isRead: Boolean): Result<Unit> {
        return emailRepository.markEmailsAsRead(emailIds, isRead)
    }
}
