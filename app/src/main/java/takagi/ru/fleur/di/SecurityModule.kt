package takagi.ru.fleur.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import takagi.ru.fleur.data.security.SecureCredentialStorage
import javax.inject.Singleton

/**
 * 安全模块
 * 提供安全相关的依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideSecureCredentialStorage(
        @ApplicationContext context: Context
    ): SecureCredentialStorage {
        return SecureCredentialStorage(context)
    }
}
