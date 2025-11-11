package takagi.ru.fleur.data.repository

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.domain.repository.SyncStatusRepository
import takagi.ru.fleur.worker.EmailSyncWorker
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 同步状态仓库实现
 * 使用 WorkManager 监听后台同步任务状态
 */
@Singleton
class SyncStatusRepositoryImpl @Inject constructor(
    private val workManager: WorkManager
) : SyncStatusRepository {
    
    companion object {
        private const val ONE_TIME_SYNC_WORK = "one_time_email_sync"
    }
    
    /**
     * 观察同步状态
     */
    override fun observeSyncStatus(): Flow<Boolean> {
        return workManager
            .getWorkInfosByTagFlow(EmailSyncWorker.TAG)
            .map { workInfos ->
                workInfos.any { it.state == WorkInfo.State.RUNNING }
            }
    }
    
    /**
     * 触发立即同步
     */
    override suspend fun triggerSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<EmailSyncWorker>()
            .setConstraints(constraints)
            .addTag(EmailSyncWorker.TAG)
            .build()
        
        workManager.enqueueUniqueWork(
            ONE_TIME_SYNC_WORK,
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    /**
     * 取消同步
     */
    override suspend fun cancelSync() {
        workManager.cancelUniqueWork(ONE_TIME_SYNC_WORK)
    }
}
