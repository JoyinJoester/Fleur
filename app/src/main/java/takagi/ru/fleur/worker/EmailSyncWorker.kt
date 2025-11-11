package takagi.ru.fleur.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import takagi.ru.fleur.domain.repository.AccountRepository
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.domain.repository.PreferencesRepository
import takagi.ru.fleur.domain.usecase.SyncEmailsUseCase
import takagi.ru.fleur.notification.NotificationService

/**
 * 邮件同步 Worker
 * 后台周期性同步所有账户的邮件
 */
@HiltWorker
class EmailSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncEmailsUseCase: SyncEmailsUseCase,
    private val emailRepository: EmailRepository,
    private val accountRepository: AccountRepository,
    private val preferencesRepository: PreferencesRepository,
    private val notificationService: NotificationService
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        const val WORK_NAME = "email_sync_work"
        const val TAG = "EmailSyncWorker"
    }
    
    /**
     * 执行同步任务
     */
    override suspend fun doWork(): Result {
        return try {
            // 检查通知是否启用
            val preferences = preferencesRepository.getUserPreferences().first()
            val notificationsEnabled = preferences.notificationsEnabled && 
                                      notificationService.hasNotificationPermission()
            
            // 调用同步用例
            val syncResult = syncEmailsUseCase()
            
            syncResult.fold(
                onSuccess = { results ->
                    // 如果启用了通知，为有新邮件的账户发送通知
                    if (notificationsEnabled) {
                        results.forEach { result ->
                            if (result.success && result.newEmailsCount > 0) {
                                sendNewEmailNotifications(result.accountId)
                            }
                        }
                    }
                    
                    // 检查是否所有账户都同步成功
                    val allSuccess = results.all { it.success }
                    
                    if (allSuccess) {
                        Result.success()
                    } else {
                        // 部分失败，重试
                        Result.retry()
                    }
                },
                onFailure = { error ->
                    // 同步失败，重试
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            // 发生异常，重试
            Result.retry()
        }
    }
    
    /**
     * 发送新邮件通知
     */
    private suspend fun sendNewEmailNotifications(accountId: String) {
        try {
            // 获取账户信息
            val account = accountRepository.getAccounts().first()
                .find { it.id == accountId } ?: return
            
            // 获取最近的未读邮件（最多 5 封）
            val emails = emailRepository.getEmails(accountId = accountId, page = 0, pageSize = 5)
                .first()
                .getOrNull()
                ?.filter { !it.isRead }
                ?: return
            
            // 为每封新邮件发送通知
            emails.forEach { email ->
                notificationService.showNewEmailNotification(
                    email = email,
                    accountName = account.displayName ?: account.email
                )
            }
        } catch (e: Exception) {
            // 忽略通知错误，不影响同步结果
        }
    }
}
