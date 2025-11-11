package takagi.ru.fleur.data.offline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.google.gson.Gson
import takagi.ru.fleur.data.local.dao.PendingOperationDao
import takagi.ru.fleur.data.local.entity.PendingOperationEntity
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.util.NetworkMonitor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 离线操作管理器
 * 管理离线操作队列的执行
 */
@Singleton
class OfflineOperationManager @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val emailRepository: EmailRepository,
    private val networkMonitor: NetworkMonitor
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()
    
    companion object {
        const val OPERATION_SEND_EMAIL = "SEND_EMAIL"
        const val OPERATION_DELETE_EMAIL = "DELETE_EMAIL"
        const val OPERATION_ARCHIVE_EMAIL = "ARCHIVE_EMAIL"
        const val OPERATION_MARK_READ = "MARK_READ"
        
        private const val MAX_RETRY_COUNT = 3
        private const val RETRY_DELAY_MS = 10000L // 10 秒
    }
    
    init {
        // 监听网络状态，网络恢复时执行队列
        scope.launch {
            networkMonitor.observeNetworkStatus().collect { isConnected ->
                if (isConnected) {
                    processQueue()
                }
            }
        }
    }
    
    /**
     * 添加发送邮件操作到队列
     */
    suspend fun queueSendEmail(email: Email) {
        val operation = PendingOperationEntity(
            operationType = OPERATION_SEND_EMAIL,
            emailId = email.id,
            accountId = email.accountId,
            data = gson.toJson(email),
            timestamp = System.currentTimeMillis(),
            retryCount = 0,
            lastError = null
        )
        pendingOperationDao.insertPendingOperation(operation)
    }
    
    /**
     * 添加删除邮件操作到队列
     */
    suspend fun queueDeleteEmail(emailId: String, accountId: String) {
        val operation = PendingOperationEntity(
            operationType = OPERATION_DELETE_EMAIL,
            emailId = emailId,
            accountId = accountId,
            data = null,
            timestamp = System.currentTimeMillis(),
            retryCount = 0,
            lastError = null
        )
        pendingOperationDao.insertPendingOperation(operation)
    }
    
    /**
     * 添加归档邮件操作到队列
     */
    suspend fun queueArchiveEmail(emailId: String, accountId: String) {
        val operation = PendingOperationEntity(
            operationType = OPERATION_ARCHIVE_EMAIL,
            emailId = emailId,
            accountId = accountId,
            data = null,
            timestamp = System.currentTimeMillis(),
            retryCount = 0,
            lastError = null
        )
        pendingOperationDao.insertPendingOperation(operation)
    }
    
    /**
     * 添加标记已读操作到队列
     */
    suspend fun queueMarkAsRead(emailId: String, accountId: String, isRead: Boolean) {
        val operation = PendingOperationEntity(
            operationType = OPERATION_MARK_READ,
            emailId = emailId,
            accountId = accountId,
            data = gson.toJson(isRead),
            timestamp = System.currentTimeMillis(),
            retryCount = 0,
            lastError = null
        )
        pendingOperationDao.insertPendingOperation(operation)
    }
    
    /**
     * 处理队列中的所有操作
     */
    suspend fun processQueue() {
        if (!networkMonitor.isNetworkAvailable()) {
            return
        }
        
        // 等待 10 秒后开始处理（需求 11.5）
        delay(RETRY_DELAY_MS)
        
        val operations = pendingOperationDao.getAllPendingOperations().first()
        
        operations.forEach { operation ->
            if (operation.retryCount >= MAX_RETRY_COUNT) {
                // 超过最大重试次数，删除操作
                pendingOperationDao.deletePendingOperation(operation)
                return@forEach
            }
            
            try {
                when (operation.operationType) {
                    OPERATION_SEND_EMAIL -> processSendEmail(operation)
                    OPERATION_DELETE_EMAIL -> processDeleteEmail(operation)
                    OPERATION_ARCHIVE_EMAIL -> processArchiveEmail(operation)
                    OPERATION_MARK_READ -> processMarkAsRead(operation)
                }
                
                // 操作成功，删除队列项
                pendingOperationDao.deletePendingOperation(operation)
            } catch (e: Exception) {
                // 操作失败，增加重试次数
                pendingOperationDao.incrementRetryCount(
                    operation.id,
                    e.message ?: "Unknown error"
                )
            }
        }
    }
    
    /**
     * 处理发送邮件操作
     */
    private suspend fun processSendEmail(operation: PendingOperationEntity) {
        val email = gson.fromJson(operation.data ?: return, Email::class.java)
        emailRepository.sendEmail(email).getOrThrow()
    }
    
    /**
     * 处理删除邮件操作
     */
    private suspend fun processDeleteEmail(operation: PendingOperationEntity) {
        val emailId = operation.emailId ?: return
        emailRepository.deleteEmail(emailId).getOrThrow()
    }
    
    /**
     * 处理归档邮件操作
     */
    private suspend fun processArchiveEmail(operation: PendingOperationEntity) {
        val emailId = operation.emailId ?: return
        emailRepository.archiveEmail(emailId).getOrThrow()
    }
    
    /**
     * 处理标记已读操作
     */
    private suspend fun processMarkAsRead(operation: PendingOperationEntity) {
        val emailId = operation.emailId ?: return
        val isRead = gson.fromJson(operation.data ?: return, Boolean::class.java)
        emailRepository.markAsRead(emailId, isRead).getOrThrow()
    }
    
    /**
     * 获取待处理操作数量
     */
    suspend fun getPendingOperationCount(): Int {
        return pendingOperationDao.getPendingOperationCount()
    }
    
    /**
     * 清空队列
     */
    suspend fun clearQueue() {
        pendingOperationDao.clearAllPendingOperations()
    }
}
