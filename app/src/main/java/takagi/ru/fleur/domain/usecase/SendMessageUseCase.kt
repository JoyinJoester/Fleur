package takagi.ru.fleur.domain.usecase

import kotlinx.datetime.Clock
import takagi.ru.fleur.domain.model.Attachment
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.EmailAddress
import takagi.ru.fleur.domain.repository.EmailRepository
import java.util.UUID
import javax.inject.Inject

/**
 * 发送消息用例
 * 
 * 在对话中发送新消息（回复邮件）
 * 将消息内容转换为邮件格式并发送
 */
class SendMessageUseCase @Inject constructor(
    private val emailRepository: EmailRepository
) {
    /**
     * 执行用例
     * 
     * @param threadId 线程ID（对话ID）
     * @param accountId 发送账户ID
     * @param from 发件人地址
     * @param to 收件人地址列表
     * @param subject 邮件主题
     * @param content 消息文本内容
     * @param attachments 附件列表
     * @param replyToEmailId 回复的邮件ID（可选）
     * @return Result<Email> 发送结果，成功时返回已发送的邮件对象
     */
    suspend operator fun invoke(
        threadId: String,
        accountId: String,
        from: EmailAddress,
        to: List<EmailAddress>,
        subject: String,
        content: String,
        attachments: List<Attachment> = emptyList(),
        replyToEmailId: String? = null
    ): Result<Email> {
        // 验证输入
        if (to.isEmpty()) {
            return Result.failure(IllegalArgumentException("收件人不能为空"))
        }
        
        if (content.isBlank() && attachments.isEmpty()) {
            return Result.failure(IllegalArgumentException("消息内容和附件不能同时为空"))
        }
        
        // 构建邮件对象
        val email = Email(
            id = UUID.randomUUID().toString(),
            threadId = threadId,
            accountId = accountId,
            from = from,
            to = to,
            cc = emptyList(),
            bcc = emptyList(),
            subject = subject,
            bodyPreview = content.take(200), // 前 200 字符作为预览
            bodyPlain = content,
            bodyHtml = null,
            bodyMarkdown = null,
            contentType = "text",
            attachments = attachments,
            timestamp = Clock.System.now(),
            isRead = true, // 发送的邮件默认已读
            isStarred = false,
            labels = emptyList()
        )
        
        // 发送邮件
        return emailRepository.sendEmail(email).map { email }
    }
    
    /**
     * 快速回复
     * 
     * 简化的回复方法，自动从原邮件提取收件人和主题
     * 
     * @param originalEmail 原始邮件
     * @param from 发件人地址
     * @param content 回复内容
     * @param attachments 附件列表
     * @return Result<Email> 发送结果
     */
    suspend fun reply(
        originalEmail: Email,
        from: EmailAddress,
        content: String,
        attachments: List<Attachment> = emptyList()
    ): Result<Email> {
        // 回复时，收件人是原邮件的发件人
        val to = listOf(originalEmail.from)
        
        // 主题添加 "Re: " 前缀（如果还没有）
        val subject = if (originalEmail.subject.startsWith("Re:", ignoreCase = true)) {
            originalEmail.subject
        } else {
            "Re: ${originalEmail.subject}"
        }
        
        return invoke(
            threadId = originalEmail.threadId,
            accountId = originalEmail.accountId,
            from = from,
            to = to,
            subject = subject,
            content = content,
            attachments = attachments,
            replyToEmailId = originalEmail.id
        )
    }
}
