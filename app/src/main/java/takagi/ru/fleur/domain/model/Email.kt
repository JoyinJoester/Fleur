package takagi.ru.fleur.domain.model

import kotlinx.datetime.Instant

/**
 * 邮件模型
 * @property id 邮件唯一标识
 * @property threadId 邮件线程ID
 * @property accountId 所属账户ID
 * @property from 发件人
 * @property to 收件人列表
 * @property cc 抄送列表
 * @property bcc 密送列表
 * @property subject 主题
 * @property bodyPreview 正文预览（前200字符）
 * @property bodyPlain 纯文本正文
 * @property bodyHtml HTML格式正文（可选）
 * @property bodyMarkdown Markdown格式正文（可选）
 * @property contentType 内容类型（text/markdown/html）
 * @property attachments 附件列表
 * @property timestamp 时间戳
 * @property isRead 是否已读
 * @property isStarred 是否星标
 * @property labels 标签列表
 */
data class Email(
    val id: String,
    val threadId: String,
    val accountId: String,
    val from: EmailAddress,
    val to: List<EmailAddress>,
    val cc: List<EmailAddress> = emptyList(),
    val bcc: List<EmailAddress> = emptyList(),
    val subject: String,
    val bodyPreview: String,
    val bodyPlain: String,
    val bodyHtml: String? = null,
    val bodyMarkdown: String? = null,
    val contentType: String = "text",
    val attachments: List<Attachment> = emptyList(),
    val timestamp: Instant,
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val labels: List<String> = emptyList()
) {
    /**
     * 是否有附件
     */
    fun hasAttachments(): Boolean = attachments.isNotEmpty()
    
    /**
     * 获取所有收件人（包括to、cc、bcc）
     */
    fun allRecipients(): List<EmailAddress> = to + cc + bcc
}
