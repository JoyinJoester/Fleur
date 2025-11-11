package takagi.ru.fleur.domain.usecase

import takagi.ru.fleur.domain.repository.EmailRepository
import javax.inject.Inject

/**
 * 恢复邮件用例
 * 用于从垃圾箱恢复邮件到收件箱
 */
class RestoreEmailUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 恢复单个邮件
     * @param emailId 邮件ID
     * @return Result<Unit> 恢复结果
     */
    suspend operator fun invoke(emailId: String): Result<Unit> {
        return emailRepository.restoreEmail(emailId)
    }
    
    /**
     * 批量恢复邮件
     * @param emailIds 邮件ID列表
     * @return Result<Unit> 恢复结果
     */
    suspend fun restoreMultiple(emailIds: List<String>): Result<Unit> {
        return emailRepository.restoreEmails(emailIds)
    }
}
