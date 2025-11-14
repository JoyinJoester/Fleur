package takagi.ru.fleur.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import takagi.ru.fleur.data.local.dao.AccountDao
import takagi.ru.fleur.data.local.dao.AttachmentDao
import takagi.ru.fleur.data.local.dao.ContactDao
import takagi.ru.fleur.data.local.dao.EmailDao
import takagi.ru.fleur.data.local.dao.PendingOperationDao
import takagi.ru.fleur.data.local.dao.SyncQueueDao
import takagi.ru.fleur.data.local.entity.AccountEntity
import takagi.ru.fleur.data.local.entity.AttachmentEntity
import takagi.ru.fleur.data.local.entity.ContactEntity
import takagi.ru.fleur.data.local.entity.EmailEntity
import takagi.ru.fleur.data.local.entity.PendingOperationEntity
import takagi.ru.fleur.data.local.entity.SyncOperation

/**
 * Fleur 数据库
 * 
 * 版本: 6 (账户管理 M3E 重新设计)
 * 实体: EmailEntity, AccountEntity, AttachmentEntity, PendingOperationEntity, SyncOperation, ContactEntity
 * 
 * 版本历史:
 * - v1: 初始版本
 * - v2: 修复 FleurError 架构
 * - v3: 添加 body_markdown 和 content_type 字段
 * - v4: 添加 SyncOperation 实体用于本地优先架构
 * - v5: 添加 ContactEntity 用于存储用户保存的联系人
 * - v6: 账户表重构 - 移除 WebDAV 配置，添加 IMAP/SMTP 配置和时间戳
 */
@Database(
    entities = [
        EmailEntity::class,
        AccountEntity::class,
        AttachmentEntity::class,
        PendingOperationEntity::class,
        SyncOperation::class,
        ContactEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class FleurDatabase : RoomDatabase() {
    
    abstract fun emailDao(): EmailDao
    abstract fun accountDao(): AccountDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun pendingOperationDao(): PendingOperationDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun contactDao(): ContactDao
    
    companion object {
        const val DATABASE_NAME = "fleur_database"
    }
}
