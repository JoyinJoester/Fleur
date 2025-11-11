package takagi.ru.fleur.util

import takagi.ru.fleur.domain.model.FleurError
import java.util.regex.Pattern

/**
 * 邮件地址验证工具
 */
object EmailValidator {
    
    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )
    
    /**
     * 验证邮件地址格式
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_PATTERN.matcher(email).matches()
    }
    
    /**
     * 验证邮件地址列表
     * @return 验证结果，失败时返回错误
     */
    fun validateEmailList(emails: List<String>): Result<Unit> {
        if (emails.isEmpty()) {
            return Result.failure(
                FleurError.ValidationError(
                    field = "recipients",
                    errorMessage = "请至少添加一个收件人"
                )
            )
        }
        
        val invalidEmails = emails.filter { !isValidEmail(it) }
        if (invalidEmails.isNotEmpty()) {
            return Result.failure(
                FleurError.ValidationError(
                    field = "recipients",
                    errorMessage = "邮件地址格式不正确: ${invalidEmails.joinToString(", ")}"
                )
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * 验证邮件主题
     */
    fun validateSubject(subject: String): Result<Unit> {
        if (subject.isBlank()) {
            return Result.failure(
                FleurError.ValidationError(
                    field = "subject",
                    errorMessage = "请输入邮件主题"
                )
            )
        }
        
        if (subject.length > 200) {
            return Result.failure(
                FleurError.ValidationError(
                    field = "subject",
                    errorMessage = "主题长度不能超过 200 个字符"
                )
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * 验证邮件正文
     */
    fun validateBody(body: String): Result<Unit> {
        if (body.isBlank()) {
            return Result.failure(
                FleurError.ValidationError(
                    field = "body",
                    errorMessage = "请输入邮件内容"
                )
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * 验证附件大小
     */
    fun validateAttachmentSize(
        attachmentSize: Long,
        maxSingleSize: Long = 25 * 1024 * 1024, // 25MB
        totalSize: Long,
        maxTotalSize: Long = 50 * 1024 * 1024 // 50MB
    ): Result<Unit> {
        if (attachmentSize > maxSingleSize) {
            return Result.failure(
                FleurError.ValidationError(
                    field = "attachment",
                    errorMessage = "单个附件大小不能超过 ${maxSingleSize / 1024 / 1024}MB"
                )
            )
        }
        
        if (totalSize + attachmentSize > maxTotalSize) {
            return Result.failure(
                FleurError.ValidationError(
                    field = "attachments",
                    errorMessage = "附件总大小不能超过 ${maxTotalSize / 1024 / 1024}MB"
                )
            )
        }
        
        return Result.success(Unit)
    }
}
