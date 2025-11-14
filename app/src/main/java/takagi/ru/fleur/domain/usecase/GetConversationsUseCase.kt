package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.data.mapper.ConversationMapper
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.repository.AccountRepository
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.ui.model.ConversationUiModel
import javax.inject.Inject

/**
 * 获取对话列表用例
 * 
 * 将邮件按联系人分组，转换为对话列表
 * 每个联系人代表一个对话
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
            pageSize = pageSize * 5 // 获取更多邮件以确保聚合后有足够对话
        ).map { result ->
            result.mapCatching { emails ->
                // 获取当前用户邮箱地址
                val currentUserEmail = getCurrentUserEmail(accountId)
                
                // 按联系人邮箱分组邮件（聊天列表逻辑）
                val emailsByContact = emails.groupBy { email ->
                    determineContactEmail(email, currentUserEmail)
                        .trim()
                        .lowercase()
                }
                
                // 将每个联系人的邮件转换为一个对话模型
                emailsByContact.map { (contactEmail, contactEmails) ->
                    ConversationMapper.fromContactEmails(
                        contactEmail = contactEmail,
                        emails = contactEmails,
                        currentUserEmail = currentUserEmail
                    )
                }
                // 按最后消息时间降序排序
                .sortedByDescending { it.lastMessageTime }
                // 应用分页限制
                .take(pageSize)
            }
        }
    }
    
    /**
     * 确定联系人邮箱地址
     * 如果当前用户是发件人，返回第一个收件人；否则返回发件人
     */
    private fun determineContactEmail(email: Email, currentUserEmail: String): String {
        return if (email.from.address.equals(currentUserEmail, ignoreCase = true)) {
            // 当前用户是发件人，取第一个收件人
            email.to.firstOrNull()?.address ?: email.from.address
        } else {
            // 当前用户是收件人，取发件人
            email.from.address
        }
    }
    
    /**
     * 获取当前用户邮箱地址
     * 
     * @param accountId 账户ID，null 表示使用默认账户
     * @return 用户邮箱地址
     */
    private suspend fun getCurrentUserEmail(accountId: String?): String {
        return try {
            if (accountId != null) {
                accountRepository.getAccountById(accountId).first()?.email ?: ""
            } else {
                accountRepository.getDefaultAccount().first()?.email ?: ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
