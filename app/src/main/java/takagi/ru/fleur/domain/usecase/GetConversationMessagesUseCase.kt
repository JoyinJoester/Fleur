package takagi.ru.fleur.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.data.mapper.MessageMapper
import takagi.ru.fleur.domain.repository.AccountRepository
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.ui.model.MessageUiModel
import javax.inject.Inject

/**
 * 获取对话消息列表用例
 * 
 * 获取与指定联系人的所有消息
 * 消息按时间升序排序（最早的在前）
 */
class GetConversationMessagesUseCase @Inject constructor(
    private val emailRepository: EmailRepository,
    private val accountRepository: AccountRepository
) {
    /**
     * 执行用例
     * 
     * @param contactEmail 联系人邮箱地址（对话ID）
     * @param accountId 当前用户账户ID
     * @param currentUserEmail 当前用户邮箱地址，用于判断消息方向
     * @return Flow<Result<List<MessageUiModel>>> 消息列表流
     */
    operator fun invoke(
        contactEmail: String,
        accountId: String?,
        currentUserEmail: String
    ): Flow<Result<List<MessageUiModel>>> {
        val normalizedContact = contactEmail.trim().lowercase()
        return emailRepository.getEmailsByContact(accountId, normalizedContact).map { result ->
            result.mapCatching { emails ->
                // 将邮件列表转换为消息列表
                val messages = MessageMapper.fromEmailList(
                    emails = emails,
                    currentUserEmail = currentUserEmail
                )
                
                // 按时间升序排序（最早的消息在前）
                // 这样在 UI 中使用 reverseLayout 时，最新消息会显示在底部
                messages.sortedBy { it.timestamp }
            }
        }
    }
}
