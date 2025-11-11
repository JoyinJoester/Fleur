package takagi.ru.fleur.util

import android.util.Log
import takagi.ru.fleur.domain.model.FleurError
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * 错误处理工具
 * 将异常转换为 FleurError
 */
object ErrorHandler {
    
    private const val TAG = "ErrorHandler"
    
    /**
     * 将异常转换为 FleurError
     */
    fun handleException(exception: Throwable): FleurError {
        // 记录错误日志
        Log.e(TAG, "Error occurred", exception)
        
        return when (exception) {
            is FleurError -> exception
            
            is UnknownHostException -> FleurError.NetworkError(
                errorMessage = "无法连接到服务器",
                errorCause = exception
            )
            
            is SocketTimeoutException -> FleurError.NetworkError(
                errorMessage = "连接超时",
                errorCause = exception
            )
            
            is SSLException -> FleurError.NetworkError(
                errorMessage = "安全连接失败",
                errorCause = exception
            )
            
            is IOException -> FleurError.NetworkError(
                errorMessage = "网络连接失败",
                errorCause = exception
            )
            
            is SecurityException -> FleurError.AuthenticationError(
                errorMessage = "权限不足"
            )
            
            else -> FleurError.UnknownError(
                errorMessage = exception.message ?: "未知错误",
                errorCause = exception
            )
        }
    }
    
    /**
     * 记录同步错误（后台日志）
     */
    fun logSyncError(accountId: String, error: Throwable) {
        Log.e(TAG, "Sync error for account $accountId", error)
        // 这里可以添加更多的错误追踪逻辑，如上报到分析服务
    }
    
    /**
     * 检查是否需要重试
     */
    fun shouldRetry(error: FleurError): Boolean {
        return when (error) {
            is FleurError.NetworkError -> true
            // TODO: 添加 ServerError 类型支持
            else -> false
        }
    }
    
    /**
     * 获取重试延迟时间（毫秒）
     */
    fun getRetryDelay(attemptCount: Int): Long {
        // 指数退避：1s, 2s, 4s, 8s...
        return (1000L * Math.pow(2.0, attemptCount.toDouble())).toLong()
            .coerceAtMost(30000L) // 最大 30 秒
    }
}

/**
 * Result 扩展函数：处理错误
 */
fun <T> Result<T>.handleError(): Result<T> {
    return this.onFailure { exception ->
        ErrorHandler.handleException(exception)
    }
}

/**
 * Result 扩展函数：映射错误
 */
fun <T> Result<T>.mapError(): Result<T> {
    return this.recoverCatching { exception ->
        throw ErrorHandler.handleException(exception)
    }
}
