package takagi.ru.fleur.domain.model

import kotlinx.datetime.Instant

/**
 * 邮件线程模型
 * 表示具有相同主题的一组相关邮件
 * @property id 线程唯一标识
 * @property subject 主题
 * @property participants 参与者列表
 * @property emails 邮件列表（按时间排序）
 * @property lastMessageTime 最后一封邮件的时间
 * @property unreadCount 未读邮件数量
 */
data class EmailThread(
    val id: String,
    val subject: String,
    val participants: List<EmailAddress>,
    val emails: List<Email>,
    val lastMessageTime: Instant,
    val unreadCount: Int
) {
    /**
     * 获取最新的邮件
     */
    fun latestEmail(): Email? = emails.maxByOrNull { it.timestamp }
    
    /**
     * 获取第一封邮件
     */
    fun firstEmail(): Email? = emails.minByOrNull { it.timestamp }
    
    /**
     * 是否有未读邮件
     */
    fun hasUnread(): Boolean = unreadCount > 0
    
    /**
     * 邮件数量
     */
    fun messageCount(): Int = emails.size
}
