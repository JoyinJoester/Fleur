package takagi.ru.fleur.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.data.local.entity.AttachmentEntity

/**
 * 附件 DAO
 * 提供附件的数据库操作
 */
@Dao
interface AttachmentDao {
    
    /**
     * 获取邮件的所有附件
     */
    @Query("SELECT * FROM attachments WHERE email_id = :emailId")
    fun getAttachmentsByEmailId(emailId: String): Flow<List<AttachmentEntity>>
    
    /**
     * 根据ID获取附件
     */
    @Query("SELECT * FROM attachments WHERE id = :attachmentId")
    suspend fun getAttachmentById(attachmentId: String): AttachmentEntity?
    
    /**
     * 插入附件
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity)
    
    /**
     * 批量插入附件
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<AttachmentEntity>)
    
    /**
     * 更新附件
     */
    @Update
    suspend fun updateAttachment(attachment: AttachmentEntity)
    
    /**
     * 删除附件
     */
    @Delete
    suspend fun deleteAttachment(attachment: AttachmentEntity)
    
    /**
     * 根据ID删除附件
     */
    @Query("DELETE FROM attachments WHERE id = :attachmentId")
    suspend fun deleteAttachmentById(attachmentId: String)
    
    /**
     * 删除邮件的所有附件
     */
    @Query("DELETE FROM attachments WHERE email_id = :emailId")
    suspend fun deleteAttachmentsByEmailId(emailId: String)
    
    /**
     * 更新附件的本地路径
     */
    @Query("UPDATE attachments SET local_path = :localPath WHERE id = :attachmentId")
    suspend fun updateLocalPath(attachmentId: String, localPath: String)
}
