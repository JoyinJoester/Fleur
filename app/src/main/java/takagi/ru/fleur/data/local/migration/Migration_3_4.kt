package takagi.ru.fleur.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移：v3 -> v4
 * 添加本地优先架构的同步队列表
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 创建 sync_queue 表
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sync_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                operation_type TEXT NOT NULL,
                email_id TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                retry_count INTEGER NOT NULL DEFAULT 0,
                last_error TEXT,
                extra_data TEXT
            )
            """.trimIndent()
        )
        
        // 创建索引以优化查询性能
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_sync_queue_timestamp 
            ON sync_queue(timestamp)
            """.trimIndent()
        )
        
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_sync_queue_email_id 
            ON sync_queue(email_id)
            """.trimIndent()
        )
    }
}
