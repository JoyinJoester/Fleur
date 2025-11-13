package takagi.ru.fleur.data.mapper

import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.ui.model.MessageStatus
import takagi.ru.fleur.ui.model.MessageUiModel

/**
 * 消息映射器
 * 将邮件转换为消息 UI 模型
 */
object MessageMapper {
    
    /**
     * 从邮件创建消息模型
     * 
     * @param email 邮件对象
     * @param currentUserEmail 当前用户邮箱地址，用于判断是否为发送的消息
     * @return 消息 UI 模型
     */
    fun fromEmail(
        email: Email,
        currentUserEmail: String
    ): MessageUiModel {
        val isSent = email.from.address.equals(currentUserEmail, ignoreCase = true)
        
        return MessageUiModel(
            id = email.id,
            conversationId = email.threadId,
            senderId = email.from.address,
            senderName = email.from.name ?: email.from.address,
            senderAvatar = null, // TODO: 从联系人系统获取头像
            content = email.bodyPlain,
            timestamp = email.timestamp,
            status = determineStatus(email, isSent),
            attachments = email.attachments.map { AttachmentMapper.fromAttachment(it) },
            replyTo = null, // TODO: 解析回复关系，需要额外的邮件数据
            isRead = email.isRead
        )
    }
    
    /**
     * 批量转换邮件列表为消息列表
     * 
     * @param emails 邮件列表
     * @param currentUserEmail 当前用户邮箱地址
     * @return 消息 UI 模型列表
     */
    fun fromEmailList(
        emails: List<Email>,
        currentUserEmail: String
    ): List<MessageUiModel> {
        return emails.map { fromEmail(it, currentUserEmail) }
    }
    
    /**
     * 确定消息状态
     * 
     * 对于发送的消息：
     * - 如果邮件已读，状态为 READ
     * - 否则状态为 SENT（简化处理，实际可能需要更复杂的逻辑）
     * 
     * 对于接收的消息：
     * - 如果已读，状态为 READ
     * - 否则状态为 DELIVERED
     */
    private fun determineStatus(email: Email, isSent: Boolean): MessageStatus {
        return if (isSent) {
            // 发送的消息，简化处理为已发送状态
            // 实际应用中可能需要从邮件服务器获取更详细的状态
            MessageStatus.SENT
        } else {
            // 接收的消息
            if (email.isRead) MessageStatus.READ else MessageStatus.DELIVERED
        }
    }
}
