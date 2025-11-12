package takagi.ru.fleur.domain.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.EmailThread
import takagi.ru.fleur.domain.model.SearchFilters
import takagi.ru.fleur.domain.model.SyncResult

/**
 * 邮件仓库接口
 * 定义邮件相关的数据操作
 */
interface EmailRepository {
    
    /**
     * 获取邮件列表（分页）
     * @param accountId 账户ID，null表示所有账户
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return Flow<Result<List<Email>>> 邮件列表流
     */
    fun getEmails(
        accountId: String? = null,
        page: Int = 0,
        pageSize: Int = 50
    ): Flow<Result<List<Email>>>
    
    /**
     * 根据ID获取单个邮件
     * @param emailId 邮件ID
     * @return Flow<Result<Email>> 邮件流
     */
    fun getEmailById(emailId: String): Flow<Result<Email>>
    
    /**
     * 获取邮件线程
     * @param threadId 线程ID
     * @return Flow<Result<EmailThread>> 邮件线程流
     */
    fun getEmailThread(threadId: String): Flow<Result<EmailThread>>
    
    /**
     * 搜索邮件
     * @param query 搜索关键词
     * @param filters 搜索过滤器
     * @return Flow<Result<List<Email>>> 搜索结果流
     */
    fun searchEmails(
        query: String,
        filters: SearchFilters = SearchFilters()
    ): Flow<Result<List<Email>>>
    
    /**
     * 发送邮件
     * @param email 要发送的邮件
     * @return Result<Unit> 发送结果
     */
    suspend fun sendEmail(email: Email): Result<Unit>
    
    /**
     * 删除邮件
     * @param emailId 邮件ID
     * @return Result<Unit> 删除结果
     */
    suspend fun deleteEmail(emailId: String): Result<Unit>
    
    /**
     * 归档邮件
     * @param emailId 邮件ID
     * @return Result<Unit> 归档结果
     */
    suspend fun archiveEmail(emailId: String): Result<Unit>
    
    /**
     * 标记邮件为已读/未读
     * @param emailId 邮件ID
     * @param isRead 是否已读
     * @return Result<Unit> 操作结果
     */
    suspend fun markAsRead(emailId: String, isRead: Boolean): Result<Unit>
    
    /**
     * 标记邮件为星标/取消星标
     * @param emailId 邮件ID
     * @param isStarred 是否星标
     * @return Result<Unit> 操作结果
     */
    suspend fun toggleStar(emailId: String, isStarred: Boolean): Result<Unit>
    
    /**
     * 同步邮件
     * @param accountId 账户ID
     * @return Result<SyncResult> 同步结果
     */
    suspend fun syncEmails(accountId: String): Result<SyncResult>
    
    /**
     * 批量删除邮件
     * @param emailIds 邮件ID列表
     * @return Result<Unit> 删除结果
     */
    suspend fun deleteEmails(emailIds: List<String>): Result<Unit>
    
    /**
     * 批量归档邮件
     * @param emailIds 邮件ID列表
     * @return Result<Unit> 归档结果
     */
    suspend fun archiveEmails(emailIds: List<String>): Result<Unit>
    
    /**
     * 批量标记为已读
     * @param emailIds 邮件ID列表
     * @param isRead 是否已读
     * @return Result<Unit> 操作结果
     */
    suspend fun markEmailsAsRead(emailIds: List<String>, isRead: Boolean): Result<Unit>
    
    /**
     * 获取已发送邮件（分页）
     * @param accountId 账户ID
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return Flow<Result<List<Email>>> 已发送邮件列表流
     */
    fun getSentEmails(
        accountId: String,
        page: Int = 0,
        pageSize: Int = 50
    ): Flow<Result<List<Email>>>
    
    /**
     * 获取草稿邮件（分页）
     * @param accountId 账户ID
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return Flow<Result<List<Email>>> 草稿邮件列表流
     */
    fun getDraftEmails(
        accountId: String,
        page: Int = 0,
        pageSize: Int = 50
    ): Flow<Result<List<Email>>>
    
    /**
     * 获取星标邮件（分页）
     * @param accountId 账户ID
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return Flow<Result<List<Email>>> 星标邮件列表流
     */
    fun getStarredEmails(
        accountId: String,
        page: Int = 0,
        pageSize: Int = 50
    ): Flow<Result<List<Email>>>
    
    /**
     * 获取归档邮件（分页）
     * @param accountId 账户ID
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return Flow<Result<List<Email>>> 归档邮件列表流
     */
    fun getArchivedEmails(
        accountId: String,
        page: Int = 0,
        pageSize: Int = 50
    ): Flow<Result<List<Email>>>
    
    /**
     * 获取垃圾箱邮件（分页）
     * @param accountId 账户ID
     * @param page 页码（从0开始）
     * @param pageSize 每页数量
     * @return Flow<Result<List<Email>>> 垃圾箱邮件列表流
     */
    fun getTrashedEmails(
        accountId: String,
        page: Int = 0,
        pageSize: Int = 50
    ): Flow<Result<List<Email>>>
    
    /**
     * 恢复邮件（从垃圾箱恢复到收件箱）
     * @param emailId 邮件ID
     * @return Result<Unit> 恢复结果
     */
    suspend fun restoreEmail(emailId: String): Result<Unit>
    
    /**
     * 批量恢复邮件
     * @param emailIds 邮件ID列表
     * @return Result<Unit> 恢复结果
     */
    suspend fun restoreEmails(emailIds: List<String>): Result<Unit>
    
    /**
     * 移动邮件到指定文件夹
     * @param emailId 邮件ID
     * @param targetFolder 目标文件夹（inbox, sent, drafts, archive, trash, starred）
     * @return Result<Unit> 移动结果
     */
    suspend fun moveToFolder(emailId: String, targetFolder: String): Result<Unit>
}
