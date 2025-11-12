package takagi.ru.fleur.util

import android.util.Log
import takagi.ru.fleur.domain.model.Email

/**
 * 邮件列表诊断日志工具
 * 用于记录邮件列表的状态和重复情况，帮助快速定位问题
 */
object EmailListLogger {
    
    private const val TAG = "EmailListLogger"
    
    /**
     * 记录重复邮件的日志
     * 
     * @param layer 调用层（如 "Repository", "ViewModel", "UI"）
     * @param originalCount 原始邮件数量
     * @param uniqueCount 去重后的邮件数量
     */
    fun logDuplicates(layer: String, originalCount: Int, uniqueCount: Int) {
        if (originalCount > uniqueCount) {
            val duplicateCount = originalCount - uniqueCount
            Log.w(TAG, "[$layer] 发现 $duplicateCount 个重复邮件 (原始: $originalCount, 去重后: $uniqueCount)")
        }
    }
    
    /**
     * 记录邮件列表的详细信息
     * 仅在 DEBUG 模式下记录详细信息
     * 
     * @param layer 调用层（如 "Repository", "ViewModel", "UI"）
     * @param emails 邮件列表
     */
    fun logEmailList(layer: String, emails: List<Email>) {
        Log.d(TAG, "[$layer] 邮件列表大小: ${emails.size}")
        
        // 仅在调试模式下记录详细信息（通过日志级别控制）
        if (Log.isLoggable(TAG, Log.DEBUG) && emails.isNotEmpty()) {
            // 检查是否有重复的ID
            val emailIds = emails.map { it.id }
            val duplicates = emailIds.groupingBy { it }.eachCount().filter { it.value > 1 }
            
            if (duplicates.isNotEmpty()) {
                Log.e(TAG, "[$layer] 发现重复的邮件ID:")
                duplicates.forEach { (id, count) ->
                    Log.e(TAG, "  - ID: $id, 出现次数: $count")
                }
            }
            
            // 记录前5封邮件的基本信息
            val previewCount = minOf(5, emails.size)
            Log.d(TAG, "[$layer] 前 $previewCount 封邮件:")
            emails.take(previewCount).forEachIndexed { index, email ->
                Log.d(TAG, "  [$index] ID: ${email.id}, 主题: ${email.subject}, 已读: ${email.isRead}")
            }
        }
    }
    
    /**
     * 记录邮件列表的统计信息
     * 
     * @param layer 调用层
     * @param emails 邮件列表
     */
    fun logEmailStats(layer: String, emails: List<Email>) {
        if (emails.isEmpty()) {
            Log.d(TAG, "[$layer] 邮件列表为空")
            return
        }
        
        val unreadCount = emails.count { !it.isRead }
        val starredCount = emails.count { it.isStarred }
        val withAttachments = emails.count { it.hasAttachments() }
        
        Log.d(TAG, "[$layer] 邮件统计:")
        Log.d(TAG, "  - 总数: ${emails.size}")
        Log.d(TAG, "  - 未读: $unreadCount")
        Log.d(TAG, "  - 星标: $starredCount")
        Log.d(TAG, "  - 带附件: $withAttachments")
    }
    
    /**
     * 记录邮件ID列表（用于调试）
     * 仅在调试模式下记录
     * 
     * @param layer 调用层
     * @param emailIds 邮件ID列表
     */
    fun logEmailIds(layer: String, emailIds: List<String>) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "[$layer] 邮件ID列表 (${emailIds.size} 个):")
            emailIds.forEachIndexed { index, id ->
                Log.d(TAG, "  [$index] $id")
            }
        }
    }
    
    /**
     * 记录错误信息
     * 
     * @param layer 调用层
     * @param message 错误消息
     * @param throwable 异常对象（可选）
     */
    fun logError(layer: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, "[$layer] $message", throwable)
        } else {
            Log.e(TAG, "[$layer] $message")
        }
    }
    
    /**
     * 记录警告信息
     * 
     * @param layer 调用层
     * @param message 警告消息
     */
    fun logWarning(layer: String, message: String) {
        Log.w(TAG, "[$layer] $message")
    }
    
    /**
     * 记录信息
     * 
     * @param layer 调用层
     * @param message 信息内容
     */
    fun logInfo(layer: String, message: String) {
        Log.i(TAG, "[$layer] $message")
    }
}
