package takagi.ru.fleur.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 同步操作实体
 * 用于本地优先架构的 WebDAV 同步队列
 * 
 * 索引优化：
 * - timestamp: 按时间顺序处理同步操作
 * - email_id: 快速查找特定邮件的待同步操作
 */
@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["email_id"])
    ]
)
data class SyncOperation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    /**
     * 操作类型
     */
    @ColumnInfo(name = "operation_type")
    val operationType: OperationType,
    
    /**
     * 邮件 ID
     */
    @ColumnInfo(name = "email_id")
    val emailId: String,
    
    /**
     * 操作时间戳（毫秒）
     */
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    /**
     * 重试次数
     */
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    
    /**
     * 最后一次错误信息
     */
    @ColumnInfo(name = "last_error")
    val lastError: String? = null,
    
    /**
     * 额外数据（JSON 格式）
     * 用于存储操作相关的额外信息，如移动到的文件夹名称等
     */
    @ColumnInfo(name = "extra_data")
    val extraData: String? = null
)
