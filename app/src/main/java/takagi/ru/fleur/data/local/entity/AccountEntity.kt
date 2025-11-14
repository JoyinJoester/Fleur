package takagi.ru.fleur.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 账户数据库实体
 * 存储邮件账户信息，包括 IMAP 和 SMTP 服务器配置
 */
@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["email"]),
        Index(value = ["is_default"])
    ]
)
data class AccountEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "display_name")
    val displayName: String,
    
    @ColumnInfo(name = "color")
    val color: Int, // ARGB color value
    
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean,
    
    // IMAP 配置
    @ColumnInfo(name = "imap_host")
    val imapHost: String,
    
    @ColumnInfo(name = "imap_port")
    val imapPort: Int,
    
    @ColumnInfo(name = "imap_use_ssl")
    val imapUseSsl: Boolean,
    
    @ColumnInfo(name = "imap_username")
    val imapUsername: String,
    
    // SMTP 配置
    @ColumnInfo(name = "smtp_host")
    val smtpHost: String,
    
    @ColumnInfo(name = "smtp_port")
    val smtpPort: Int,
    
    @ColumnInfo(name = "smtp_use_ssl")
    val smtpUseSsl: Boolean,
    
    @ColumnInfo(name = "smtp_username")
    val smtpUsername: String,
    
    // 时间戳
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
