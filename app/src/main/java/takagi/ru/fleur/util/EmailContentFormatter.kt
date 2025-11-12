package takagi.ru.fleur.util

import takagi.ru.fleur.domain.model.Email
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 邮件内容格式化工具类
 * 
 * 提供邮件回复和转发时的内容格式化功能，包括：
 * - 添加主题前缀（Re:、Fwd:）
 * - 构建引用正文
 * - 格式化原邮件信息头
 */
object EmailContentFormatter {
    
    /**
     * 添加回复前缀到主题
     * 
     * 如果主题已经包含 "Re:" 前缀（不区分大小写），则不重复添加
     * 
     * @param subject 原始主题
     * @return 添加了 "Re: " 前缀的主题（如果需要）
     */
    fun addReplyPrefix(subject: String): String {
        return if (subject.trim().startsWith("Re:", ignoreCase = true)) {
            subject
        } else {
            "Re: $subject"
        }
    }
    
    /**
     * 添加转发前缀到主题
     * 
     * 如果主题已经包含 "Fwd:" 前缀（不区分大小写），则不重复添加
     * 
     * @param subject 原始主题
     * @return 添加了 "Fwd: " 前缀的主题（如果需要）
     */
    fun addForwardPrefix(subject: String): String {
        return if (subject.trim().startsWith("Fwd:", ignoreCase = true)) {
            subject
        } else {
            "Fwd: $subject"
        }
    }
    
    /**
     * 构建回复邮件的正文
     * 
     * 格式：
     * ```
     * [用户输入区域]
     * 
     * ---------- 原始邮件 ----------
     * 发件人: 张三 <zhangsan@example.com>
     * 日期: 2024-01-15 14:30:25
     * 收件人: 李四 <lisi@example.com>
     * 抄送: 王五 <wangwu@example.com>
     * 主题: 会议通知
     * 
     * > 原邮件内容第一行
     * > 原邮件内容第二行
     * ```
     * 
     * @param originalEmail 原始邮件
     * @return 格式化后的回复正文
     */
    fun buildReplyBody(originalEmail: Email): String {
        return buildString {
            appendLine()
            appendLine()
            append(buildReplyQuote(originalEmail))
        }
    }

    /**
     * 构建回复引用内容（不包含用户输入区域）
     */
    fun buildReplyQuote(originalEmail: Email): String {
        return buildString {
            appendLine("---------- 原始邮件 ----------")
            appendLine("发件人: ${originalEmail.from.formatted()}")
            appendLine("日期: ${formatTimestamp(originalEmail.timestamp)}")
            appendLine("收件人: ${originalEmail.to.joinToString(", ") { it.formatted() }}")

            if (originalEmail.cc.isNotEmpty()) {
                appendLine("抄送: ${originalEmail.cc.joinToString(", ") { it.formatted() }}")
            }

            appendLine("主题: ${originalEmail.subject}")
            appendLine()

            val originalContent = getEmailContent(originalEmail)
            append(originalContent)
        }.trimEnd()
    }
    
    /**
     * 构建转发邮件的正文
     * 
     * 格式：
     * ```
     * [用户输入区域]
     * 
     * ---------- 转发邮件 ----------
     * 发件人: 张三 <zhangsan@example.com>
     * 日期: 2024-01-15 14:30:25
     * 收件人: 李四 <lisi@example.com>
     * 抄送: 王五 <wangwu@example.com>
     * 主题: 会议通知
     * 
     * 附件:
     *   - 会议议程.pdf (245.3 KB)
     *   - 演示文稿.pptx (1.2 MB)
     * 
     * 原邮件内容第一行
     * 原邮件内容第二行
     * ```
     * 
     * @param originalEmail 原始邮件
     * @return 格式化后的转发正文
     */
    fun buildForwardBody(originalEmail: Email): String {
        return buildString {
            appendLine()
            appendLine()
            append(buildForwardQuote(originalEmail))
        }
    }

    /**
     * 构建转发引用内容（不包含用户输入区域）
     */
    fun buildForwardQuote(originalEmail: Email): String {
        return buildString {
            appendLine("---------- 转发邮件 ----------")
            appendLine("发件人: ${originalEmail.from.formatted()}")
            appendLine("日期: ${formatTimestamp(originalEmail.timestamp)}")
            appendLine("收件人: ${originalEmail.to.joinToString(", ") { it.formatted() }}")

            if (originalEmail.cc.isNotEmpty()) {
                appendLine("抄送: ${originalEmail.cc.joinToString(", ") { it.formatted() }}")
            }

            appendLine("主题: ${originalEmail.subject}")
            appendLine()

            if (originalEmail.attachments.isNotEmpty()) {
                appendLine("附件:")
                originalEmail.attachments.forEach { attachment ->
                    appendLine("  - ${attachment.fileName} (${attachment.formattedSize()})")
                }
                appendLine()
            }

            val originalContent = getEmailContent(originalEmail)
            append(originalContent)
        }.trimEnd()
    }
    
    /**
     * 获取邮件内容
     * 
     * 优先级：纯文本 > Markdown > HTML（去除标签）
     * 
     * @param email 邮件对象
     * @return 邮件内容文本
     */
    private fun getEmailContent(email: Email): String {
        return when {
            email.bodyPlain.isNotBlank() -> email.bodyPlain
            email.bodyMarkdown != null && email.bodyMarkdown.isNotBlank() -> email.bodyMarkdown
            email.bodyHtml != null && email.bodyHtml.isNotBlank() -> stripHtml(email.bodyHtml)
            else -> email.bodyPreview
        }
    }
    
    /**
     * 格式化时间戳为可读格式
     * 
     * 格式：yyyy-MM-dd HH:mm:ss
     * 
     * @param timestamp 时间戳
     * @return 格式化后的时间字符串
     */
    private fun formatTimestamp(timestamp: Instant): String {
        val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
        return String.format(
            "%04d-%02d-%02d %02d:%02d:%02d",
            localDateTime.year,
            localDateTime.monthNumber,
            localDateTime.dayOfMonth,
            localDateTime.hour,
            localDateTime.minute,
            localDateTime.second
        )
    }
    
    /**
     * 移除 HTML 标签，转换为纯文本
     * 
     * 这是一个简化的实现，处理常见的 HTML 实体和标签
     * 
     * @param html HTML 内容
     * @return 纯文本内容
     */
    private fun stripHtml(html: String): String {
        return html
            // 移除所有 HTML 标签
            .replace(Regex("<[^>]*>"), "")
            // 替换常见的 HTML 实体
            .replace("&nbsp;", " ")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            // 移除多余的空白
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * 合并回复内容和原邮件（发送时调用）
     * 
     * 格式：
     * ```
     * [用户的回复内容]
     * 
     * ---------- 原始邮件 ----------
     * 发件人: 张三 <zhangsan@example.com>
     * 日期: 2024-01-15 14:30:25
     * 收件人: 李四 <lisi@example.com>
     * 抄送: 王五 <wangwu@example.com>
     * 主题: 会议通知
     * 
     * > 原邮件内容第一行
     * > 原邮件内容第二行
     * ```
     * 
     * @param replyText 用户输入的回复内容
     * @param originalEmail 原始邮件
     * @return 合并后的邮件正文
     */
    fun mergeReplyContent(replyText: String, originalEmail: Email): String {
        return buildString {
            // 用户的回复内容
            append(replyText)
            
            // 分隔线和原邮件
            appendLine()
            appendLine()
            appendLine("---------- 原始邮件 ----------")
            appendLine("发件人: ${originalEmail.from.formatted()}")
            appendLine("日期: ${formatTimestamp(originalEmail.timestamp)}")
            appendLine("收件人: ${originalEmail.to.joinToString(", ") { it.formatted() }}")
            if (originalEmail.cc.isNotEmpty()) {
                appendLine("抄送: ${originalEmail.cc.joinToString(", ") { it.formatted() }}")
            }
            appendLine("主题: ${originalEmail.subject}")
            appendLine()
            
            // 引用原邮件内容
            val originalContent = getEmailContent(originalEmail)
            append(originalContent)
        }
    }
    
    /**
     * 合并转发内容和原邮件（发送时调用）
     * 
     * 格式：
     * ```
     * [用户的转发说明]
     * 
     * ---------- 转发邮件 ----------
     * 发件人: 张三 <zhangsan@example.com>
     * 日期: 2024-01-15 14:30:25
     * 收件人: 李四 <lisi@example.com>
     * 抄送: 王五 <wangwu@example.com>
     * 主题: 会议通知
     * 
     * 附件:
     *   - 会议议程.pdf (245.3 KB)
     *   - 演示文稿.pptx (1.2 MB)
     * 
     * 原邮件内容第一行
     * 原邮件内容第二行
     * ```
     * 
     * @param forwardNote 用户输入的转发说明
     * @param originalEmail 原始邮件
     * @return 合并后的邮件正文
     */
    fun mergeForwardContent(forwardNote: String, originalEmail: Email): String {
        return buildString {
            // 用户的转发说明
            if (forwardNote.isNotBlank()) {
                append(forwardNote)
                appendLine()
                appendLine()
            }
            
            // 分隔线和原邮件
            appendLine("---------- 转发邮件 ----------")
            appendLine("发件人: ${originalEmail.from.formatted()}")
            appendLine("日期: ${formatTimestamp(originalEmail.timestamp)}")
            appendLine("收件人: ${originalEmail.to.joinToString(", ") { it.formatted() }}")
            if (originalEmail.cc.isNotEmpty()) {
                appendLine("抄送: ${originalEmail.cc.joinToString(", ") { it.formatted() }}")
            }
            appendLine("主题: ${originalEmail.subject}")
            appendLine()
            
            // 附件信息
            if (originalEmail.attachments.isNotEmpty()) {
                appendLine("附件:")
                originalEmail.attachments.forEach { attachment ->
                    appendLine("  - ${attachment.fileName} (${attachment.formattedSize()})")
                }
                appendLine()
            }
            
            // 原邮件内容（不添加引用标记）
            val originalContent = getEmailContent(originalEmail)
            append(originalContent)
        }
    }
}
