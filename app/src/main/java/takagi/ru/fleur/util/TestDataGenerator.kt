package takagi.ru.fleur.util

import takagi.ru.fleur.data.local.entity.EmailEntity
import com.google.gson.Gson

/**
 * 测试数据生成器
 * 用于生成模拟邮件数据
 */
object TestDataGenerator {
    
    private val gson = Gson()
    
    private val senders = listOf(
        "张三" to "zhangsan@example.com",
        "李四" to "lisi@company.com",
        "王五" to "wangwu@test.com",
        "赵六" to "zhaoliu@demo.com",
        "GitHub" to "notifications@github.com",
        "Google" to "no-reply@google.com",
        "Amazon" to "auto-confirm@amazon.com"
    )
    
    private val subjects = listOf(
        "重要：项目进度更新",
        "会议邀请：下周一团队会议",
        "您的订单已发货",
        "账单提醒：本月账单已生成",
        "[GitHub] New pull request",
        "欢迎加入我们的团队！",
        "系统维护通知",
        "优惠活动：限时折扣",
        "密码重置请求",
        "新功能发布通知"
    )
    
    private val bodyTemplates = listOf(
        "您好，\n\n这是一封测试邮件。\n\n祝好！",
        "亲爱的用户，\n\n感谢您的支持。这是系统自动生成的测试邮件。\n\n最诚挚的问候",
        "Hi,\n\nThis is a test email from Fleur email client.\n\nBest regards",
        "尊敬的客户，\n\n您的请求已经收到并正在处理中。\n\n谢谢！"
    )
    
    /**
     * 生成测试邮件列表
     */
    fun generateTestEmails(accountId: String, count: Int = 20): List<EmailEntity> {
        val now = System.currentTimeMillis()
        return (1..count).map { index ->
            val sender = senders.random()
            val body = bodyTemplates.random()
            EmailEntity(
                id = "test_email_${accountId}_$index",
                threadId = "thread_$index",
                accountId = accountId,
                fromAddress = sender.second,
                fromName = sender.first,
                toAddresses = gson.toJson(listOf("me@example.com")),
                ccAddresses = null,
                bccAddresses = null,
                subject = subjects.random(),
                bodyPreview = body.take(50) + "...",
                bodyPlain = body,
                bodyHtml = null,
                bodyMarkdown = null,
                contentType = "text",
                timestamp = now - (index * 3600000L), // 每封邮件间隔1小时
                isRead = index > count / 2, // 前半部分未读
                isStarred = index % 5 == 0, // 每5封标星一封
                labels = if (index % 3 == 0) gson.toJson(listOf("重要")) else null
            )
        }
    }
}
