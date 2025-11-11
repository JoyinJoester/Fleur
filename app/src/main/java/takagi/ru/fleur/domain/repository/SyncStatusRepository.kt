package takagi.ru.fleur.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 同步状态仓库接口
 * 监听后台同步任务的状态
 */
interface SyncStatusRepository {
    
    /**
     * 观察同步状态
     * @return Flow<Boolean> true 表示正在同步，false 表示未同步
     */
    fun observeSyncStatus(): Flow<Boolean>
    
    /**
     * 触发立即同步
     */
    suspend fun triggerSync()
    
    /**
     * 取消同步
     */
    suspend fun cancelSync()
}
