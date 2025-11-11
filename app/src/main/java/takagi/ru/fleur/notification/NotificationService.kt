package takagi.ru.fleur.notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import takagi.ru.fleur.MainActivity
import takagi.ru.fleur.R
import takagi.ru.fleur.domain.model.Email
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知服务
 * 管理新邮件通知的创建和显示
 */
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val CHANNEL_ID_NEW_EMAIL = "new_email_channel"
        private const val CHANNEL_ID_SYNC = "sync_channel"
        private const val GROUP_KEY_EMAILS = "emails_group"
        
        private const val NOTIFICATION_ID_SUMMARY = 0
        private const val NOTIFICATION_ID_BASE = 1000
        
        const val ACTION_ARCHIVE = "action_archive"
        const val ACTION_DELETE = "action_delete"
        const val ACTION_REPLY = "action_reply"
        
        const val EXTRA_EMAIL_ID = "extra_email_id"
        const val EXTRA_ACCOUNT_ID = "extra_account_id"
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    /**
     * 创建通知渠道（Android 8.0+）
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 创建通知渠道组
            val emailGroup = NotificationChannelGroup(
                GROUP_KEY_EMAILS,
                "邮件通知"
            )
            notificationManager.createNotificationChannelGroup(emailGroup)
            
            // 新邮件通知渠道
            val newEmailChannel = NotificationChannel(
                CHANNEL_ID_NEW_EMAIL,
                "新邮件",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "接收新邮件时显示通知"
                group = GROUP_KEY_EMAILS
                enableVibration(true)
                enableLights(true)
            }
            
            // 同步通知渠道
            val syncChannel = NotificationChannel(
                CHANNEL_ID_SYNC,
                "邮件同步",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "邮件同步进度通知"
                group = GROUP_KEY_EMAILS
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannels(
                listOf(newEmailChannel, syncChannel)
            )
        }
    }
    
    /**
     * 显示新邮件通知
     * @param email 新邮件
     * @param accountName 账户名称
     */
    fun showNewEmailNotification(email: Email, accountName: String) {
        // 创建点击通知的 Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_EMAIL_ID, email.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            email.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 创建归档操作
        val archiveIntent = createActionIntent(ACTION_ARCHIVE, email.id, email.accountId)
        val archivePendingIntent = PendingIntent.getBroadcast(
            context,
            email.id.hashCode() + 1,
            archiveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 创建删除操作
        val deleteIntent = createActionIntent(ACTION_DELETE, email.id, email.accountId)
        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            email.id.hashCode() + 2,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 创建回复操作
        val replyIntent = createActionIntent(ACTION_REPLY, email.id, email.accountId)
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            email.id.hashCode() + 3,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_NEW_EMAIL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(email.from.name ?: email.from.address)
            .setContentText(email.subject)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(email.bodyPreview.take(200))
                    .setSummaryText(accountName)
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY_EMAILS)
            .setGroupSummary(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EMAIL)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "归档",
                archivePendingIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "删除",
                deletePendingIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "回复",
                replyPendingIntent
            )
            .build()
        
        // 显示通知
        val notificationId = NOTIFICATION_ID_BASE + email.id.hashCode()
        notificationManager.notify(notificationId, notification)
        
        // 更新分组摘要通知
        updateSummaryNotification()
    }
    
    /**
     * 显示多封新邮件通知（分组）
     * @param emails 新邮件列表
     * @param accountName 账户名称
     */
    fun showNewEmailsNotification(emails: List<Email>, accountName: String) {
        if (emails.isEmpty()) return
        
        // 为每封邮件创建单独的通知
        emails.forEach { email ->
            showNewEmailNotification(email, accountName)
        }
    }
    
    /**
     * 更新分组摘要通知
     */
    private fun updateSummaryNotification() {
        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID_NEW_EMAIL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("新邮件")
            .setContentText("您有新邮件")
            .setGroup(GROUP_KEY_EMAILS)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_SUMMARY, summaryNotification)
    }
    
    /**
     * 显示同步进度通知
     * @param accountName 账户名称
     * @param progress 进度（0-100）
     */
    fun showSyncProgressNotification(accountName: String, progress: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SYNC)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("正在同步邮件")
            .setContentText(accountName)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        notificationManager.notify(accountName.hashCode(), notification)
    }
    
    /**
     * 取消同步进度通知
     * @param accountName 账户名称
     */
    fun cancelSyncProgressNotification(accountName: String) {
        notificationManager.cancel(accountName.hashCode())
    }
    
    /**
     * 取消指定邮件的通知
     * @param emailId 邮件 ID
     */
    fun cancelEmailNotification(emailId: String) {
        val notificationId = NOTIFICATION_ID_BASE + emailId.hashCode()
        notificationManager.cancel(notificationId)
    }
    
    /**
     * 取消所有通知
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * 创建操作 Intent
     */
    private fun createActionIntent(action: String, emailId: String, accountId: String): Intent {
        return Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_EMAIL_ID, emailId)
            putExtra(EXTRA_ACCOUNT_ID, accountId)
        }
    }
    
    /**
     * 检查通知权限
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }
}
