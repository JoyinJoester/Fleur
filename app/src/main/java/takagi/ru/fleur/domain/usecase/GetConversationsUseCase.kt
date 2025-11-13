package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.data.mapper.ConversationMapper
import takagi.ru.fleur.domain.repository.AccountRepository
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.ui.model.ConversationUiModel
import javax.inject.Inject

/**
 * 获取对话列表用例
 * 
 * 将邮件按线程分组，转换为对话列表
 * 每个线程代表一个对话
 */
class GetConversationsUseCase @Inject constructor(
    private val emailRepository: EmailRepository,
    private val accountRepository: AccountRepository
) {
    /**
     * 执行用例
     * 
     * @param accountId 账户ID，null 表示使用默认账户
     * @param page 页码（从 0 开始）
     * @param pageSize 每页数量
     * @return Flow<Result<List<ConversationUiModel>>> 对话列表流
     */
    operator fun invoke(
        accountId: String? = null,
        page: Int = 0,
        pageSize: Int = 50
    ): Flow<Result<List<ConversationUiModel>>> {
        return emailRepository.getEmails(
            accountId = accountId,
            page = page,
            pageSize = pageSize
        ).map { result ->
            result.mapCatching { emails ->
                // 获取当前用户邮箱地址
                val currentUserEmail = getCurrentUserEmail(accountId)
                
                // 按 threadId 分组邮件
                val emailsByThread = emails.groupBy { it.threadId }
                
                // 将每个线程转换为对话模型
                emailsByThread.map { (threadId, threadEmails) ->
                    ConversationMapper.fromEmailThread(
                        threadId = threadId,
                        emails = threadEmails,
                        currentUserEmail = currentUserEmail
                    )
                }
                // 按最后消息时间降序排序
                .sortedByDescending { it.lastMessageTime }
            }
        }
    }
    
    /**
     * 获取当前用户邮箱地址
     * 
     * @param accountId 账户ID，null 表示使用默认账户
     * @return 用户邮箱地址
     */
    private suspend fun getCurrentUserEmail(accountId: String?): String {
        // TODO: 实现从 AccountRepository 获取当前用户邮箱
        // 这里暂时返回空字符串，实际应该从 Flow 中获取
        // 由于 Flow 的异步特性，可能需要重构为在 ViewModel 中处理
        return ""
    }
}
