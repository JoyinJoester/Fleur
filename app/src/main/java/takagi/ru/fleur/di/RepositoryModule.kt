package takagi.ru.fleur.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import takagi.ru.fleur.data.repository.EmailRepositoryImpl
import takagi.ru.fleur.data.repository.PreferencesRepositoryImpl
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.domain.repository.PreferencesRepository
import javax.inject.Singleton

/**
 * Repository 模块
 * 提供 Repository 实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindEmailRepository(
        impl: EmailRepositoryImpl
    ): EmailRepository
    
    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        impl: takagi.ru.fleur.data.repository.AccountRepositoryImpl
    ): takagi.ru.fleur.domain.repository.AccountRepository
    
    @Binds
    @Singleton
    abstract fun bindSyncStatusRepository(
        impl: takagi.ru.fleur.data.repository.SyncStatusRepositoryImpl
    ): takagi.ru.fleur.domain.repository.SyncStatusRepository
    
    companion object {
        @Provides
        @Singleton
        fun providePreferencesRepository(
            @ApplicationContext context: Context
        ): PreferencesRepository {
            return PreferencesRepositoryImpl(context)
        }
    }
}
