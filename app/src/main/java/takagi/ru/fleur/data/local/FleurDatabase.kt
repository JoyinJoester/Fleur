package takagi.ru.fleur.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import takagi.ru.fleur.data.local.dao.AccountDao
import takagi.ru.fleur.data.local.dao.AttachmentDao
import takagi.ru.fleur.data.local.dao.EmailDao
import takagi.ru.fleur.data.local.dao.PendingOperationDao
import takagi.ru.fleur.data.local.entity.AccountEntity
import takagi.ru.fleur.data.local.entity.AttachmentEntity
import takagi.ru.fleur.data.local.entity.EmailEntity
import takagi.ru.fleur.data.local.entity.PendingOperationEntity

/**
 * Fleur 数据库
 * 
 * 版本: 3 (添加 Markdown 支持)
 * 实体: EmailEntity, AccountEntity, AttachmentEntity, PendingOperationEntity
 * 
 * 版本历史:
 * - v1: 初始版本
 * - v2: 修复 FleurError 架构
 * - v3: 添加 body_markdown 和 content_type 字段
 */
@Database(
    entities = [
        EmailEntity::class,
        AccountEntity::class,
        AttachmentEntity::class,
        PendingOperationEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class FleurDatabase : RoomDatabase() {
    
    abstract fun emailDao(): EmailDao
    abstract fun accountDao(): AccountDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun pendingOperationDao(): PendingOperationDao
    
    companion object {
        const val DATABASE_NAME = "fleur_database"
    }
}
