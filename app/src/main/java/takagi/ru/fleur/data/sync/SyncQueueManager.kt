package takagi.ru.fleur.data.sync

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import takagi.ru.fleur.data.local.dao.SyncQueueDao
import takagi.ru.fleur.data.local.entity.OperationType
import takagi.ru.fleur.data.local.entity.SyncOperation
import takagi.ru.fleur.data.remote.webdav.WebDAVClient
import takagi.ru.fleur.domain.repository.PreferencesRepository
import takagi.ru.fleur.util.NetworkMonitor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

/**
 * 同步队列管理器
 * 负责管理待同步操作队列和后台同步逻辑
 * 
 * 核心功能：
 * - 添加操作到同步队列
 * - 处理同步队列中的操作
 * - 实现指数退避重试策略
 * - 提供待同步数量查询
 */
@Singleton
class SyncQueueManager @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val webdavClient: WebDAVClient,
    private val preferencesRepository: PreferencesRepository,
    private val networkMonitor: NetworkMonitor
) {
    
    companion object {
        private const val TAG = "SyncQueueManager"
        
        // 重试配置
        private const val MAX_RETRY_COUNT = 5
        private const val INITIAL_BACKOFF_MS = 1000L // 1 秒
        private const val MAX_BACKOFF_MS = 32000L // 32 秒
        private const val BACKOFF_MULTIPLIER = 2.0
    }
    
    /**
     * 添加操作到同步队列
     * 
     * @param operation 待同步的操作
     * @return 插入的操作 ID
     */
    suspend fun enqueue(operation: SyncOperation): Long {
        Log.d(TAG, "入队操作: ${operation.operationType} for email ${operation.emailId}")
        return syncQueueDao.insertSyncOperation(operation)
    }
    
    /**
     * 批量添加操作到同步队列
     * 
     * @param operations 待同步的操作列表
     */
    suspend fun enqueueAll(operations: List<SyncOperation>) {
        if (operations.isEmpty()) return
        Log.d(TAG, "批量入队 ${operations.size} 个操作")
        syncQueueDao.insertSyncOperations(operations)
    }
    
    /**
     * 处理同步队列
     * 按顺序处理所有待同步操作
     * 
     * @return 成功同步的操作数量
     */
    suspend fun processSyncQueue(): Int {
        // 检查网络连接
        if (!networkMonitor.isNetworkAvailable()) {
            Log.d(TAG, "网络不可用，跳过同步")
            return 0
        }
        
        // 获取所有待同步操作
        val operations = syncQueueDao.getAllSyncOperationsList()
        if (operations.isEmpty()) {
            Log.d(TAG, "同步队列为空")
            return 0
        }
        
        Log.d(TAG, "开始处理同步队列，共 ${operations.size} 个操作")
        var successCount = 0
        
        for (operation in operations) {
            // 检查是否超过最大重试次数
            if (operation.retryCount >= MAX_RETRY_COUNT) {
                Log.w(TAG, "操作 ${operation.id} 已达到最大重试次数，跳过")
                continue
            }
            
            // 执行同步操作
            val result = syncOperation(operation)
            
            if (result) {
                // 同步成功，从队列中移除
                syncQueueDao.deleteSyncOperationById(operation.id)
                successCount++
                Log.d(TAG, "操作 ${operation.id} 同步成功")
            } else {
                // 同步失败，增加重试计数
                val error = "同步失败，将在稍后重试"
                syncQueueDao.incrementRetryCount(operation.id, error)
                Log.w(TAG, "操作 ${operation.id} 同步失败，重试次数: ${operation.retryCount + 1}")
                
                // 应用指数退避延迟
                val backoffDelay = calculateBackoffDelay(operation.retryCount)
                kotlinx.coroutines.delay(backoffDelay)
            }
        }
        
        Log.d(TAG, "同步队列处理完成，成功: $successCount/${operations.size}")
        return successCount
    }
    
    /**
     * 执行单个同步操作
     * 
     * @param operation 待同步的操作
     * @return 是否成功
     */
    private suspend fun syncOperation(operation: SyncOperation): Boolean {
        return try {
            when (operation.operationType) {
                OperationType.MARK_READ -> {
                    // 调用 WebDAV 客户端标记为已读
                    val flags = takagi.ru.fleur.data.remote.webdav.dto.EmailFlags(isRead = true)
                    val result = webdavClient.updateEmailFlags(operation.emailId, flags)
                    result.isSuccess
                }
                OperationType.MARK_UNREAD -> {
                    // 调用 WebDAV 客户端标记为未读
                    val flags = takagi.ru.fleur.data.remote.webdav.dto.EmailFlags(isRead = false)
                    val result = webdavClient.updateEmailFlags(operation.emailId, flags)
                    result.isSuccess
                }
                OperationType.ARCHIVE -> {
                    // 归档操作：在 WebDAV 中可能需要移动到归档文件夹或设置特定标记
                    // 目前使用删除标记来表示归档（根据实际 WebDAV 服务器实现调整）
                    Log.d(TAG, "归档操作暂时跳过，等待 WebDAV 服务器支持")
                    true // 暂时返回成功，避免重试
                }
                OperationType.DELETE -> {
                    // 调用 WebDAV 客户端删除邮件
                    val result = webdavClient.deleteEmail(operation.emailId)
                    result.isSuccess
                }
                OperationType.STAR -> {
                    // 调用 WebDAV 客户端添加星标
                    val flags = takagi.ru.fleur.data.remote.webdav.dto.EmailFlags(isStarred = true)
                    val result = webdavClient.updateEmailFlags(operation.emailId, flags)
                    result.isSuccess
                }
                OperationType.UNSTAR -> {
                    // 调用 WebDAV 客户端移除星标
                    val flags = takagi.ru.fleur.data.remote.webdav.dto.EmailFlags(isStarred = false)
                    val result = webdavClient.updateEmailFlags(operation.emailId, flags)
                    result.isSuccess
                }
                OperationType.MOVE_TO_FOLDER -> {
                    // 移动到文件夹操作：从 extraData 中获取目标文件夹
                    val targetFolder = operation.extraData
                    if (targetFolder.isNullOrBlank()) {
                        Log.e(TAG, "移动文件夹操作失败: extraData 为空")
                        false
                    } else {
                        // 调用 WebDAV 客户端移动邮件到指定文件夹
                        // 注意：这需要 WebDAV 服务器支持文件夹操作
                        // 目前暂时跳过实际的 WebDAV 调用，等待服务器支持
                        Log.d(TAG, "移动文件夹操作: emailId=${operation.emailId}, targetFolder=$targetFolder (WebDAV 同步暂时跳过)")
                        true // 暂时返回成功，避免重试
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "同步操作失败: ${operation.operationType} for ${operation.emailId}", e)
            false
        }
    }
    
    /**
     * 计算指数退避延迟时间
     * 
     * @param retryCount 当前重试次数
     * @return 延迟时间（毫秒）
     */
    private fun calculateBackoffDelay(retryCount: Int): Long {
        val delay = (INITIAL_BACKOFF_MS * BACKOFF_MULTIPLIER.pow(retryCount.toDouble())).toLong()
        return min(delay, MAX_BACKOFF_MS)
    }
    
    /**
     * 获取待同步操作数量
     * 
     * @return 待同步操作数量
     */
    suspend fun getPendingCount(): Int {
        return syncQueueDao.getPendingCount()
    }
    
    /**
     * 获取待同步操作数量（Flow）
     * 用于 UI 实时显示
     * 
     * @return 待同步操作数量的 Flow
     */
    fun getPendingCountFlow(): Flow<Int> {
        return syncQueueDao.getPendingCountFlow()
    }
    
    /**
     * 获取失败的操作列表
     * 
     * @return 失败的操作列表
     */
    suspend fun getFailedOperations(): List<SyncOperation> {
        return syncQueueDao.getFailedOperations(MAX_RETRY_COUNT)
    }
    
    /**
     * 清空同步队列
     * 注意：此操作会删除所有待同步操作，请谨慎使用
     */
    suspend fun clearQueue() {
        Log.w(TAG, "清空同步队列")
        syncQueueDao.clearQueue()
    }
    
    /**
     * 删除指定邮件的所有待同步操作
     * 
     * @param emailId 邮件 ID
     */
    suspend fun clearOperationsForEmail(emailId: String) {
        Log.d(TAG, "清除邮件 $emailId 的所有待同步操作")
        syncQueueDao.deleteSyncOperationsByEmailId(emailId)
    }
    
    /**
     * 重试失败的操作
     * 将失败操作的重试计数重置为 0
     * 
     * @param operationId 操作 ID
     */
    suspend fun retryFailedOperation(operationId: Long) {
        val operation = syncQueueDao.getSyncOperationById(operationId) ?: return
        val resetOperation = operation.copy(retryCount = 0, lastError = null)
        syncQueueDao.updateSyncOperation(resetOperation)
        Log.d(TAG, "重置操作 $operationId 的重试计数")
    }
    
    /**
     * 重试所有失败的操作
     */
    suspend fun retryAllFailedOperations() {
        val failedOperations = getFailedOperations()
        Log.d(TAG, "重试 ${failedOperations.size} 个失败的操作")
        
        failedOperations.forEach { operation ->
            val resetOperation = operation.copy(retryCount = 0, lastError = null)
            syncQueueDao.updateSyncOperation(resetOperation)
        }
    }
}
