package takagi.ru.fleur.util

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import takagi.ru.fleur.data.local.FleurDatabase

/**
 * 测试数据插入工具
 * 用于快速插入测试邮件到数据库
 */
class TestDataInserter(private val context: Context) {
    
    private val database by lazy {
        Room.databaseBuilder(
            context,
            FleurDatabase::class.java,
            FleurDatabase.DATABASE_NAME
        ).build()
    }
    private val emailDao by lazy { database.emailDao() }
    
    /**
     * 插入测试邮件
     * 
     * @param accountId 账户ID
     * @param count 邮件数量
     * @return 插入的邮件数量
     */
    suspend fun insertTestEmails(
        accountId: String,
        count: Int = 20
    ): Int = withContext(Dispatchers.IO) {
        try {
            val emails = TestEmailGenerator.generateEmails(accountId, count)
            emailDao.insertEmails(emails)
            emails.size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    /**
     * 插入完整的测试套件
     * 包含各种场景的测试邮件
     * 
     * @param accountId 账户ID
     * @return 插入的邮件数量
     */
    suspend fun insertTestSuite(accountId: String): Int = withContext(Dispatchers.IO) {
        try {
            val emails = TestEmailGenerator.generateTestSuite(accountId)
            emailDao.insertEmails(emails)
            emails.size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    /**
     * 清空所有测试邮件
     * 删除ID以"test_email_"开头的邮件
     */
    suspend fun clearTestEmails() = withContext(Dispatchers.IO) {
        try {
            // 注意：这需要在 EmailDao 中添加相应的方法
            // 或者使用原始SQL查询
            database.clearAllTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    companion object {
        /**
         * 快速插入测试邮件的便捷方法
         * 
         * 使用示例：
         * ```kotlin
         * // 在 ViewModel 或 Activity 中
         * viewModelScope.launch {
         *     TestDataInserter.quickInsert(context, "account_123", 30)
         * }
         * ```
         */
        suspend fun quickInsert(
            context: Context,
            accountId: String,
            count: Int = 20
        ): Int {
            return TestDataInserter(context).insertTestEmails(accountId, count)
        }
        
        /**
         * 快速插入测试套件
         */
        suspend fun quickInsertSuite(
            context: Context,
            accountId: String
        ): Int {
            return TestDataInserter(context).insertTestSuite(accountId)
        }
    }
}
