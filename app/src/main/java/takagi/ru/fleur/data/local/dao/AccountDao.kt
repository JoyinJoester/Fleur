package takagi.ru.fleur.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.data.local.entity.AccountEntity

/**
 * 账户 DAO
 * 提供账户的数据库操作
 */
@Dao
interface AccountDao {
    
    /**
     * 获取所有账户
     */
    @Query("SELECT * FROM accounts ORDER BY is_default DESC, email ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>
    
    /**
     * 根据ID获取账户
     */
    @Query("SELECT * FROM accounts WHERE id = :accountId")
    fun getAccountById(accountId: String): Flow<AccountEntity?>
    
    /**
     * 获取默认账户
     */
    @Query("SELECT * FROM accounts WHERE is_default = 1 LIMIT 1")
    fun getDefaultAccount(): Flow<AccountEntity?>
    
    /**
     * 根据邮箱地址获取账户
     */
    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): AccountEntity?
    
    /**
     * 插入账户
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)
    
    /**
     * 更新账户
     */
    @Update
    suspend fun updateAccount(account: AccountEntity)
    
    /**
     * 删除账户
     */
    @Delete
    suspend fun deleteAccount(account: AccountEntity)
    
    /**
     * 根据ID删除账户
     */
    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: String)
    
    /**
     * 清除所有账户的默认状态
     */
    @Query("UPDATE accounts SET is_default = 0")
    suspend fun clearDefaultAccounts()
    
    /**
     * 设置默认账户
     */
    @Query("UPDATE accounts SET is_default = 1 WHERE id = :accountId")
    suspend fun setDefaultAccount(accountId: String)
    
    /**
     * 获取账户数量
     */
    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int
}
