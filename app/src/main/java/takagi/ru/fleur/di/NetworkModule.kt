package takagi.ru.fleur.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import takagi.ru.fleur.data.remote.webdav.WebDAVClient
import takagi.ru.fleur.data.remote.webdav.WebDAVClientImpl
import takagi.ru.fleur.data.remote.webdav.parser.WebDAVXmlParser
import takagi.ru.fleur.data.remote.webdav.parser.WebDAVXmlParserImpl
import javax.inject.Singleton

/**
 * 网络模块
 * 提供 WebDAV 客户端和相关依赖
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    
    @Binds
    @Singleton
    abstract fun bindWebDAVClient(
        impl: WebDAVClientImpl
    ): WebDAVClient
    
    @Binds
    @Singleton
    abstract fun bindWebDAVXmlParser(
        impl: WebDAVXmlParserImpl
    ): WebDAVXmlParser
}
