package takagi.ru.fleur.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
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
 */
class EmailRepositoryImpl @Inject constructor(
    private val emailDao: EmailDao,
    private val attachmentDao: AttachmentDao,
    private val webdavClient: WebDAVClient
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
     * 删除邮件（移动到回收站）
     * 将邮件从当前文件夹移动到回收站
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
                
                // 3. 更新本地数据库
                emailDao.updateEmail(updatedEntity)
                
                // 4. 尝试同步到远程服务器
                try {
                    // 注意: WebDAVClient 目前没有移动到回收站的方法
                    // 这里暂时不调用远程删除,保持本地状态
                    Log.d(TAG, "邮件已移动到回收站: emailId=$emailId")
                } catch (e: IllegalArgumentException) {
                    Log.w(TAG, "WebDAV 未连接，仅在本地移动到回收站: $emailId")
                } catch (e: Exception) {
                    Log.e(TAG, "远程同步删除失败: emailId=$emailId, error=${e.message}", e)
                }
                
                Log.d(TAG, "删除邮件成功（移动到回收站）: emailId=$emailId, 标签更新: ${emailEntity.labels} -> trash")
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
     * 归档邮件
     * 将邮件从收件箱移动到归档文件夹
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
                
                // 5. 调用 emailDao.updateEmail() 更新数据库
                emailDao.updateEmail(updatedEntity)
                
                // 6. 尝试调用 webdavClient 同步到远程服务器，捕获异常并记录日志
                try {
                    // 注意: WebDAVClient 目前没有 updateEmailLabels 方法
                    // 使用 updateEmailFlags 来标记为已读
                    val flags = EmailFlags(isRead = true)
                    webdavClient.updateEmailFlags(emailId, flags)
                } catch (e: IllegalArgumentException) {
                    // WebDAV 未连接，仅在本地更新
                    Log.w(TAG, "WebDAV 未连接，仅在本地归档邮件: $emailId")
                } catch (e: Exception) {
                    // 其他错误，记录但不影响本地操作
                    Log.e(TAG, "远程同步归档失败: emailId=$emailId, error=${e.message}", e)
                }
                
                Log.d(TAG, "归档邮件成功: emailId=$emailId, 标签更新: ${emailEntity.labels} -> ${updatedEntity.labels}")
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
     * 标记邮件为星标/取消星标
     */
    override suspend fun toggleStar(emailId: String, isStarred: Boolean): Result<Unit> {
        return try {
            // 先更新本地数据库
            emailDao.toggleStar(emailId, isStarred)
            
            // 尝试更新远程服务器（如果连接的话）
            try {
                val flags = EmailFlags(isStarred = isStarred)
                webdavClient.updateEmailFlags(emailId, flags)
            } catch (e: IllegalArgumentException) {
                // WebDAV 未连接，忽略错误，只在本地更新
                android.util.Log.w("EmailRepository", "WebDAV 未连接，仅在本地更新星标状态")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FleurError.DatabaseError(e.message ?: "更新失败"))
        }
    }
    
    /**
     * 同步邮件（增量同步）
     */
    override suspend fun syncEmails(accountId: String): Result<SyncResult> {
        return try {
            // 获取最后同步时间
            val lastSyncTime = getLastSyncTime(accountId)
            
            // 从 WebDAV 服务器获取新邮件
            val result = webdavClient.fetchEmails(lastSyncTime)
            
            if (result.isSuccess) {
                val emailDtos = result.getOrNull() ?: emptyList()
                var newCount = 0
                var updatedCount = 0
                
                // 保存到本地数据库
                for (dto in emailDtos) {
                    val email = dto.toDomain(accountId)
                    val existing = emailDao.getEmailById(email.id)
                    
                    if (existing == null) {
                        newCount++
                    } else {
                        updatedCount++
                    }
                    
                    emailDao.insertEmail(email.toEntity())
                    
                    // 保存附件
                    email.attachments.forEach { attachment ->
                        attachmentDao.insertAttachment(attachment.toEntity())
                    }
                }
                
                // 清理旧邮件（30天前）
                val cutoffTime = Clock.System.now().minus(CACHE_DURATION)
                emailDao.deleteEmailsBefore(cutoffTime.toEpochMilliseconds())
                
                Result.success(
                    SyncResult(
                        accountId = accountId,
                        newEmailsCount = newCount,
                        updatedEmailsCount = updatedCount,
                        success = true
                    )
                )
            } else {
                Result.failure(
                    FleurError.SyncError(
                        result.exceptionOrNull()?.message ?: "同步失败"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(FleurError.NetworkError(e.message ?: "网络错误"))
        }
    }
    
    /**
     * 批量删除邮件（移动到回收站）
     * 使用批量更新优化性能
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
            
            // 使用批量更新标签
            if (updates.isNotEmpty()) {
                emailDao.updateEmailLabels(updates)
                
                Log.d(TAG, "批量删除完成（移动到回收站）: 成功=${updates.size}, 失败=${failedIds.size}")
            }
            
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
     * 批量归档邮件
     * 使用批量更新优化性能
     * 
     * @param emailIds 邮件ID列表
     * @return 操作结果，包含成功和失败的数量
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
            
            // 使用 emailDao.updateEmailLabels(updates) 批量更新标签
            if (updates.isNotEmpty()) {
                emailDao.updateEmailLabels(updates)
                
                // 使用 emailDao.markEmailsAsRead() 批量标记为已读
                emailDao.markEmailsAsRead(updates.keys.toList(), true)
                
                // 尝试同步到远程服务器，记录失败的邮件ID
                try {
                    val flags = EmailFlags(isRead = true)
                    updates.keys.forEach { emailId ->
                        try {
                            webdavClient.updateEmailFlags(emailId, flags)
                        } catch (e: Exception) {
                            Log.w(TAG, "批量归档: 远程同步失败, emailId=$emailId, error=${e.message}")
                            // 不将同步失败的邮件添加到 failedIds，因为本地操作已成功
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    Log.w(TAG, "WebDAV 未连接，仅在本地批量归档邮件")
                }
            }
            
            // 记录成功和失败的数量
            val successCount = updates.size
            val failureCount = failedIds.size
            Log.d(TAG, "批量归档完成: 成功=$successCount, 失败=$failureCount")
            
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
     * 批量标记为已读
     */
    override suspend fun markEmailsAsRead(emailIds: List<String>, isRead: Boolean): Result<Unit> {
        return try {
            // 更新远程服务器
            val flags = EmailFlags(isRead = isRead)
            emailIds.forEach { emailId ->
                webdavClient.updateEmailFlags(emailId, flags)
            }
            
            // 更新本地数据库
            emailDao.markEmailsAsRead(emailIds, isRead)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FleurError.NetworkError(e.message ?: "批量更新失败"))
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
     * 获取最后同步时间
     */
    private suspend fun getLastSyncTime(accountId: String): Instant? {
        // 简化实现：获取该账户最新邮件的时间戳
        // 实际应用中应该存储在 PreferencesRepository 中
        return null
    }
}
