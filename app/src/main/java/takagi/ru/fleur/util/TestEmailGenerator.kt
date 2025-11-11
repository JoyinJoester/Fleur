package takagi.ru.fleur.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import takagi.ru.fleur.data.local.entity.EmailEntity
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * 测试邮件生成器
 * 用于生成各种类型的测试邮件数据
 */
object TestEmailGenerator {
    
    // 测试发件人列表
    private val senders = listOf(
        "张三" to "zhangsan@example.com",
        "李四" to "lisi@company.com",
        "王五" to "wangwu@tech.com",
        "赵六" to "zhaoliu@startup.io",
        "GitHub" to "noreply@github.com",
        "LinkedIn" to "notifications@linkedin.com",
        "Amazon" to "no-reply@amazon.com",
        "Google" to "noreply@google.com",
        "Apple" to "noreply@apple.com",
        "Microsoft" to "noreply@microsoft.com"
    )
    
    // 测试主题模板
    private val subjects = listOf(
        "项目进度更新 - Q4季度报告",
        "会议邀请：下周一产品评审会",
        "重要通知：系统维护公告",
        "【紧急】服务器故障处理报告",
        "Re: 关于新功能的讨论",
        "Fwd: 客户反馈汇总",
        "周报：本周工作总结",
        "【提醒】明天的团队建设活动",
        "代码审查请求 - PR #1234",
        "Bug修复：登录问题已解决",
        "新版本发布通知 v2.0.0",
        "Welcome to our service!",
        "Your order has been shipped",
        "Security alert: New sign-in",
        "Monthly newsletter - November 2024",
        "Invitation to connect on LinkedIn",
        "Your GitHub pull request was merged",
        "Payment confirmation",
        "Meeting notes from yesterday",
        "Q&A: Common questions answered"
    )
    
    // 测试邮件正文模板
    private val bodies = listOf(
        """
        您好，
        
        这是本周的项目进度更新。我们已经完成了以下工作：
        
        1. 完成了用户界面的重构
        2. 修复了已知的性能问题
        3. 添加了新的测试用例
        
        下周计划：
        - 开始集成测试
        - 准备发布候选版本
        
        如有任何问题，请随时联系我。
        
        此致
        敬礼
        """.trimIndent(),
        
        """
        Hi team,
        
        I hope this email finds you well. I wanted to share some exciting updates about our project.
        
        We've made significant progress this week and are on track to meet our deadlines.
        
        Please review the attached documents and let me know if you have any questions.
        
        Best regards
        """.trimIndent(),
        
        """
        尊敬的用户，
        
        感谢您使用我们的服务。我们将于本周末进行系统维护，预计维护时间为2小时。
        
        维护期间，部分功能可能无法使用，给您带来不便敬请谅解。
        
        维护完成后，系统将自动恢复正常。
        
        客服团队
        """.trimIndent(),
        
        """
        Hello,
        
        This is a quick reminder about tomorrow's meeting at 2 PM.
        
        Agenda:
        - Review Q4 goals
        - Discuss new features
        - Team updates
        
        See you there!
        """.trimIndent(),
        
        """
        各位同事，
        
        本周工作总结如下：
        
        完成的任务：
        • 实现了新的导航抽屉UI
        • 优化了邮件列表性能
        • 修复了5个bug
        
        遇到的问题：
        • 需要更多的测试设备
        • API文档需要更新
        
        下周计划：
        • 继续优化性能
        • 准备用户测试
        
        谢谢！
        """.trimIndent()
    )
    
    /**
     * 生成单条测试邮件
     * 
     * @param accountId 账户ID
     * @param id 邮件ID（可选，默认自动生成）
     * @param isRead 是否已读（可选，默认随机）
     * @param isStarred 是否星标（可选，默认随机）
     * @param hoursAgo 多少小时前（可选，默认随机）
     */
    fun generateEmail(
        accountId: String,
        id: String = "test_email_${Random.nextInt(10000, 99999)}",
        isRead: Boolean? = null,
        isStarred: Boolean? = null,
        hoursAgo: Int? = null
    ): EmailEntity {
        val sender = senders.random()
        val subject = subjects.random()
        val body = bodies.random()
        val bodyPreview = body.take(200)
        
        // 计算时间戳
        val now = Clock.System.now()
        val timestamp = if (hoursAgo != null) {
            now.minus(hoursAgo.hours)
        } else {
            now.minus(Random.nextInt(0, 168).hours) // 0-7天内
        }
        
        return EmailEntity(
            id = id,
            threadId = "thread_${id}",
            accountId = accountId,
            fromAddress = sender.second,
            fromName = sender.first,
            toAddresses = """[{"address":"me@example.com","name":"我"}]""",
            ccAddresses = null,
            bccAddresses = null,
            subject = subject,
            bodyPreview = bodyPreview,
            bodyPlain = body,
            bodyHtml = "<html><body><p>${body.replace("\n", "<br>")}</p></body></html>",
            bodyMarkdown = null,
            contentType = "html",
            timestamp = timestamp.toEpochMilliseconds(),
            isRead = isRead ?: Random.nextBoolean(),
            isStarred = isStarred ?: (Random.nextInt(100) < 20), // 20%概率星标
            labels = null
        )
    }
    
    /**
     * 生成一组测试邮件
     * 
     * @param accountId 账户ID
     * @param count 邮件数量
     * @param unreadRatio 未读邮件比例（0.0-1.0）
     */
    fun generateEmails(
        accountId: String,
        count: Int = 20,
        unreadRatio: Float = 0.3f
    ): List<EmailEntity> {
        return (1..count).map { index ->
            val isRead = Random.nextFloat() > unreadRatio
            generateEmail(
                accountId = accountId,
                id = "test_email_${System.currentTimeMillis()}_$index",
                isRead = isRead,
                hoursAgo = index * 2 // 每封邮件间隔2小时
            )
        }
    }
    
    /**
     * 生成邮件线程（对话）
     * 
     * @param accountId 账户ID
     * @param threadId 线程ID
     * @param messageCount 消息数量
     */
    fun generateEmailThread(
        accountId: String,
        threadId: String = "thread_${Random.nextInt(1000, 9999)}",
        messageCount: Int = 5
    ): List<EmailEntity> {
        val baseSubject = "Re: ${subjects.random()}"
        val now = Clock.System.now()
        
        return (1..messageCount).map { index ->
            val sender = senders.random()
            val body = bodies.random()
            
            EmailEntity(
                id = "email_${threadId}_$index",
                threadId = threadId,
                accountId = accountId,
                fromAddress = sender.second,
                fromName = sender.first,
                toAddresses = """[{"address":"me@example.com","name":"我"}]""",
                ccAddresses = null,
                bccAddresses = null,
                subject = baseSubject,
                bodyPreview = body.take(200),
                bodyPlain = body,
                bodyHtml = "<html><body><p>${body.replace("\n", "<br>")}</p></body></html>",
                bodyMarkdown = null,
                contentType = "html",
                timestamp = now.minus((messageCount - index).days).toEpochMilliseconds(),
                isRead = index < messageCount, // 最后一封未读
                isStarred = false,
                labels = null
            )
        }
    }
    
    /**
     * 生成带附件的邮件
     */
    fun generateEmailWithAttachments(
        accountId: String,
        attachmentCount: Int = 3
    ): EmailEntity {
        val email = generateEmail(accountId)
        // 注意：这里只生成邮件实体，附件需要单独插入到 attachments 表
        return email.copy(
            subject = "[附件] ${email.subject}"
        )
    }
    
    /**
     * 生成各种场景的测试邮件集合
     */
    fun generateTestSuite(accountId: String): List<EmailEntity> {
        return buildList {
            // 1. 最近的未读邮件
            add(generateEmail(accountId, isRead = false, hoursAgo = 1))
            add(generateEmail(accountId, isRead = false, hoursAgo = 3))
            
            // 2. 星标邮件
            add(generateEmail(accountId, isStarred = true, hoursAgo = 12))
            add(generateEmail(accountId, isStarred = true, hoursAgo = 24))
            
            // 3. 已读邮件
            add(generateEmail(accountId, isRead = true, hoursAgo = 48))
            add(generateEmail(accountId, isRead = true, hoursAgo = 72))
            
            // 4. 邮件线程
            addAll(generateEmailThread(accountId, messageCount = 3))
            
            // 5. 更多随机邮件
            addAll(generateEmails(accountId, count = 10))
        }
    }
}
