package takagi.ru.fleur.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移：v2 -> v3
 * 添加 Markdown 支持字段
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 添加 body_markdown 字段
        database.execSQL(
            """
            ALTER TABLE emails 
            ADD COLUMN body_markdown TEXT
            """.trimIndent()
        )
        
        // 添加 content_type 字段，默认值为 'text'
        database.execSQL(
            """
            ALTER TABLE emails 
            ADD COLUMN content_type TEXT NOT NULL DEFAULT 'text'
            """.trimIndent()
        )
    }
}
