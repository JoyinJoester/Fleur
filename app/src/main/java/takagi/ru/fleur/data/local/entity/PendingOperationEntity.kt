package takagi.ru.fleur.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 待处理操作实体
 * 用于离线操作队列
 * 
 * 索引优化：
 * - timestamp: 按时间顺序处理
 */
@Entity(
    tableName = "pending_operations",
    indices = [
        Index(value = ["timestamp"])
    ]
)
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "operation_type")
    val operationType: String, // SEND_EMAIL, DELETE_EMAIL, ARCHIVE_EMAIL, MARK_READ
    
    @ColumnInfo(name = "email_id")
    val emailId: String?,
    
    @ColumnInfo(name = "account_id")
    val accountId: String,
    
    @ColumnInfo(name = "data")
    val data: String?, // JSON 格式的操作数据
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    
    @ColumnInfo(name = "last_error")
    val lastError: String?
)
