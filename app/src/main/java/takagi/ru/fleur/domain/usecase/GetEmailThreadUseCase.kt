package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.domain.model.EmailThread
import takagi.ru.fleur.domain.repository.EmailRepository
import javax.inject.Inject

/**
 * 获取邮件线程用例
 */
class GetEmailThreadUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 执行用例
     * @param threadId 线程ID
     */
    operator fun invoke(threadId: String): Flow<Result<EmailThread>> {
        return emailRepository.getEmailThread(threadId)
    }
}
