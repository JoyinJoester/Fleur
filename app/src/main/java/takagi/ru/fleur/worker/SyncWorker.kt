package takagi.ru.fleur.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import takagi.ru.fleur.data.sync.SyncQueueManager
import java.util.concurrent.TimeUnit

/**
 * 同步队列 Worker
 * 后台周期性处理同步队列，将本地操作同步到 WebDAV 服务器
 * 
 * 功能：
 * - 定期检查同步队列
 * - 处理待同步操作
 * - 自动重试失败的操作
 * - 仅在网络可用时执行
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncQueueManager: SyncQueueManager
) : CoroutineWorker(appContext, workerParams) {
    
    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "sync_queue_work"
        
        // 默认同步间隔：15 分钟
        private const val DEFAULT_SYNC_INTERVAL_MINUTES = 15L
        
        /**
         * 调度周期性同步任务
         * 
         * @param context 应用上下文
         * @param intervalMinutes 同步间隔（分钟），默认 15 分钟
         */
        fun schedule(context: Context, intervalMinutes: Long = DEFAULT_SYNC_INTERVAL_MINUTES) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // 需要网络连接
                .setRequiresBatteryNotLow(true) // 电量不低时执行
                .build()
            
            val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                intervalMinutes,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // 保持现有任务
                syncWorkRequest
            )
            
            Log.d(TAG, "已调度同步任务，间隔: $intervalMinutes 分钟")
        }
        
        /**
         * 取消周期性同步任务
         * 
         * @param context 应用上下文
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "已取消同步任务")
        }
    }
    
    /**
     * 执行同步任务
     * 
     * @return 任务执行结果
     */
    override suspend fun doWork(): Result {
        Log.d(TAG, "开始执行同步任务")
        
        return try {
            // 处理同步队列
            val successCount = syncQueueManager.processSyncQueue()
            
            Log.d(TAG, "同步任务完成，成功同步 $successCount 个操作")
            
            // 检查是否还有待同步操作
            val pendingCount = syncQueueManager.getPendingCount()
            
            if (pendingCount > 0) {
                Log.d(TAG, "还有 $pendingCount 个操作待同步")
                // 如果还有待同步操作，返回成功但建议重试
                // 这样可以在下一个周期继续处理
                Result.success()
            } else {
                Log.d(TAG, "所有操作已同步完成")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "同步任务执行失败", e)
            
            // 检查重试次数
            if (runAttemptCount < 3) {
                Log.d(TAG, "将重试同步任务，当前尝试次数: $runAttemptCount")
                Result.retry()
            } else {
                Log.w(TAG, "同步任务已达到最大重试次数，标记为失败")
                Result.failure()
            }
        }
    }
}
