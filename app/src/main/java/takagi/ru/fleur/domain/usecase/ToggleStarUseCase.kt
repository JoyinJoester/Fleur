package takagi.ru.fleur.domain.usecase

import takagi.ru.fleur.domain.repository.EmailRepository
import javax.inject.Inject

/**
 * 切换星标用例
 * 用于添加或取消邮件的星标
 */
class ToggleStarUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 切换单个邮件的星标状态
     * @param emailId 邮件ID
     * @param isStarred 是否星标
     * @return Result<Unit> 操作结果
     */
    suspend operator fun invoke(emailId: String, isStarred: Boolean): Result<Unit> {
        return emailRepository.toggleStar(emailId, isStarred)
    }
    
    /**
     * 批量切换邮件的星标状态
     * @param emailIds 邮件ID列表
     * @param isStarred 是否星标
     * @return Result<Unit> 操作结果
     */
    suspend fun toggleMultiple(emailIds: List<String>, isStarred: Boolean): Result<Unit> {
        return try {
            // 逐个切换星标状态
            emailIds.forEach { emailId ->
                val result = emailRepository.toggleStar(emailId, isStarred)
                if (result.isFailure) {
                    return result
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
