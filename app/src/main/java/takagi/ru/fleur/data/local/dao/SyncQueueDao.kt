package takagi.ru.fleur.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.data.local.entity.SyncOperation

/**
 * 同步队列 DAO
 * 提供同步队列的数据库操作
 */
@Dao
interface SyncQueueDao {
    
    /**
     * 获取所有待同步操作
     * 按时间戳升序排序，确保先进先出
     */
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    fun getAllSyncOperations(): Flow<List<SyncOperation>>
    
    /**
     * 获取所有待同步操作（非 Flow）
     * 用于同步处理
     */
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    suspend fun getAllSyncOperationsList(): List<SyncOperation>
    
    /**
     * 根据 ID 获取同步操作
     */
    @Query("SELECT * FROM sync_queue WHERE id = :operationId")
    suspend fun getSyncOperationById(operationId: Long): SyncOperation?
    
    /**
     * 获取指定邮件的待同步操作
     */
    @Query("SELECT * FROM sync_queue WHERE email_id = :emailId ORDER BY timestamp ASC")
    suspend fun getSyncOperationsByEmailId(emailId: String): List<SyncOperation>
    
    /**
     * 插入同步操作
     * @return 插入的操作 ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncOperation(operation: SyncOperation): Long
    
    /**
     * 批量插入同步操作
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncOperations(operations: List<SyncOperation>)
    
    /**
     * 更新同步操作
     */
    @Update
    suspend fun updateSyncOperation(operation: SyncOperation)
    
    /**
     * 删除同步操作
     */
    @Delete
    suspend fun deleteSyncOperation(operation: SyncOperation)
    
    /**
     * 根据 ID 删除同步操作
     */
    @Query("DELETE FROM sync_queue WHERE id = :operationId")
    suspend fun deleteSyncOperationById(operationId: Long)
    
    /**
     * 批量删除同步操作
     */
    @Query("DELETE FROM sync_queue WHERE id IN (:operationIds)")
    suspend fun deleteSyncOperationsByIds(operationIds: List<Long>)
    
    /**
     * 删除指定邮件的所有同步操作
     */
    @Query("DELETE FROM sync_queue WHERE email_id = :emailId")
    suspend fun deleteSyncOperationsByEmailId(emailId: String)
    
    /**
     * 增加重试次数并更新错误信息
     */
    @Query("UPDATE sync_queue SET retry_count = retry_count + 1, last_error = :error WHERE id = :operationId")
    suspend fun incrementRetryCount(operationId: Long, error: String)
    
    /**
     * 获取待同步操作数量
     */
    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun getPendingCount(): Int
    
    /**
     * 获取待同步操作数量（Flow）
     * 用于 UI 实时显示
     */
    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getPendingCountFlow(): Flow<Int>
    
    /**
     * 获取失败次数超过阈值的操作
     * @param maxRetries 最大重试次数
     */
    @Query("SELECT * FROM sync_queue WHERE retry_count >= :maxRetries ORDER BY timestamp ASC")
    suspend fun getFailedOperations(maxRetries: Int): List<SyncOperation>
    
    /**
     * 清空同步队列
     */
    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
    
    /**
     * 删除指定时间之前的操作
     * 用于清理过期的失败操作
     */
    @Query("DELETE FROM sync_queue WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOperationsBefore(beforeTimestamp: Long)
}
