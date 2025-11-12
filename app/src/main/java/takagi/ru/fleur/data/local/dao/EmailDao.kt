package takagi.ru.fleur.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.data.local.entity.EmailEntity

/**
 * 邮件 DAO
 * 提供邮件的数据库操作
 */
@Dao
interface EmailDao {
    
    /**
     * 分页查询邮件
     * @param accountId 账户ID，null表示所有账户
     * @param limit 每页数量
     * @param offset 偏移量
     */
    @Query("""
        SELECT * FROM emails 
        WHERE (:accountId IS NULL OR account_id = :accountId)
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getEmailsPaged(
        accountId: String?,
        limit: Int,
        offset: Int
    ): Flow<List<EmailEntity>>
    
    /**
     * 根据ID获取邮件
     */
    @Query("SELECT * FROM emails WHERE id = :emailId")
    fun getEmailById(emailId: String): Flow<EmailEntity?>
    
    /**
     * 获取邮件线程
     * @param threadId 线程ID
     */
    @Query("""
        SELECT * FROM emails 
        WHERE thread_id = :threadId 
        ORDER BY timestamp ASC
    """)
    fun getEmailThread(threadId: String): Flow<List<EmailEntity>>
    
    /**
     * 搜索邮件
     * @param query 搜索关键词
     */
    @Query("""
        SELECT * FROM emails 
        WHERE subject LIKE '%' || :query || '%' 
           OR body_plain LIKE '%' || :query || '%'
           OR from_name LIKE '%' || :query || '%'
           OR from_address LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchEmails(query: String): Flow<List<EmailEntity>>
    
    /**
     * 高级搜索邮件
     * 支持多条件过滤
     */
    @Query("""
        SELECT * FROM emails 
        WHERE (:query IS NULL OR subject LIKE '%' || :query || '%' OR body_plain LIKE '%' || :query || '%')
          AND (:accountId IS NULL OR account_id = :accountId)
          AND (:isUnread IS NULL OR is_read = :isUnread)
          AND (:isStarred IS NULL OR is_starred = :isStarred)
          AND (:startTime IS NULL OR timestamp >= :startTime)
          AND (:endTime IS NULL OR timestamp <= :endTime)
        ORDER BY timestamp DESC
    """)
    fun advancedSearch(
        query: String?,
        accountId: String?,
        isUnread: Boolean?,
        isStarred: Boolean?,
        startTime: Long?,
        endTime: Long?
    ): Flow<List<EmailEntity>>
    
    /**
     * 获取未读邮件数量
     */
    @Query("""
        SELECT COUNT(*) FROM emails 
        WHERE is_read = 0 
          AND (:accountId IS NULL OR account_id = :accountId)
    """)
    fun getUnreadCount(accountId: String?): Flow<Int>
    
    /**
     * 插入邮件
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: EmailEntity)
    
    /**
     * 批量插入邮件
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailEntity>)
    
    /**
     * 更新邮件
     */
    @Update
    suspend fun updateEmail(email: EmailEntity)
    
    /**
     * 删除邮件
     */
    @Delete
    suspend fun deleteEmail(email: EmailEntity)
    
    /**
     * 根据ID删除邮件
     */
    @Query("DELETE FROM emails WHERE id = :emailId")
    suspend fun deleteEmailById(emailId: String)
    
    /**
     * 批量删除邮件
     */
    @Query("DELETE FROM emails WHERE id IN (:emailIds)")
    suspend fun deleteEmailsByIds(emailIds: List<String>)
    
    /**
     * 标记邮件为已读/未读
     */
    @Query("UPDATE emails SET is_read = :isRead WHERE id = :emailId")
    suspend fun markAsRead(emailId: String, isRead: Boolean)
    
    /**
     * 批量标记为已读/未读
     */
    @Query("UPDATE emails SET is_read = :isRead WHERE id IN (:emailIds)")
    suspend fun markEmailsAsRead(emailIds: List<String>, isRead: Boolean)
    
    /**
     * 切换星标状态
     */
    @Query("UPDATE emails SET is_starred = :isStarred WHERE id = :emailId")
    suspend fun toggleStar(emailId: String, isStarred: Boolean)
    
    /**
     * 删除指定时间之前的邮件（清理缓存）
     */
    @Query("DELETE FROM emails WHERE timestamp < :beforeTimestamp")
    suspend fun deleteEmailsBefore(beforeTimestamp: Long)
    
    /**
     * 删除指定账户的所有邮件
     */
    @Query("DELETE FROM emails WHERE account_id = :accountId")
    suspend fun deleteEmailsByAccount(accountId: String)
    
    /**
     * 获取所有邮件数量
     */
    @Query("SELECT COUNT(*) FROM emails")
    suspend fun getEmailCount(): Int
    
    /**
     * 获取已发送邮件（分页）
     * @param accountId 账户ID
     * @param limit 每页数量
     * @param offset 偏移量
     */
    @Query("""
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND labels LIKE '%sent%'
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getSentEmails(
        accountId: String,
        limit: Int,
        offset: Int
    ): Flow<List<EmailEntity>>
    
    /**
     * 获取草稿邮件（分页）
     * @param accountId 账户ID
     * @param limit 每页数量
     * @param offset 偏移量
     */
    @Query("""
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND labels LIKE '%drafts%'
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getDraftEmails(
        accountId: String,
        limit: Int,
        offset: Int
    ): Flow<List<EmailEntity>>
    
    /**
     * 获取星标邮件（分页）
     * @param accountId 账户ID
     * @param limit 每页数量
     * @param offset 偏移量
     */
    @Query("""
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND is_starred = 1
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getStarredEmails(
        accountId: String,
        limit: Int,
        offset: Int
    ): Flow<List<EmailEntity>>
    
    /**
     * 获取归档邮件（分页）
     * @param accountId 账户ID
     * @param limit 每页数量
     * @param offset 偏移量
     */
    @Query("""
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND labels LIKE '%archive%'
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getArchivedEmails(
        accountId: String,
        limit: Int,
        offset: Int
    ): Flow<List<EmailEntity>>
    
    /**
     * 获取垃圾箱邮件（分页）
     * @param accountId 账户ID
     * @param limit 每页数量
     * @param offset 偏移量
     */
    @Query("""
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND labels LIKE '%trash%'
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getTrashedEmails(
        accountId: String,
        limit: Int,
        offset: Int
    ): Flow<List<EmailEntity>>
    
    /**
     * 删除所有测试邮件
     * 删除ID以"test_email_"开头的邮件，用于清理测试数据
     */
    @Query("DELETE FROM emails WHERE id LIKE 'test_email_%'")
    suspend fun deleteTestEmails(): Int
}
