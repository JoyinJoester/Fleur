package takagi.ru.fleur.domain.model

/**
 * WebDAV 配置模型
 * @property serverUrl 服务器地址
 * @property port 端口号
 * @property username 用户名
 * @property useSsl 是否使用SSL/TLS
 * @property calendarPath 日历路径（可选）
 * @property contactsPath 联系人路径（可选）
 */
data class WebDAVConfig(
    val serverUrl: String,
    val port: Int,
    val username: String,
    val useSsl: Boolean = true,
    val calendarPath: String? = null,
    val contactsPath: String? = null
) {
    /**
     * 获取完整的服务器URL
     */
    fun fullUrl(): String {
        val protocol = if (useSsl) "https" else "http"
        return "$protocol://$serverUrl:$port"
    }
}
