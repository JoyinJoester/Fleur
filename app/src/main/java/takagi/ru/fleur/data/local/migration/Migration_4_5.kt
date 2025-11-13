package takagi.ru.fleur.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移:版本 4 -> 5
 * 添加 contacts 表用于存储用户保存的联系人
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建 contacts 表
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS contacts (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                email TEXT NOT NULL,
                phone_number TEXT,
                organization TEXT,
                job_title TEXT,
                address TEXT,
                notes TEXT,
                avatar_url TEXT,
                is_favorite INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        
        // 为 email 创建唯一索引(防止重复邮箱)
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_contacts_email ON contacts(email)"
        )
        
        // 为常用查询字段创建索引
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_contacts_name ON contacts(name)"
        )
        
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_contacts_is_favorite ON contacts(is_favorite)"
        )
        
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_contacts_created_at ON contacts(created_at)"
        )
    }
}
