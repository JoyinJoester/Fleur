package takagi.ru.fleur.domain.model

/**
 * Fleur 应用错误类型
 * 使用 sealed class 定义所有可能的错误类型
 */
sealed class FleurError(message: String, cause: Throwable? = null) : Throwable(message, cause) {
    /**
     * 网络错误
     * @property message 错误消息
     * @property cause 原始异常（可选）
     */
    data class NetworkError(
        val errorMessage: String,
        val errorCause: Throwable? = null
    ) : FleurError(errorMessage, errorCause)
    
    /**
     * 认证错误
     * @property message 错误消息
     */
    data class AuthenticationError(
        val errorMessage: String
    ) : FleurError(errorMessage)
    
    /**
     * 数据库错误
     * @property message 错误消息
     */
    data class DatabaseError(
        val errorMessage: String
    ) : FleurError(errorMessage)
    
    /**
     * 未找到错误
     * @property message 错误消息
     */
    data class NotFoundError(
        val errorMessage: String
    ) : FleurError(errorMessage)
    
    /**
     * 安全错误
     * @property message 错误消息
     */
    data class SecurityError(
        val errorMessage: String
    ) : FleurError(errorMessage)
    
    /**
     * 同步错误
     * @property message 失败原因
     * @property accountId 账户ID(可选)
     */
    data class SyncError(
        val errorMessage: String,
        val accountId: String? = null
    ) : FleurError(errorMessage)
    
    /**
     * 存储错误
     * @property availableSpace 可用空间(字节)
     * @property message 错误消息
     */
    data class StorageError(
        val availableSpace: Long,
        val errorMessage: String
    ) : FleurError(errorMessage)
    
    /**
     * 验证错误
     * @property field 字段名称
     * @property message 错误消息
     */
    data class ValidationError(
        val field: String,
        val errorMessage: String
    ) : FleurError(errorMessage)
    
    /**
     * 未知错误
     * @property message 错误消息
     * @property cause 原始异常(可选)
     */
    data class UnknownError(
        val errorMessage: String,
        val errorCause: Throwable? = null
    ) : FleurError(errorMessage, errorCause)
    
    /**
     * 获取用户友好的错误消息
     */
    fun getUserMessage(): String = when (this) {
        is NetworkError -> "网络连接失败: $errorMessage"
        is AuthenticationError -> "账户认证失败: $errorMessage"
        is DatabaseError -> "数据库错误: $errorMessage"
        is NotFoundError -> "未找到: $errorMessage"
        is SecurityError -> "安全错误: $errorMessage"
        is SyncError -> "邮件同步失败: $errorMessage"
        is StorageError -> "存储空间不足 (剩余 ${formatBytes(availableSpace)})"
        is ValidationError -> "$field: $errorMessage"
        is UnknownError -> "发生未知错误: $errorMessage"
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
