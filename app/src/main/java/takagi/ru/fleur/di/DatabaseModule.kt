package takagi.ru.fleur.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import takagi.ru.fleur.data.local.FleurDatabase
import takagi.ru.fleur.data.local.dao.AccountDao
import takagi.ru.fleur.data.local.dao.AttachmentDao
import takagi.ru.fleur.data.local.dao.EmailDao
import takagi.ru.fleur.data.local.dao.PendingOperationDao
import takagi.ru.fleur.data.local.dao.SyncQueueDao
import takagi.ru.fleur.data.local.migration.MIGRATION_2_3
import takagi.ru.fleur.data.local.migration.MIGRATION_3_4
import javax.inject.Singleton

/**
 * 数据库模块
 * 提供 Room 数据库和 DAO 实例
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
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
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4) // 添加迁移策略
            .fallbackToDestructiveMigration() // 开发阶段使用，如果迁移失败则重建
            .build()
    }
    
    @Provides
    @Singleton
    fun provideEmailDao(database: FleurDatabase): EmailDao {
        return database.emailDao()
    }
    
    @Provides
    @Singleton
    fun provideAccountDao(database: FleurDatabase): AccountDao {
        return database.accountDao()
    }
    
    @Provides
    @Singleton
    fun provideAttachmentDao(database: FleurDatabase): AttachmentDao {
        return database.attachmentDao()
    }
    
    @Provides
    @Singleton
    fun providePendingOperationDao(database: FleurDatabase): PendingOperationDao {
        return database.pendingOperationDao()
    }
    
    @Provides
    @Singleton
    fun provideSyncQueueDao(database: FleurDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }
}
