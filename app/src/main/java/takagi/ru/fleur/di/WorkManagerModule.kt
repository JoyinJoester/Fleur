package takagi.ru.fleur.di

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import takagi.ru.fleur.worker.EmailSyncWorker
import takagi.ru.fleur.worker.SyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * WorkManager 依赖注入模块
 * 配置后台同步任务
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {
    
    /**
     * 限定符：邮件同步工作名称
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class EmailSyncWorkName
    
    /**
     * 限定符：同步队列工作名称
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SyncQueueWorkName
    
    /**
     * 提供 WorkManager 实例
     */
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager = WorkManager.getInstance(context)
    
    /**
     * 配置并启动周期性邮件同步任务
     */
    @EmailSyncWorkName
    @Provides
    @Singleton
    fun provideEmailSyncWork(
        workManager: WorkManager
    ): String {
        // 创建网络连接约束
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // 创建周期性工作请求（每 15 分钟）
        val syncWorkRequest = PeriodicWorkRequestBuilder<EmailSyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(EmailSyncWorker.TAG)
            .build()
        
        // 将工作加入队列（如果已存在则保留）
        workManager.enqueueUniquePeriodicWork(
            EmailSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
        
        return EmailSyncWorker.WORK_NAME
    }
    
    /**
     * 配置并启动周期性同步队列处理任务
     * 用于将本地操作同步到 WebDAV 服务器
     */
    @SyncQueueWorkName
    @Provides
    @Singleton
    fun provideSyncQueueWork(
        workManager: WorkManager
    ): String {
        // 创建约束条件
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // 需要网络连接
            .setRequiresBatteryNotLow(true) // 电量不低时执行
            .build()
        
        // 创建周期性工作请求（每 15 分钟）
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("SyncWorker")
            .build()
        
        // 将工作加入队列（如果已存在则保留）
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
        
        return SyncWorker.WORK_NAME
    }
}
