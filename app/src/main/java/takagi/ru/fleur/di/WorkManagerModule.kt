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
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * WorkManager 依赖注入模块
 * 配置后台同步任务
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {
    
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
}
