package takagi.ru.fleur.domain.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.domain.model.Contact

/**
 * 联系人仓库接口
 * 提供联系人的增删改查功能
 */
interface ContactRepository {
    
    /**
     * 获取所有已保存的联系人(按姓名排序)
     */
    fun getAllContacts(): Flow<List<Contact>>
    
    /**
     * 根据 ID 获取联系人
     */
    fun getContactById(contactId: String): Flow<Contact?>
    
    /**
     * 根据邮箱获取联系人
     */
    suspend fun getContactByEmail(email: String): Contact?
    
    /**
     * 获取所有收藏的联系人
     */
    fun getFavoriteContacts(): Flow<List<Contact>>
    
    /**
     * 搜索联系人(姓名、邮箱、组织)
     */
    fun searchContacts(query: String): Flow<List<Contact>>
    
    /**
     * 添加新联系人
     */
    suspend fun addContact(contact: Contact): Result<Unit>
    
    /**
     * 批量添加联系人
     */
    suspend fun addContacts(contacts: List<Contact>): Result<Unit>
    
    /**
     * 更新联系人
     */
    suspend fun updateContact(contact: Contact): Result<Unit>
    
    /**
     * 删除联系人
     */
    suspend fun deleteContact(contact: Contact): Result<Unit>
    
    /**
     * 根据 ID 删除联系人
     */
    suspend fun deleteContactById(contactId: String): Result<Unit>
    
    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(contactId: String): Result<Unit>
    
    /**
     * 检查邮箱是否已存在
     */
    suspend fun emailExists(email: String): Boolean
    
    /**
     * 获取联系人总数
     */
    suspend fun getContactCount(): Int
}
