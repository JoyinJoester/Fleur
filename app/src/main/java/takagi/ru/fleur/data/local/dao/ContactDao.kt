package takagi.ru.fleur.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.data.local.entity.ContactEntity

/**
 * 联系人数据访问对象
 * 提供联系人的CRUD操作
 */
@Dao
interface ContactDao {
    
    /**
     * 获取所有联系人（按名称排序）
     */
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>
    
    /**
     * 根据ID获取联系人
     */
    @Query("SELECT * FROM contacts WHERE id = :contactId")
    fun getContactById(contactId: String): Flow<ContactEntity?>
    
    /**
     * 根据邮箱获取联系人
     */
    @Query("SELECT * FROM contacts WHERE email = :email LIMIT 1")
    suspend fun getContactByEmail(email: String): ContactEntity?
    
    /**
     * 获取收藏的联系人
     */
    @Query("SELECT * FROM contacts WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteContacts(): Flow<List<ContactEntity>>
    
    /**
     * 搜索联系人（按名称或邮箱）
     */
    @Query("""
        SELECT * FROM contacts 
        WHERE name LIKE '%' || :query || '%' 
        OR email LIKE '%' || :query || '%'
        OR organization LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchContacts(query: String): Flow<List<ContactEntity>>
    
    /**
     * 插入联系人
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)
    
    /**
     * 插入多个联系人
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)
    
    /**
     * 更新联系人
     */
    @Update
    suspend fun updateContact(contact: ContactEntity)
    
    /**
     * 删除联系人
     */
    @Delete
    suspend fun deleteContact(contact: ContactEntity)
    
    /**
     * 根据ID删除联系人
     */
    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContactById(contactId: String)
    
    /**
     * 切换收藏状态
     */
    @Query("UPDATE contacts SET is_favorite = NOT is_favorite WHERE id = :contactId")
    suspend fun toggleFavorite(contactId: String)
    
    /**
     * 获取联系人总数
     */
    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getContactCount(): Int
    
    /**
     * 检查邮箱是否已存在
     */
    @Query("SELECT EXISTS(SELECT 1 FROM contacts WHERE email = :email LIMIT 1)")
    suspend fun emailExists(email: String): Boolean
}
