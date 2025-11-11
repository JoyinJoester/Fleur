package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.repository.EmailRepository
import javax.inject.Inject

/**
 * 获取邮件列表用例
 */
class GetEmailsUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 执行用例
     * @param accountId 账户ID，null表示所有账户
     * @param page 页码
     * @param pageSize 每页数量
     */
    operator fun invoke(
        accountId: String? = null,
        page: Int = 0,
        pageSize: Int = 50
    ): Flow<Result<List<Email>>> {
        return emailRepository.getEmails(accountId, page, pageSize)
    }
}
