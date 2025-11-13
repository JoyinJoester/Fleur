package takagi.ru.fleur.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.data.local.dao.ContactDao
import takagi.ru.fleur.data.local.mapper.EntityMapper
import takagi.ru.fleur.domain.model.Contact
import takagi.ru.fleur.domain.repository.ContactRepository
import takagi.ru.fleur.util.PinyinUtils
import javax.inject.Inject

/**
 * 联系人仓库实现
 */
class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao
) : ContactRepository {
    
    override fun getAllContacts(): Flow<List<Contact>> {
        return contactDao.getAllContacts()
            .map { entities -> entities.map { EntityMapper.toContact(it) } }
    }
    
    override fun getContactById(contactId: String): Flow<Contact?> {
        return contactDao.getContactById(contactId)
            .map { it?.let { entity -> EntityMapper.toContact(entity) } }
    }
    
    override suspend fun getContactByEmail(email: String): Contact? {
        return contactDao.getContactByEmail(email)
            ?.let { EntityMapper.toContact(it) }
    }
    
    override fun getFavoriteContacts(): Flow<List<Contact>> {
        return contactDao.getFavoriteContacts()
            .map { entities -> entities.map { EntityMapper.toContact(it) } }
    }
    
    override fun searchContacts(query: String): Flow<List<Contact>> {
        // 获取所有联系人,然后使用拼音工具进行过滤
        return contactDao.getAllContacts()
            .map { entities -> 
                entities
                    .map { EntityMapper.toContact(it) }
                    .filter { contact ->
                        // 支持姓名拼音/首字母、邮箱、组织搜索
                        PinyinUtils.matches(contact.name, query) ||
                        contact.email.contains(query, ignoreCase = true) ||
                        contact.organization?.let { org -> 
                            PinyinUtils.matches(org, query) 
                        } ?: false
                    }
            }
    }
    
    override suspend fun addContact(contact: Contact): Result<Unit> {
        return try {
            contactDao.insertContact(EntityMapper.toContactEntity(contact))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addContacts(contacts: List<Contact>): Result<Unit> {
        return try {
            contactDao.insertContacts(contacts.map { EntityMapper.toContactEntity(it) })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateContact(contact: Contact): Result<Unit> {
        return try {
            contactDao.updateContact(EntityMapper.toContactEntity(contact))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteContact(contact: Contact): Result<Unit> {
        return try {
            contactDao.deleteContact(EntityMapper.toContactEntity(contact))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteContactById(contactId: String): Result<Unit> {
        return try {
            contactDao.deleteContactById(contactId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun toggleFavorite(contactId: String): Result<Unit> {
        return try {
            contactDao.toggleFavorite(contactId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun emailExists(email: String): Boolean {
        return contactDao.emailExists(email)
    }
    
    override suspend fun getContactCount(): Int {
        return contactDao.getContactCount()
    }
}
