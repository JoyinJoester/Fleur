package takagi.ru.fleur.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 账户数据库实体
 */
@Entity(tableName = "accounts")
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
    
    // WebDAV 配置
    @ColumnInfo(name = "server_url")
    val serverUrl: String,
    
    @ColumnInfo(name = "port")
    val port: Int,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "use_ssl")
    val useSsl: Boolean,
    
    @ColumnInfo(name = "calendar_path")
    val calendarPath: String?,
    
    @ColumnInfo(name = "contacts_path")
    val contactsPath: String?
)
