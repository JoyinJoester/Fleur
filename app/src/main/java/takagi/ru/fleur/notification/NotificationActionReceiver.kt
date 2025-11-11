package takagi.ru.fleur.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import takagi.ru.fleur.domain.usecase.ArchiveEmailUseCase
import takagi.ru.fleur.domain.usecase.DeleteEmailUseCase
import javax.inject.Inject

/**
 * 通知操作接收器
 * 处理通知中的操作按钮点击事件
 */
class NotificationActionReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        val emailId = intent.getStringExtra(NotificationService.EXTRA_EMAIL_ID) ?: return
        
        // 手动获取依赖
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            NotificationActionReceiverEntryPoint::class.java
        )
        
        when (intent.action) {
            NotificationService.ACTION_ARCHIVE -> {
                handleArchive(emailId, entryPoint.archiveEmailUseCase(), entryPoint.notificationService())
            }
            NotificationService.ACTION_DELETE -> {
                handleDelete(emailId, entryPoint.deleteEmailUseCase(), entryPoint.notificationService())
            }
            NotificationService.ACTION_REPLY -> {
                handleReply(context, emailId)
            }
        }
    }
    
    /**
     * 处理归档操作
     */
    private fun handleArchive(
        emailId: String,
        archiveEmailUseCase: ArchiveEmailUseCase,
        notificationService: NotificationService
    ) {
        scope.launch {
            archiveEmailUseCase(emailId)
            notificationService.cancelEmailNotification(emailId)
        }
    }
    
    /**
     * 处理删除操作
     */
    private fun handleDelete(
        emailId: String,
        deleteEmailUseCase: DeleteEmailUseCase,
        notificationService: NotificationService
    ) {
        scope.launch {
            deleteEmailUseCase(emailId)
            notificationService.cancelEmailNotification(emailId)
        }
    }
    
    /**
     * 处理回复操作
     * 打开撰写界面
     */
    private fun handleReply(context: Context, emailId: String) {
        val intent = Intent(context, takagi.ru.fleur.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NotificationService.EXTRA_EMAIL_ID, emailId)
            putExtra("action", "reply")
        }
        context.startActivity(intent)
    }
}

/**
 * Hilt EntryPoint 用于 BroadcastReceiver
 */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface NotificationActionReceiverEntryPoint {
    fun archiveEmailUseCase(): ArchiveEmailUseCase
    fun deleteEmailUseCase(): DeleteEmailUseCase
    fun notificationService(): NotificationService
}
