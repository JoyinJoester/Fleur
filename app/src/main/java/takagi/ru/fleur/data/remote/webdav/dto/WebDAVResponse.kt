package takagi.ru.fleur.data.remote.webdav.dto

/**
 * WebDAV 响应数据传输对象
 * 表示 WebDAV 服务器的响应
 */
data class WebDAVResponse(
    val statusCode: Int,
    val statusMessage: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
) {
    /**
     * 是否成功响应（2xx 状态码）
     */
    fun isSuccessful(): Boolean = statusCode in 200..299
    
    /**
     * 是否认证错误（401 状态码）
     */
    fun isAuthError(): Boolean = statusCode == 401
    
    /**
     * 是否未找到资源（404 状态码）
     */
    fun isNotFound(): Boolean = statusCode == 404
    
    /**
     * 是否服务器错误（5xx 状态码）
     */
    fun isServerError(): Boolean = statusCode in 500..599
}

/**
 * WebDAV 多状态响应
 * 用于处理 PROPFIND 等返回多个资源状态的请求
 */
data class WebDAVMultiStatusResponse(
    val responses: List<WebDAVResourceResponse>
)

/**
 * WebDAV 资源响应
 * 表示单个资源的状态和属性
 */
data class WebDAVResourceResponse(
    val href: String,
    val statusCode: Int,
    val properties: Map<String, String> = emptyMap()
) {
    /**
     * 是否成功响应
     */
    fun isSuccessful(): Boolean = statusCode in 200..299
}
