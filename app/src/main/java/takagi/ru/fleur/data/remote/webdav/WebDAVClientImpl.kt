package takagi.ru.fleur.data.remote.webdav

import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import takagi.ru.fleur.data.remote.webdav.dto.EmailDto
import takagi.ru.fleur.data.remote.webdav.dto.EmailFlags
import takagi.ru.fleur.data.remote.webdav.parser.WebDAVXmlParser
import takagi.ru.fleur.domain.model.WebDAVConfig
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.pow

/**
 * WebDAV 客户端实现
 * 使用 OkHttp 与 WebDAV 服务器通信
 */
class WebDAVClientImpl @Inject constructor(
    private val xmlParser: WebDAVXmlParser
) : WebDAVClient {
    
    private var client: OkHttpClient? = null
    private var config: WebDAVConfig? = null
    private var credentials: String? = null
    
    companion object {
        private const val CONNECT_TIMEOUT = 10L // 秒
        private const val READ_TIMEOUT = 30L // 秒
        private const val WRITE_TIMEOUT = 30L // 秒
        private const val MAX_CONNECTIONS = 5
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 1000L
        
        private val XML_MEDIA_TYPE = "application/xml; charset=utf-8".toMediaType()
    }
    
    /**
     * 连接到 WebDAV 服务器
     */
    override suspend fun connect(config: WebDAVConfig, password: String): Result<Unit> {
        return try {
            this.config = config
            this.credentials = Credentials.basic(config.username, password)
            
            // 创建 OkHttp 客户端
            client = createOkHttpClient(config)
            
            // 验证连接
            val testRequest = Request.Builder()
                .url("${config.fullUrl()}/")
                .header("Authorization", credentials!!)
                .method("OPTIONS", null)
                .build()
            
            val response = executeWithRetry { client!!.newCall(testRequest).execute() }
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("连接失败: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取邮件列表
     */
    override suspend fun fetchEmails(since: Instant?): Result<List<EmailDto>> {
        return try {
            requireConnected()
            
            // 构建 PROPFIND 请求获取邮件列表
            val propfindBody = buildPropfindRequest(since)
            val request = Request.Builder()
                .url("${config!!.fullUrl()}/mail/")
                .header("Authorization", credentials!!)
                .header("Depth", "1")
                .method("PROPFIND", propfindBody.toRequestBody(XML_MEDIA_TYPE))
                .build()
            
            val response = executeWithRetry { client!!.newCall(request).execute() }
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                xmlParser.parseEmailList(body)
            } else {
                Result.failure(IOException("获取邮件失败: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 发送邮件
     */
    override suspend fun sendEmail(email: EmailDto): Result<Unit> {
        return try {
            requireConnected()
            
            // 构建邮件 XML
            val emailXml = buildEmailXml(email)
            val request = Request.Builder()
                .url("${config!!.fullUrl()}/mail/sent/${email.id}.eml")
                .header("Authorization", credentials!!)
                .put(emailXml.toRequestBody(XML_MEDIA_TYPE))
                .build()
            
            val response = executeWithRetry { client!!.newCall(request).execute() }
            
            if (response.isSuccessful || response.code == 201) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("发送邮件失败: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除邮件
     */
    override suspend fun deleteEmail(emailId: String): Result<Unit> {
        return try {
            requireConnected()
            
            val request = Request.Builder()
                .url("${config!!.fullUrl()}/mail/$emailId.eml")
                .header("Authorization", credentials!!)
                .delete()
                .build()
            
            val response = executeWithRetry { client!!.newCall(request).execute() }
            
            if (response.isSuccessful || response.code == 204) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("删除邮件失败: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新邮件标记
     */
    override suspend fun updateEmailFlags(emailId: String, flags: EmailFlags): Result<Unit> {
        return try {
            requireConnected()
            
            // 构建 PROPPATCH 请求更新标记
            val proppatchBody = buildProppatchRequest(flags)
            val request = Request.Builder()
                .url("${config!!.fullUrl()}/mail/$emailId.eml")
                .header("Authorization", credentials!!)
                .method("PROPPATCH", proppatchBody.toRequestBody(XML_MEDIA_TYPE))
                .build()
            
            val response = executeWithRetry { client!!.newCall(request).execute() }
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("更新标记失败: ${response.code} ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建 OkHttp 客户端
     */
    private fun createOkHttpClient(config: WebDAVConfig): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectionPool(okhttp3.ConnectionPool(MAX_CONNECTIONS, 5, TimeUnit.MINUTES))
        
        // 配置 SSL/TLS
        if (config.useSsl) {
            try {
                // 创建信任所有证书的 TrustManager（生产环境应使用证书固定）
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                })
                
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                
                builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                // 如果 SSL 配置失败，使用默认配置
            }
        }
        
        return builder.build()
    }
    
    /**
     * 使用指数退避策略执行请求
     */
    private suspend fun executeWithRetry(block: () -> Response): Response {
        var lastException: Exception? = null
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                val response = block()
                // 如果是临时错误（5xx），则重试
                if (response.code in 500..599 && attempt < MAX_RETRIES - 1) {
                    val backoffMs = INITIAL_BACKOFF_MS * 2.0.pow(attempt).toLong()
                    delay(backoffMs)
                } else {
                    return response
                }
            } catch (e: IOException) {
                lastException = e
                if (attempt < MAX_RETRIES - 1) {
                    val backoffMs = INITIAL_BACKOFF_MS * 2.0.pow(attempt).toLong()
                    delay(backoffMs)
                }
            }
        }
        
        throw lastException ?: IOException("请求失败")
    }
    
    /**
     * 检查是否已连接
     */
    private fun requireConnected() {
        require(client != null && config != null && credentials != null) {
            "未连接到 WebDAV 服务器，请先调用 connect()"
        }
    }
    
    /**
     * 构建 PROPFIND 请求体
     */
    private fun buildPropfindRequest(since: Instant?): String {
        return """
            <?xml version="1.0" encoding="utf-8"?>
            <D:propfind xmlns:D="DAV:">
                <D:prop>
                    <D:displayname/>
                    <D:getcontentlength/>
                    <D:getlastmodified/>
                    <D:resourcetype/>
                </D:prop>
            </D:propfind>
        """.trimIndent()
    }
    
    /**
     * 构建邮件 XML
     */
    private fun buildEmailXml(email: EmailDto): String {
        val toAddresses = email.to.joinToString(", ") { 
            if (it.name != null) "${it.name} <${it.address}>" else it.address 
        }
        val ccAddresses = email.cc.joinToString(", ") { 
            if (it.name != null) "${it.name} <${it.address}>" else it.address 
        }
        
        return """
            From: ${email.from.name ?: ""} <${email.from.address}>
            To: $toAddresses
            ${if (email.cc.isNotEmpty()) "Cc: $ccAddresses" else ""}
            Subject: ${email.subject}
            Date: ${email.timestamp}
            Content-Type: ${if (email.bodyHtml != null) "text/html" else "text/plain"}; charset=utf-8
            
            ${email.bodyHtml ?: email.bodyPlain}
        """.trimIndent()
    }
    
    /**
     * 构建 PROPPATCH 请求体
     */
    private fun buildProppatchRequest(flags: EmailFlags): String {
        return """
            <?xml version="1.0" encoding="utf-8"?>
            <D:propertyupdate xmlns:D="DAV:" xmlns:M="urn:schemas:httpmail:">
                <D:set>
                    <D:prop>
                        <M:read>${flags.isRead}</M:read>
                        <M:flagged>${flags.isStarred}</M:flagged>
                        <M:answered>${flags.isAnswered}</M:answered>
                    </D:prop>
                </D:set>
            </D:propertyupdate>
        """.trimIndent()
    }
}
