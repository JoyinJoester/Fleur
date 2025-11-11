package takagi.ru.fleur.domain.usecase

import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.repository.EmailRepository
import javax.inject.Inject

/**
 * 发送邮件用例
 */
class SendEmailUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 执行用例
     * @param email 要发送的邮件
     */
    suspend operator fun invoke(email: Email): Result<Unit> {
        // 验证邮件
        if (email.to.isEmpty()) {
            return Result.failure(IllegalArgumentException("收件人不能为空"))
        }
        
        if (email.subject.isBlank()) {
            return Result.failure(IllegalArgumentException("主题不能为空"))
        }
        
        // 发送邮件
        return emailRepository.sendEmail(email)
    }
}
