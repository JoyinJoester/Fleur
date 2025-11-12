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
     * 删除邮件
     */
    override suspend fun deleteEmail(emailId: String): Result<Unit> {
        return try {
            // 先从本地数据库删除
            emailDao.deleteEmailById(emailId)
            attachmentDao.deleteAttachmentsByEmailId(emailId)
            
            // 尝试从 WebDAV 服务器删除（如果连接的话）
            try {
                webdavClient.deleteEmail(emailId)
            } catch (e: IllegalArgumentException) {
                // WebDAV 未连接，忽略错误，只在本地删除
                android.util.Log.w("EmailRepository", "WebDAV 未连接，仅在本地删除邮件")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FleurError.DatabaseError(e.message ?: "删除失败"))
        }
    }
    
    /**
     * 归档邮件
     */
    override suspend fun archiveEmail(emailId: String): Result<Unit> {
        return try {
            // 标记为已读并从收件箱移除
            markAsRead(emailId, true)
            // 实际应用中可能需要移动到归档文件夹
            Result.success(Unit)
        } catch (e: Exception) {
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
     * 批量删除邮件
     */
    override suspend fun deleteEmails(emailIds: List<String>): Result<Unit> {
        return try {
            // 逐个删除（实际应用中可能需要批量API）
            emailIds.forEach { emailId ->
                webdavClient.deleteEmail(emailId)
            }
            
            // 从本地数据库删除
            emailDao.deleteEmailsByIds(emailIds)
            emailIds.forEach { emailId ->
                attachmentDao.deleteAttachmentsByEmailId(emailId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FleurError.NetworkError(e.message ?: "批量删除失败"))
        }
    }
    
    /**
     * 批量归档邮件
     */
    override suspend fun archiveEmails(emailIds: List<String>): Result<Unit> {
        return try {
            emailIds.forEach { emailId ->
                archiveEmail(emailId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
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
