package takagi.ru.fleur.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 邮件数据库实体
 * 
 * 索引优化：
 * - account_id: 按账户查询
 * - thread_id: 按线程查询
 * - timestamp: 按时间排序
 * - is_read: 按已读状态过滤
 */
@Entity(
    tableName = "emails",
    indices = [
        Index(value = ["account_id"]),
        Index(value = ["thread_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["is_read"])
    ]
)
data class EmailEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "thread_id")
    val threadId: String,
    
    @ColumnInfo(name = "account_id")
    val accountId: String,
    
    @ColumnInfo(name = "from_address")
    val fromAddress: String,
    
    @ColumnInfo(name = "from_name")
    val fromName: String?,
    
    @ColumnInfo(name = "to_addresses")
    val toAddresses: String, // JSON 格式
    
    @ColumnInfo(name = "cc_addresses")
    val ccAddresses: String?, // JSON 格式
    
    @ColumnInfo(name = "bcc_addresses")
    val bccAddresses: String?, // JSON 格式
    
    @ColumnInfo(name = "subject")
    val subject: String,
    
    @ColumnInfo(name = "body_preview")
    val bodyPreview: String,
    
    @ColumnInfo(name = "body_plain")
    val bodyPlain: String,
    
    @ColumnInfo(name = "body_html")
    val bodyHtml: String?,
    
    @ColumnInfo(name = "body_markdown")
    val bodyMarkdown: String?,
    
    @ColumnInfo(name = "content_type")
    val contentType: String = "text", // text, markdown, html
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long, // Unix timestamp in milliseconds
    
    @ColumnInfo(name = "is_read")
    val isRead: Boolean,
    
    @ColumnInfo(name = "is_starred")
    val isStarred: Boolean,
    
    @ColumnInfo(name = "labels")
    val labels: String? // JSON 格式
)
