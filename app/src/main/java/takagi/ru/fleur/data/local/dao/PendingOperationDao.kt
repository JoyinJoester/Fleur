package takagi.ru.fleur.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.data.local.entity.PendingOperationEntity

/**
 * 待处理操作 DAO
 * 提供离线操作队列的数据库操作
 */
@Dao
interface PendingOperationDao {
    
    /**
     * 获取所有待处理操作
     * 按时间顺序排序
     */
    @Query("SELECT * FROM pending_operations ORDER BY timestamp ASC")
    fun getAllPendingOperations(): Flow<List<PendingOperationEntity>>
    
    /**
     * 获取指定账户的待处理操作
     */
    @Query("SELECT * FROM pending_operations WHERE account_id = :accountId ORDER BY timestamp ASC")
    fun getPendingOperationsByAccount(accountId: String): Flow<List<PendingOperationEntity>>
    
    /**
     * 根据ID获取待处理操作
     */
    @Query("SELECT * FROM pending_operations WHERE id = :operationId")
    suspend fun getPendingOperationById(operationId: Long): PendingOperationEntity?
    
    /**
     * 插入待处理操作
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingOperation(operation: PendingOperationEntity): Long
    
    /**
     * 更新待处理操作
     */
    @Update
    suspend fun updatePendingOperation(operation: PendingOperationEntity)
    
    /**
     * 删除待处理操作
     */
    @Delete
    suspend fun deletePendingOperation(operation: PendingOperationEntity)
    
    /**
     * 根据ID删除待处理操作
     */
    @Query("DELETE FROM pending_operations WHERE id = :operationId")
    suspend fun deletePendingOperationById(operationId: Long)
    
    /**
     * 删除指定账户的所有待处理操作
     */
    @Query("DELETE FROM pending_operations WHERE account_id = :accountId")
    suspend fun deletePendingOperationsByAccount(accountId: String)
    
    /**
     * 增加重试次数
     */
    @Query("UPDATE pending_operations SET retry_count = retry_count + 1, last_error = :error WHERE id = :operationId")
    suspend fun incrementRetryCount(operationId: Long, error: String)
    
    /**
     * 获取待处理操作数量
     */
    @Query("SELECT COUNT(*) FROM pending_operations")
    suspend fun getPendingOperationCount(): Int
    
    /**
     * 清空所有待处理操作
     */
    @Query("DELETE FROM pending_operations")
    suspend fun clearAllPendingOperations()
}
