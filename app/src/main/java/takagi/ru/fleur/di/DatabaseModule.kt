package takagi.ru.fleur.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import takagi.ru.fleur.data.local.FleurDatabase
import takagi.ru.fleur.data.local.dao.*
import takagi.ru.fleur.data.local.migration.MIGRATION_2_3
import takagi.ru.fleur.data.local.migration.MIGRATION_3_4
import takagi.ru.fleur.data.local.migration.MIGRATION_4_5
import takagi.ru.fleur.data.local.migration.MIGRATION_5_6
import javax.inject.Singleton

/**
 * 数据库模块
 * 提供 Room 数据库和 DAO 实例
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * 提供 Fleur 数据库实例
     * 包含所有必要的迁移策略
     */
    @Provides
    @Singleton
    fun provideFleurDatabase(
        @ApplicationContext context: Context
    ): FleurDatabase {
        return Room.databaseBuilder(
            context,
            FleurDatabase::class.java,
            FleurDatabase.DATABASE_NAME
        )
            .addMigrations(
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6  // 账户管理 M3E 重新设计迁移
            )
            .fallbackToDestructiveMigration() // 开发阶段使用，生产环境应移除
            .build()
    }
    
    /**
     * 提供 EmailDao
     */
    @Provides
    fun provideEmailDao(database: FleurDatabase): EmailDao {
        return database.emailDao()
    }
    
    /**
     * 提供 AccountDao
     */
    @Provides
    fun provideAccountDao(database: FleurDatabase): AccountDao {
        return database.accountDao()
    }
    
    /**
     * 提供 AttachmentDao
     */
    @Provides
    fun provideAttachmentDao(database: FleurDatabase): AttachmentDao {
        return database.attachmentDao()
    }
    
    /**
     * 提供 PendingOperationDao
     */
    @Provides
    fun providePendingOperationDao(database: FleurDatabase): PendingOperationDao {
        return database.pendingOperationDao()
    }
    
    /**
     * 提供 SyncQueueDao
     */
    @Provides
    fun provideSyncQueueDao(database: FleurDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }
    
    /**
     * 提供 ContactDao
     */
    @Provides
    fun provideContactDao(database: FleurDatabase): ContactDao {
        return database.contactDao()
    }
}
