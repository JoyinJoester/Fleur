package takagi.ru.fleur.data.mapper

import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.ui.model.ConversationUiModel

/**
 * 对话映射器
 * 将邮件线程转换为对话 UI 模型
 */
object ConversationMapper {
    
    /**
     * 从邮件线程创建对话模型
     * 
     * @param threadId 线程ID
     * @param emails 线程中的所有邮件，应按时间排序
     * @param currentUserEmail 当前用户邮箱地址，用于判断联系人
     * @return 对话 UI 模型
     */
    fun fromEmailThread(
        threadId: String,
        emails: List<Email>,
        currentUserEmail: String
    ): ConversationUiModel {
        require(emails.isNotEmpty()) { "Email list cannot be empty" }
        
        // 获取最新的邮件作为对话预览
        val latestEmail = emails.maxByOrNull { it.timestamp }!!
        
        // 计算未读消息数
        val unreadCount = emails.count { !it.isRead }
        
        // 判断是否有附件
        val hasAttachment = emails.any { it.hasAttachments() }
        
        // 确定联系人信息（对话中的另一方）
        val contact = determineContact(latestEmail, currentUserEmail)
        
        return ConversationUiModel(
            id = threadId,
            contactName = contact.name ?: contact.address,
            contactEmail = contact.address,
            contactAvatar = null, // TODO: 从联系人系统获取头像
            lastMessage = latestEmail.bodyPreview,
            lastMessageTime = latestEmail.timestamp,
            unreadCount = unreadCount,
            hasAttachment = hasAttachment,
            isPinned = false
        )
    }
    
    /**
     * 确定对话的联系人（对话中的另一方）
     * 如果是发送的邮件，联系人是收件人；如果是接收的邮件，联系人是发件人
     */
    private fun determineContact(
        email: Email,
        currentUserEmail: String
    ): takagi.ru.fleur.domain.model.EmailAddress {
        return if (email.from.address.equals(currentUserEmail, ignoreCase = true)) {
            // 当前用户是发件人，取第一个收件人作为联系人
            email.to.firstOrNull() ?: email.from
        } else {
            // 当前用户是收件人，取发件人作为联系人
            email.from
        }
    }
}
