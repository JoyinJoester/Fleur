package takagi.ru.fleur.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import takagi.ru.fleur.domain.repository.isWebDAVEnabled
import takagi.ru.fleur.data.local.dao.AttachmentDao
import takagi.ru.fleur.data.local.dao.EmailDao
import takagi.ru.fleur.data.local.mapper.EntityMapper.toDomain
import takagi.ru.fleur.data.local.mapper.EntityMapper.toEntity
import takagi.ru.fleur.data.remote.webdav.WebDAVClient
import takagi.ru.fleur.data.remote.webdav.dto.EmailFlags
import takagi.ru.fleur.data.remote.webdav.mapper.DtoMapper.toDomain
import takagi.ru.fleur.data.remote.webdav.mapper.DtoMapper.toDto
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.EmailThread
import takagi.ru.fleur.domain.model.FleurError
import takagi.ru.fleur.domain.model.SearchFilters
import takagi.ru.fleur.domain.model.SyncResult
import takagi.ru.fleur.domain.repository.EmailRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

/**
 * 邮件仓库实现
 * 协调本地数据库和远程 WebDAV 服务器
 * 
 * 采用本地优先架构：
 * - 所有操作立即在本地生效
 * - WebDAV 同步作为可选的后台任务
 */
class EmailRepositoryImpl @Inject constructor(
    private val emailDao: EmailDao,
    private val attachmentDao: AttachmentDao,
    private val webdavClient: WebDAVClient,
    private val syncQueueManager: takagi.ru.fleur.data.sync.SyncQueueManager,
    private val preferencesRepository: takagi.ru.fleur.domain.repository.PreferencesRepository
) : EmailRepository {
    
    companion object {
        private const val TAG = "EmailRepository"
        private const val DEFAULT_PAGE_SIZE = 50
        private val CACHE_DURATION = 30.days
        
        /**
         * 对邮件列表进行去重，确保每个邮件ID只出现一次
         * 如果发现重复，会记录警告日志
         */
        private fun deduplicateEmails(emails: List<Email>, source: String): List<Email> {
            val originalSize = emails.size
            val uniqueEmails = emails.distinctBy { it.id }
            val duplicateCount = originalSize - uniqueEmails.size
            
            if (duplicateCount > 0) {
                Log.w(TAG, "[$source] 发现 $duplicateCount 个重复邮件，原始数量: $originalSize, 去重后: ${uniqueEmails.size}")
            }
            
            Log.d(TAG, "[$source] 邮件列表大小: ${uniqueEmails.size}")
            return uniqueEmails
        }
    }
    
    /**
     * 获取邮件列表（离线优先）
     * 自动对结果进行去重，确保每个邮件ID只出现一次
     */
    override fun getEmails(
        accountId: String?,
        page: Int,
        pageSize: Int
    ): Flow<Result<List<Email>>> = flow {
        // 立即返回本地数据
        emailDao.getEmailsPaged(
            accountId = accountId,
            limit = pageSize,
            offset = page * pageSize
        ).map { entities ->
            val emails = entities.map { entity ->
                val attachments = attachmentDao.getAttachmentsByEmailId(entity.id)
                    .first()
                    .map { it.toDomain() }
                entity.toDomain(attachments)
            }
            // 去重处理
            val uniqueEmails = deduplicateEmails(emails, "getEmails")
            Result.success(uniqueEmails)
        }.catch { e ->
            emit(Result.failure(FleurError.DatabaseError(e.message ?: "数据库错误")))
        }.collect { result ->
            emit(result)
        }
    }
    
    /**
     * 根据ID获取单个邮件
     */
    override fun getEmailById(emailId: String): Flow<Result<Email>> = flow {
        emailDao.getEmailById(emailId)
            .catch { e ->
                emit(Result.failure(FleurError.DatabaseError(e.message ?: "数据库错误")))
            }
            .collect { entity ->
                if (entity != null) {
                    val attachments = attachmentDao.getAttachmentsByEmailId(entity.id)
                        .first()
                        .map { it.toDomain() }
                    emit(Result.success(entity.toDomain(attachments)))
                } else {
                    emit(Result.failure(FleurError.NotFoundError("邮件不存在")))
                }
            }
    }
    
    /**
     * 获取邮件线程
     */
    override fun getEmailThread(threadId: String): Flow<Result<EmailThread>> = flow {
        emailDao.getEmailThread(threadId)
            .map { entities ->
                if (entities.isEmpty()) {
                    Result.failure(FleurError.NotFoundError("邮件线程不存在"))
                } else {
                    val emails = entities.map { entity ->
                        val attachments = attachmentDao.getAttachmentsByEmailId(entity.id)
                            .first()
                            .map { it.toDomain() }
                        entity.toDomain(attachments)
                    }
                    
                    val participants = emails.flatMap { email ->
                        listOf(email.from) + email.to + email.cc
                    }.distinctBy { it.address }
                    
                    val thread = EmailThread(
                        id = threadId,
                        subject = emails.firstOrNull()?.subject ?: "",
                        participants = participants,
                        emails = emails,
                        lastMessageTime = emails.maxOfOrNull { it.timestamp } ?: Instant.DISTANT_PAST,
                        unreadCount = emails.count { !it.isRead }
                    )
                    Result.success(thread)
                }
            }
            .catch { e ->
                emit(Result.failure(FleurError.DatabaseError(e.message ?: "数据库错误")))
            }
            .collect { result ->
                emit(result)
            }
    }
    
    /**
     * 获取与指定联系人的所有邮件（用于聊天详情）
     */
    override fun getEmailsByContact(
        accountId: String?,
        contactEmail: String
    ): Flow<Result<List<Email>>> = flow {
        Log.d(TAG, "查询与联系人的邮件 - accountId: $accountId, contactEmail: $contactEmail")
        emailDao.getEmailsByContact(accountId, contactEmail)
            .map { entities ->
                Log.d(TAG, "查询到 ${entities.size} 封邮件")
                val emails = entities.map { entity ->
                    val attachments = attachmentDao.getAttachmentsByEmailId(entity.id)
                        .first()
                        .map { it.toDomain() }
                    entity.toDomain(attachments)
                }
                Result.success(emails)
            }
            .catch { e ->
                Log.e(TAG, "查询邮件失败", e)
                emit(Result.failure(FleurError.DatabaseError(e.message ?: "数据库错误")))
            }
            .collect { result ->
                emit(result)
            }
    }
    
    /**
     * 搜索邮件
     * 自动对结果进行去重，确保每个邮件ID只出现一次
     */
    override fun searchEmails(
        query: String,
        filters: SearchFilters
    ): Flow<Result<List<Email>>> = flow {
        emailDao.advancedSearch(
            query = query.takeIf { it.isNotBlank() },
            accountId = filters.accountId,
            isUnread = filters.isUnread?.let { !it },
            isStarred = filters.isStarred,
            startTime = filters.dateRange?.start?.toEpochMilliseconds(),
            endTime = filters.dateRange?.end?.toEpochMilliseconds()
        ).map { entities ->
            val emails = entities.map { entity ->
                val attachments = attachmentDao.getAttachmentsByEmailId(entity.id)
                    .first()
                    .map { it.toDomain() }
                entity.toDomain(attachments)
            }
            
            // 应用附件过滤
            val filtered = if (filters.hasAttachment != null) {
                emails.filter { it.hasAttachments() == filters.hasAttachment }
            } else {
                emails
            }
            
            // 应用发件人过滤
            val finalFiltered = if (filters.sender != null) {
                filtered.filter { 
                    it.from.address.contains(filters.sender, ignoreCase = true) ||
                    it.from.name?.contains(filters.sender, ignoreCase = true) == true
                }
            } else {
                filtered
            }
            
            // 去重处理
            val uniqueEmails = deduplicateEmails(finalFiltered, "searchEmails")
            Result.success(uniqueEmails)
        }.catch { e ->
            emit(Result.failure(FleurError.DatabaseError(e.message ?: "搜索失败")))
        }.collect { result ->
            emit(result)
        }
    }
    
    /**
     * 发送邮件
     */
    override suspend fun sendEmail(email: Email): Result<Unit> {
        return try {
            // 发送到 WebDAV 服务器
            val emailDto = email.toDto()
            val result = webdavClient.sendEmail(emailDto)
            
            if (result.isSuccess) {
                // 保存到本地数据库
                emailDao.insertEmail(email.toEntity())
                
                // 保存附件
                email.attachments.forEach { attachment ->
                    attachmentDao.insertAttachment(attachment.toEntity())
                }
                
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("发送失败"))
            }
        } catch (e: Exception) {
            Result.failure(FleurError.NetworkError(e.message ?: "网络错误"))
        }
    }
    
    /**
     * 删除邮件（本地优先）
     * 将邮件从当前文件夹移动到回收站
     * 
     * 本地优先策略：
     * 1. 立即更新本地数据库（移除所有标签，只保留 trash）
     * 2. 立即返回成功结果
     * 3. 如果 WebDAV 已启用，添加操作到同步队列
     * 
     * @param emailId 邮件ID
     * @return 操作结果
     */
    override suspend fun deleteEmail(emailId: String): Result<Unit> {
        return try {
            // 1. 获取邮件实体
            val emailEntity = emailDao.getEmailById(emailId).first()
            
            if (emailEntity != null) {
                // 2. 移除所有标签，添加 "trash" 标签
                val updatedEntity = emailEntity.copy(
                    labels = "trash"  // 删除操作：移除所有标签，只保留 trash
                )
                
                // 3. 立即更新本地数据库
                emailDao.updateEmail(updatedEntity)
                Log.d(TAG, "本地删除邮件成功（移动到回收站）: emailId=$emailId, 标签更新: ${emailEntity.labels} -> trash")
                
                // 4. 如果 WebDAV 已启用，添加到同步队列
                if (preferencesRepository.isWebDAVEnabled()) {
                    val operation = takagi.ru.fleur.data.local.entity.SyncOperation(
                        operationType = takagi.ru.fleur.data.local.entity.OperationType.DELETE,
                        emailId = emailId,
                        timestamp = Clock.System.now().toEpochMilliseconds()
                    )
                    syncQueueManager.enqueue(operation)
                    Log.d(TAG, "已添加删除操作到同步队列: emailId=$emailId")
                } else {
                    Log.d(TAG, "WebDAV 未启用，跳过同步队列")
                }
                
                // 5. 立即返回成功
                Result.success(Unit)
            } else {
                Log.e(TAG, "删除邮件失败: 邮件不存在, emailId=$emailId")
                Result.failure(FleurError.NotFoundError("邮件不存在"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "删除邮件失败: emailId=$emailId, error=${e.message}", e)
            Result.failure(FleurError.DatabaseError(e.message ?: "删除失败"))
        }
    }
    
    /**
     * 归档邮件（本地优先）
     * 将邮件从收件箱移动到归档文件夹
     * 
     * 本地优先策略：
     * 1. 立即更新本地数据库（移除 inbox 标签，添加 archive 标签，标记为已读）
     * 2. 立即返回成功结果
     * 3. 如果 WebDAV 已启用，添加操作到同步队列
     * 
     * @param emailId 邮件ID
     * @return 操作结果
     */
    override suspend fun archiveEmail(emailId: String): Result<Unit> {
        return try {
            // 1. 获取邮件实体
            val emailEntity = emailDao.getEmailById(emailId).first()
            
            if (emailEntity != null) {
                // 2. 解析当前标签字符串，移除 "inbox" 标签
                val currentLabels = emailEntity.labels?.split(",")?.map { it.trim() }?.toMutableList() ?: mutableListOf()
                currentLabels.remove("inbox")
                
                // 3. 添加 "archive" 标签（如果不存在）
                if (!currentLabels.contains("archive")) {
                    currentLabels.add("archive")
                }
                
                // 4. 创建更新后的邮件实体，设置 isRead = true
                val updatedEntity = emailEntity.copy(
                    labels = currentLabels.joinToString(","),
                    isRead = true  // 归档时标记为已读
                )
                
                // 5. 立即更新本地数据库
                emailDao.updateEmail(updatedEntity)
                Log.d(TAG, "本地归档邮件成功: emailId=$emailId, 标签更新: ${emailEntity.labels} -> ${updatedEntity.labels}")
                
                // 6. 如果 WebDAV 已启用，添加到同步队列
                if (preferencesRepository.isWebDAVEnabled()) {
                    val operation = takagi.ru.fleur.data.local.entity.SyncOperation(
                        operationType = takagi.ru.fleur.data.local.entity.OperationType.ARCHIVE,
                        emailId = emailId,
                        timestamp = Clock.System.now().toEpochMilliseconds()
                    )
                    syncQueueManager.enqueue(operation)
                    Log.d(TAG, "已添加归档操作到同步队列: emailId=$emailId")
                } else {
                    Log.d(TAG, "WebDAV 未启用，跳过同步队列")
                }
                
                // 7. 立即返回成功
                Result.success(Unit)
            } else {
                Log.e(TAG, "归档邮件失败: 邮件不存在, emailId=$emailId")
                Result.failure(FleurError.NotFoundError("邮件不存在"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "归档邮件失败: emailId=$emailId, error=${e.message}", e)
            Result.failure(FleurError.DatabaseError(e.message ?: "归档失败"))
        }
    }
    
    /**
     * 标记邮件为已读/未读
     */
    override suspend fun markAsRead(emailId: String, isRead: Boolean): Result<Unit> {
        return try {
            // 先更新本地数据库
            emailDao.markAsRead(emailId, isRead)
            
            // 尝试更新远程服务器（如果连接的话）
            try {
                val flags = EmailFlags(isRead = isRead)
                webdavClient.updateEmailFlags(emailId, flags)
            } catch (e: IllegalArgumentException) {
                // WebDAV 未连接，忽略错误，只在本地更新
                android.util.Log.w("EmailRepository", "WebDAV 未连接，仅在本地更新已读状态")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FleurError.DatabaseError(e.message ?: "更新失败"))
        }
    }
    
    /**
     * 标记邮件为星标/取消星标（本地优先）
     * 
     * 本地优先策略：
     * 1. 立即更新本地数据库
     * 2. 立即返回成功结果
     * 3. 如果 WebDAV 已启用，添加操作到同步队列
     * 
     * @param emailId 邮件ID
     * @param isStarred 是否星标
     * @return 操作结果
     */
    override suspend fun toggleStar(emailId: String, isStarred: Boolean): Result<Unit> {
        return try {
            // 1. 立即更新本地数据库
            emailDao.toggleStar(emailId, isStarred)
            Log.d(TAG, "本地更新星标状态成功: emailId=$emailId, isStarred=$isStarred")
            
            // 2. 如果 WebDAV 已启用，添加到同步队列
            if (preferencesRepository.isWebDAVEnabled()) {
                val operationType = if (isStarred) {
                    takagi.ru.fleur.data.local.entity.OperationType.STAR
                } else {
                    takagi.ru.fleur.data.local.entity.OperationType.UNSTAR
                }
                
                val operation = takagi.ru.fleur.data.local.entity.SyncOperation(
                    operationType = operationType,
                    emailId = emailId,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
                syncQueueManager.enqueue(operation)
                Log.d(TAG, "已添加星标操作到同步队列: emailId=$emailId")
            } else {
                Log.d(TAG, "WebDAV 未启用，跳过同步队列")
            }
            
            // 3. 立即返回成功
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "更新星标状态失败: emailId=$emailId, error=${e.message}", e)
            Result.failure(FleurError.DatabaseError(e.message ?: "更新失败"))
        }
    }
    
    /**
     * 同步邮件（本地优先增量同步）
     * 
     * 本地优先策略：
     * 1. 从 WebDAV 拉取远程数据
     * 2. 使用时间戳比较本地和远程数据
     * 3. 实现冲突解决逻辑（最后写入获胜）
     * 4. 更新本地数据库
     * 
     * @param accountId 账户ID
     * @return 同步结果
     */
    override suspend fun syncEmails(accountId: String): Result<SyncResult> {
        return try {
            // 检查 WebDAV 是否已启用
            if (!preferencesRepository.isWebDAVEnabled()) {
                Log.d(TAG, "WebDAV 未启用，跳过同步")
                return Result.success(
                    SyncResult(
                        accountId = accountId,
                        newEmailsCount = 0,
                        updatedEmailsCount = 0,
                        success = true
                    )
                )
            }
            
            // 获取最后同步时间
            val lastSyncTime = getLastSyncTime(accountId)
            Log.d(TAG, "开始同步邮件: accountId=$accountId, lastSyncTime=$lastSyncTime")
            
            // 从 WebDAV 服务器获取新邮件
            val result = webdavClient.fetchEmails(lastSyncTime)
            
            if (result.isSuccess) {
                val emailDtos = result.getOrNull() ?: emptyList()
                var newCount = 0
                var updatedCount = 0
                var skippedCount = 0
                
                Log.d(TAG, "从 WebDAV 获取到 ${emailDtos.size} 封邮件")
                
                // 处理每封邮件
                for (dto in emailDtos) {
                    val remoteEmail = dto.toDomain(accountId)
                    val remoteTimestamp = remoteEmail.timestamp.toEpochMilliseconds()
                    
                    // 获取本地邮件（如果存在）
                    val localEntity = emailDao.getEmailById(remoteEmail.id).first()
                    
                    if (localEntity == null) {
                        // 本地不存在，直接插入
                        emailDao.insertEmail(remoteEmail.toEntity())
                        
                        // 保存附件
                        remoteEmail.attachments.forEach { attachment ->
                            attachmentDao.insertAttachment(attachment.toEntity())
                        }
                        
                        newCount++
                        Log.d(TAG, "新增邮件: emailId=${remoteEmail.id}")
                    } else {
                        // 本地存在，比较时间戳
                        val localTimestamp = localEntity.timestamp
                        
                        if (remoteTimestamp > localTimestamp) {
                            // 远程数据更新，更新本地数据
                            // 注意：保留本地的 isRead 和 isStarred 状态（用户可能已经修改）
                            val updatedEntity = remoteEmail.toEntity().copy(
                                isRead = localEntity.isRead,
                                isStarred = localEntity.isStarred
                            )
                            emailDao.updateEmail(updatedEntity)
                            
                            // 更新附件
                            remoteEmail.attachments.forEach { attachment ->
                                attachmentDao.insertAttachment(attachment.toEntity())
                            }
                            
                            updatedCount++
                            Log.d(TAG, "更新邮件: emailId=${remoteEmail.id}, 远程时间戳=$remoteTimestamp > 本地时间戳=$localTimestamp")
                        } else if (remoteTimestamp == localTimestamp) {
                            // 时间戳相同，检查内容是否有差异
                            // 如果本地的 isRead 或 isStarred 与远程不同，保留本地状态
                            val remoteEntity = remoteEmail.toEntity()
                            if (localEntity.isRead != remoteEntity.isRead || 
                                localEntity.isStarred != remoteEntity.isStarred) {
                                // 本地状态已修改，跳过更新
                                skippedCount++
                                Log.d(TAG, "跳过邮件（本地状态已修改）: emailId=${remoteEmail.id}")
                            } else {
                                // 内容相同，跳过
                                skippedCount++
                            }
                        } else {
                            // 本地数据更新，保留本地数据
                            skippedCount++
                            Log.d(TAG, "跳过邮件（本地更新）: emailId=${remoteEmail.id}, 本地时间戳=$localTimestamp > 远程时间戳=$remoteTimestamp")
                        }
                    }
                }
                
                // 清理旧邮件（30天前）
                val cutoffTime = Clock.System.now().minus(CACHE_DURATION)
                emailDao.deleteEmailsBefore(cutoffTime.toEpochMilliseconds())
                Log.d(TAG, "清理旧邮件: 删除了 30 天前的邮件")
                
                Log.d(TAG, "同步完成: 新增=$newCount, 更新=$updatedCount, 跳过=$skippedCount")
                
                Result.success(
                    SyncResult(
                        accountId = accountId,
                        newEmailsCount = newCount,
                        updatedEmailsCount = updatedCount,
                        success = true
                    )
                )
            } else {
                val error = result.exceptionOrNull()?.message ?: "同步失败"
                Log.e(TAG, "同步失败: $error")
                Result.failure(FleurError.SyncError(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "同步邮件异常: ${e.message}", e)
            Result.failure(FleurError.NetworkError(e.message ?: "网络错误"))
        }
    }
    
    /**
     * 批量删除邮件（本地优先）
     * 使用批量更新优化性能
     * 
     * 本地优先策略：
     * 1. 立即批量更新本地数据库（移除所有标签，只保留 trash）
     * 2. 立即返回成功结果
     * 3. 如果 WebDAV 已启用，批量添加操作到同步队列
     * 
     * @param emailIds 邮件ID列表
     * @return 操作结果
     */
    override suspend fun deleteEmails(emailIds: List<String>): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, String>()
            val failedIds = mutableListOf<String>()
            
            // 遍历邮件ID列表，获取每个邮件实体
            emailIds.forEach { emailId ->
                try {
                    val emailEntity = emailDao.getEmailById(emailId).first()
                    if (emailEntity != null) {
                        // 移除所有标签，添加 "trash" 标签
                        updates[emailId] = "trash"
                    } else {
                        Log.w(TAG, "批量删除: 邮件不存在, emailId=$emailId")
                        failedIds.add(emailId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "批量删除: 处理邮件失败, emailId=$emailId, error=${e.message}")
                    failedIds.add(emailId)
                }
            }
            
            // 1. 立即批量更新本地数据库
            if (updates.isNotEmpty()) {
                emailDao.updateEmailLabels(updates)
                Log.d(TAG, "本地批量删除成功（移动到回收站）: count=${updates.size}")
                
                // 2. 如果 WebDAV 已启用，批量添加到同步队列
                if (preferencesRepository.isWebDAVEnabled()) {
                    val operations = updates.keys.map { emailId ->
                        takagi.ru.fleur.data.local.entity.SyncOperation(
                            operationType = takagi.ru.fleur.data.local.entity.OperationType.DELETE,
                            emailId = emailId,
                            timestamp = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                    syncQueueManager.enqueueAll(operations)
                    Log.d(TAG, "已添加 ${operations.size} 个删除操作到同步队列")
                } else {
                    Log.d(TAG, "WebDAV 未启用，跳过同步队列")
                }
            }
            
            // 3. 立即返回成功
            if (failedIds.isEmpty()) {
                Result.success(Unit)
            } else {
                Log.w(TAG, "批量删除部分失败: 失败的邮件ID=$failedIds")
                Result.success(Unit)  // 部分成功也返回成功
            }
        } catch (e: Exception) {
            Log.e(TAG, "批量删除邮件失败: ${e.message}", e)
            Result.failure(FleurError.DatabaseError(e.message ?: "批量删除失败"))
        }
    }
    
    /**
     * 批量归档邮件（本地优先）
     * 使用批量更新优化性能
     * 
     * 本地优先策略：
     * 1. 立即批量更新本地数据库（标签和已读状态）
     * 2. 立即返回成功结果
     * 3. 如果 WebDAV 已启用，批量添加操作到同步队列
     * 
     * @param emailIds 邮件ID列表
     * @return 操作结果
     */
    override suspend fun archiveEmails(emailIds: List<String>): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, String>()
            val failedIds = mutableListOf<String>()
            
            // 遍历邮件ID列表，获取每个邮件实体
            emailIds.forEach { emailId ->
                try {
                    val emailEntity = emailDao.getEmailById(emailId).first()
                    if (emailEntity != null) {
                        // 为每个邮件更新标签（移除 "inbox"，添加 "archive"）
                        val currentLabels = emailEntity.labels?.split(",")?.map { it.trim() }?.toMutableList() ?: mutableListOf()
                        currentLabels.remove("inbox")
                        if (!currentLabels.contains("archive")) {
                            currentLabels.add("archive")
                        }
                        // 收集所有更新到 Map<String, String>
                        updates[emailId] = currentLabels.joinToString(",")
                    } else {
                        Log.w(TAG, "批量归档: 邮件不存在, emailId=$emailId")
                        failedIds.add(emailId)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "批量归档: 处理邮件失败, emailId=$emailId, error=${e.message}")
                    failedIds.add(emailId)
                }
            }
            
            // 1. 立即批量更新本地数据库
            if (updates.isNotEmpty()) {
                // 批量更新标签
                emailDao.updateEmailLabels(updates)
                
                // 批量标记为已读
                emailDao.markEmailsAsRead(updates.keys.toList(), true)
                
                Log.d(TAG, "本地批量归档成功: count=${updates.size}")
                
                // 2. 如果 WebDAV 已启用，批量添加到同步队列
                if (preferencesRepository.isWebDAVEnabled()) {
                    val operations = updates.keys.map { emailId ->
                        takagi.ru.fleur.data.local.entity.SyncOperation(
                            operationType = takagi.ru.fleur.data.local.entity.OperationType.ARCHIVE,
                            emailId = emailId,
                            timestamp = Clock.System.now().toEpochMilliseconds()
                        )
                    }
                    syncQueueManager.enqueueAll(operations)
                    Log.d(TAG, "已添加 ${operations.size} 个归档操作到同步队列")
                } else {
                    Log.d(TAG, "WebDAV 未启用，跳过同步队列")
                }
            }
            
            // 3. 立即返回成功
            if (failedIds.isEmpty()) {
                Result.success(Unit)
            } else {
                Log.w(TAG, "批量归档部分失败: 失败的邮件ID=$failedIds")
                Result.success(Unit)  // 部分成功也返回成功
            }
        } catch (e: Exception) {
            Log.e(TAG, "批量归档邮件失败: ${e.message}", e)
            Result.failure(FleurError.DatabaseError(e.message ?: "批量归档失败"))
        }
    }
    
    /**
     * 批量标记为已读/未读（本地优先）
     * 
     * 本地优先策略：
     * 1. 立即更新本地数据库
     * 2. 立即返回成功结果
     * 3. 如果 WebDAV 已启用，添加操作到同步队列
     * 
     * @param emailIds 邮件ID列表
     * @param isRead 是否已读
     * @return 操作结果
     */
    override suspend fun markEmailsAsRead(emailIds: List<String>, isRead: Boolean): Result<Unit> {
        return try {
            // 1. 立即更新本地数据库
            emailDao.markEmailsAsRead(emailIds, isRead)
            Log.d(TAG, "本地标记邮件为${if (isRead) "已读" else "未读"}成功: count=${emailIds.size}")
            
            // 2. 如果 WebDAV 已启用，添加到同步队列
            if (preferencesRepository.isWebDAVEnabled()) {
                val operationType = if (isRead) {
                    takagi.ru.fleur.data.local.entity.OperationType.MARK_READ
                } else {
                    takagi.ru.fleur.data.local.entity.OperationType.MARK_UNREAD
                }
                
                val operations = emailIds.map { emailId ->
                    takagi.ru.fleur.data.local.entity.SyncOperation(
                        operationType = operationType,
                        emailId = emailId,
                        timestamp = Clock.System.now().toEpochMilliseconds()
                    )
                }
                
                syncQueueManager.enqueueAll(operations)
                Log.d(TAG, "已添加 ${operations.size} 个操作到同步队列")
            } else {
                Log.d(TAG, "WebDAV 未启用，跳过同步队列")
            }
            
            // 3. 立即返回成功
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "标记邮件为${if (isRead) "已读" else "未读"}失败: ${e.message}", e)
            Result.failure(FleurError.DatabaseError(e.message ?: "本地更新失败"))
        }
    }
    
    /**
     * 获取已发送邮件（分页）
     * 自动对结果进行去重，确保每个邮件ID只出现一次
     */
    override fun getSentEmails(
        accountId: String,
        page: Int,
        pageSize: Int
    ): Flow<Result<List<Email>>> = flow {
        emailDao.getSentEmails(
            accountId = accountId,
            limit = pageSize,
            offset = page * pageSize
        ).map { entities ->
            val emails = entities.map { entity ->
                val attachments = attachmentDao.getAttachmentsByEmailId(entity.id)
                    .first()
                    .map { it.toDomain() }
                entity.toDomain(attachments)
            }
            // 去重处理
            val uniqueEmails = deduplicateEmails(emails, "getSentEmails")
            Result.success(uniqueEmails)
        }.catch { e ->
            emit(Result.failure(FleurError.DatabaseError(e.message ?: "获取已发送邮件失败")))
        }.collect { result ->
            emit(result)
        }
    }
    
    /**
     * 获取草稿邮件（分页）
     * 自动对结果进行去重，确保每个邮件ID只出现一次
     */
    override fun getDraftEmails(
        accountId: String,
        page: Int,
        pageSize: Int
    ): Flow<Result<List<Email>>> = flow {
        emailDao.getDraftEmails(
            accountId = accountId,
            limit = pageSize,
            offset = page * pageSize
        ).map { entities ->
            val emails = entities.map { entity ->
                val attachments = attachmentDao.getAttachmentsByEmailId(entity.id)
                    .first()
                    .map { it.toDomain() }
                entity.toDomain(attachments)
            }
            // 去重处理
            val uniqueEmails = deduplicateEmails(emails, "getDraftEmails")
            Result.success(uniqueEmails)
        }.catch { e ->
            emit(Result.failure(FleurError.DatabaseError(e.message ?: "获取草稿邮件失败")))
        }.collect { result ->
            emit(result)
        }
    }
    
    /**
     * 获取星标邮件（分页）
     * 自动对结果进行去重，确保每个邮件ID只出现一次
     */
    override fun getStarredEmails(
        accountId: String,
        page: Int,
        pageSize: Int
    ): Flow<Result<List<Email>>> = flow {
        emailDao.getStarredEmails(
            accountId = accountId,
            limit = pageSize,
            offset = page * pageSize
        ).map { entities ->
            val emails = entities.map { entity ->
                val attachments = attachmentDao.getAttachmentsByEmailId(entity.id)
                    .first()
                    .map { it.toDomain() }
                entity.toDomain(attachments)
            }
            // 去重处理
            val uniqueEmails = deduplicateEmails(emails, "getStarredEmails")
            Result.success(uniqueEmails)
        }.catch { e ->
            emit(Result.failure(FleurError.DatabaseError(e.message ?: "获取星标邮件失败")))
        }.collect { result ->
            emit(result)
        }
    }
    
    /**
     * 获取归档邮件（分页）
     * 自动对结果进行去重，确保每个邮件ID只出现一次
     */
    override fun getArchivedEmails(
        accountId: String,
        page: Int,
        pageSize: Int
    ): Flow<Result<List<Email>>> = flow {
        emailDao.getArchivedEmails(
            accountId = accountId,
            limit = pageSize,
            offset = page * pageSize
        ).map { entities ->
            val emails = entities.map { entity ->
                val attachments = attachmentDao.getAttachmentsByEmailId(entity.id)
                    .first()
                    .map { it.toDomain() }
                entity.toDomain(attachments)
            }
            // 去重处理
            val uniqueEmails = deduplicateEmails(emails, "getArchivedEmails")
            Result.success(uniqueEmails)
        }.catch { e ->
            emit(Result.failure(FleurError.DatabaseError(e.message ?: "获取归档邮件失败")))
        }.collect { result ->
            emit(result)
        }
    }
    
    /**
     * 获取垃圾箱邮件（分页）
     * 自动对结果进行去重，确保每个邮件ID只出现一次
     */
    override fun getTrashedEmails(
        accountId: String,
        page: Int,
        pageSize: Int
    ): Flow<Result<List<Email>>> = flow {
        emailDao.getTrashedEmails(
            accountId = accountId,
            limit = pageSize,
            offset = page * pageSize
        ).map { entities ->
            val emails = entities.map { entity ->
                val attachments = attachmentDao.getAttachmentsByEmailId(entity.id)
                    .first()
                    .map { it.toDomain() }
                entity.toDomain(attachments)
            }
            // 去重处理
            val uniqueEmails = deduplicateEmails(emails, "getTrashedEmails")
            Result.success(uniqueEmails)
        }.catch { e ->
            emit(Result.failure(FleurError.DatabaseError(e.message ?: "获取垃圾箱邮件失败")))
        }.collect { result ->
            emit(result)
        }
    }
    
    /**
     * 恢复邮件（从垃圾箱恢复到收件箱）
     * 通过移除 trash 标签并添加 inbox 标签实现
     */
    override suspend fun restoreEmail(emailId: String): Result<Unit> {
        return try {
            // 获取邮件实体
            val emailEntity = emailDao.getEmailById(emailId).first()
            
            if (emailEntity != null) {
                // 更新标签：移除 trash，添加 inbox
                val currentLabels = emailEntity.labels?.split(",")?.toMutableList() ?: mutableListOf()
                currentLabels.remove("trash")
                if (!currentLabels.contains("inbox")) {
                    currentLabels.add("inbox")
                }
                
                val updatedEntity = emailEntity.copy(
                    labels = currentLabels.joinToString(",")
                )
                
                // 更新本地数据库
                emailDao.updateEmail(updatedEntity)
                
                // TODO: 同步到远程服务器
                // webdavClient.updateEmailLabels(emailId, currentLabels)
                
                Result.success(Unit)
            } else {
                Result.failure(FleurError.NotFoundError("邮件不存在"))
            }
        } catch (e: Exception) {
            Result.failure(FleurError.DatabaseError(e.message ?: "恢复邮件失败"))
        }
    }
    
    /**
     * 批量恢复邮件
     */
    override suspend fun restoreEmails(emailIds: List<String>): Result<Unit> {
        return try {
            emailIds.forEach { emailId ->
                val result = restoreEmail(emailId)
                if (result.isFailure) {
                    return result
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FleurError.DatabaseError(e.message ?: "批量恢复邮件失败"))
        }
    }
    
    /**
     * 移动邮件到指定文件夹（本地优先）
     * 
     * 本地优先策略：
     * 1. 立即更新本地数据库（更新邮件标签）
     * 2. 立即返回成功结果
     * 3. 如果 WebDAV 已启用，添加操作到同步队列
     * 
     * @param emailId 邮件ID
     * @param targetFolder 目标文件夹（inbox, sent, drafts, archive, trash, starred）
     * @return 操作结果
     */
    override suspend fun moveToFolder(emailId: String, targetFolder: String): Result<Unit> {
        return try {
            // 1. 获取邮件实体
            val emailEntity = emailDao.getEmailById(emailId).first()
            
            if (emailEntity != null) {
                // 2. 解析当前标签
                val currentLabels = emailEntity.labels?.split(",")?.map { it.trim() }?.toMutableList() ?: mutableListOf()
                
                // 3. 移除所有文件夹标签（inbox, sent, drafts, archive, trash）
                val folderLabels = listOf("inbox", "sent", "drafts", "archive", "trash")
                currentLabels.removeAll(folderLabels)
                
                // 4. 添加目标文件夹标签
                if (!currentLabels.contains(targetFolder)) {
                    currentLabels.add(targetFolder)
                }
                
                // 5. 创建更新后的邮件实体
                val updatedEntity = emailEntity.copy(
                    labels = currentLabels.joinToString(",")
                )
                
                // 6. 立即更新本地数据库
                emailDao.updateEmail(updatedEntity)
                Log.d(TAG, "本地移动邮件成功: emailId=$emailId, 从 ${emailEntity.labels} 移动到 $targetFolder")
                
                // 7. 如果 WebDAV 已启用，添加到同步队列
                if (preferencesRepository.isWebDAVEnabled()) {
                    // 使用 extraData 存储目标文件夹信息
                    val operation = takagi.ru.fleur.data.local.entity.SyncOperation(
                        operationType = takagi.ru.fleur.data.local.entity.OperationType.MOVE_TO_FOLDER,
                        emailId = emailId,
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        extraData = targetFolder  // 存储目标文件夹
                    )
                    syncQueueManager.enqueue(operation)
                    Log.d(TAG, "已添加移动操作到同步队列: emailId=$emailId, targetFolder=$targetFolder")
                } else {
                    Log.d(TAG, "WebDAV 未启用，跳过同步队列")
                }
                
                // 8. 立即返回成功
                Result.success(Unit)
            } else {
                Log.e(TAG, "移动邮件失败: 邮件不存在, emailId=$emailId")
                Result.failure(FleurError.NotFoundError("邮件不存在"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "移动邮件失败: emailId=$emailId, targetFolder=$targetFolder, error=${e.message}", e)
            Result.failure(FleurError.DatabaseError(e.message ?: "移动失败"))
        }
    }
    
    /**
     * 获取最后同步时间
     */
    private suspend fun getLastSyncTime(accountId: String): Instant? {
        // 简化实现：获取该账户最新邮件的时间戳
        // 实际应用中应该存储在 PreferencesRepository 中
        return null
    }
}
