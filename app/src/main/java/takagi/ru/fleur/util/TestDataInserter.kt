package takagi.ru.fleur.util

import android.content.Context
import android.util.Log
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
    
    companion object {
        private const val TAG = "TestDataInserter"
        
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
    
    /**
     * 删除所有测试邮件
     * 清除ID以"test_email_"开头的邮件
     * 
     * @return 删除的邮件数量
     */
    suspend fun deleteTestEmails(): Int = withContext(Dispatchers.IO) {
        try {
            val deletedCount = emailDao.deleteTestEmails()
            Log.i(TAG, "已删除 $deletedCount 封测试邮件")
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "删除测试邮件失败", e)
            0
        }
    }
    
    /**
     * 插入测试邮件
     * 在插入前会先清除旧的测试数据，避免重复
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
            // 先清除旧的测试邮件，避免重复
            val deletedCount = emailDao.deleteTestEmails()
            Log.i(TAG, "清除了 $deletedCount 封旧的测试邮件")
            
            // 生成并插入新的测试邮件
            val emails = TestEmailGenerator.generateEmails(accountId, count)
            emailDao.insertEmails(emails)
            
            // 记录插入的邮件ID（仅在调试模式下记录详细信息）
            Log.i(TAG, "成功插入 ${emails.size} 封测试邮件到账户 $accountId")
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                emails.forEachIndexed { index, email ->
                    Log.d(TAG, "  [$index] ID: ${email.id}, 主题: ${email.subject}")
                }
            }
            
            emails.size
        } catch (e: Exception) {
            Log.e(TAG, "插入测试邮件失败", e)
            0
        }
    }
    
    /**
     * 插入完整的测试套件
     * 包含各种场景的测试邮件（未读、星标、已读、线程等）
     * 在插入前会先清除旧的测试数据，避免重复
     * 
     * @param accountId 账户ID
     * @return 插入的邮件数量
     */
    suspend fun insertTestSuite(accountId: String): Int = withContext(Dispatchers.IO) {
        try {
            // 先清除旧的测试邮件，避免重复
            val deletedCount = emailDao.deleteTestEmails()
            Log.i(TAG, "清除了 $deletedCount 封旧的测试邮件")
            
            // 生成并插入测试套件
            val emails = TestEmailGenerator.generateTestSuite(accountId)
            emailDao.insertEmails(emails)
            
            // 记录插入的邮件ID和类型
            Log.i(TAG, "成功插入测试套件，共 ${emails.size} 封邮件到账户 $accountId")
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                val unreadCount = emails.count { !it.isRead }
                val starredCount = emails.count { it.isStarred }
                val threadIds = emails.map { it.threadId }.distinct()
                Log.d(TAG, "  未读: $unreadCount, 星标: $starredCount, 线程数: ${threadIds.size}")
                emails.forEachIndexed { index, email ->
                    Log.d(TAG, "  [$index] ID: ${email.id}, 主题: ${email.subject}")
                }
            }
            
            emails.size
        } catch (e: Exception) {
            Log.e(TAG, "插入测试套件失败", e)
            0
        }
    }
    
}
