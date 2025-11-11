package takagi.ru.fleur.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import takagi.ru.fleur.ui.theme.FleurTheme
import takagi.ru.fleur.util.TestDataInserter

/**
 * 调试菜单 Activity
 * 仅在 DEBUG 模式下可用，用于快速插入测试数据
 * 
 * 在 AndroidManifest.xml 中添加：
 * ```xml
 * <activity
 *     android:name=".debug.DebugMenuActivity"
 *     android:exported="true">
 *     <intent-filter>
 *         <action android:name="android.intent.action.MAIN" />
 *         <category android:name="android.intent.category.LAUNCHER" />
 *     </intent-filter>
 * </activity>
 * ```
 */
class DebugMenuActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            FleurTheme {
                DebugMenuScreen(
                    onInsertTestEmails = { accountId, count ->
                        insertTestEmails(accountId, count)
                    },
                    onInsertTestSuite = { accountId ->
                        insertTestSuite(accountId)
                    },
                    onClearData = {
                        clearTestData()
                    }
                )
            }
        }
    }
    
    private fun insertTestEmails(accountId: String, count: Int) {
        lifecycleScope.launch {
            try {
                val inserted = TestDataInserter.quickInsert(
                    context = this@DebugMenuActivity,
                    accountId = accountId,
                    count = count
                )
                println("✅ 成功插入 $inserted 条测试邮件")
            } catch (e: Exception) {
                println("❌ 插入失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    private fun insertTestSuite(accountId: String) {
        lifecycleScope.launch {
            try {
                val inserted = TestDataInserter.quickInsertSuite(
                    context = this@DebugMenuActivity,
                    accountId = accountId
                )
                println("✅ 成功插入测试套件，共 $inserted 条邮件")
            } catch (e: Exception) {
                println("❌ 插入失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    private fun clearTestData() {
        lifecycleScope.launch {
            try {
                TestDataInserter(this@DebugMenuActivity).clearTestEmails()
                println("✅ 已清空测试数据")
            } catch (e: Exception) {
                println("❌ 清空失败: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}

/**
 * 调试菜单界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenuScreen(
    onInsertTestEmails: (accountId: String, count: Int) -> Unit,
    onInsertTestSuite: (accountId: String) -> Unit,
    onClearData: () -> Unit
) {
    var accountId by remember { mutableStateOf("test_account_001") }
    var emailCount by remember { mutableStateOf("20") }
    var statusMessage by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("调试菜单 - 测试数据") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 账户ID输入
            OutlinedTextField(
                value = accountId,
                onValueChange = { accountId = it },
                label = { Text("账户 ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 邮件数量输入
            OutlinedTextField(
                value = emailCount,
                onValueChange = { emailCount = it },
                label = { Text("邮件数量") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Divider()
            
            // 插入测试邮件按钮
            Button(
                onClick = {
                    val count = emailCount.toIntOrNull() ?: 20
                    onInsertTestEmails(accountId, count)
                    statusMessage = "正在插入 $count 条测试邮件..."
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("插入测试邮件")
            }
            
            // 插入测试套件按钮
            Button(
                onClick = {
                    onInsertTestSuite(accountId)
                    statusMessage = "正在插入测试套件..."
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("插入测试套件（推荐）")
            }
            
            Divider()
            
            // 清空数据按钮
            OutlinedButton(
                onClick = {
                    onClearData()
                    statusMessage = "正在清空测试数据..."
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("清空所有数据")
            }
            
            // 状态消息
            if (statusMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = statusMessage,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 说明文本
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "使用说明",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = """
                        1. 输入账户ID（确保账户已存在）
                        2. 选择插入方式：
                           • 测试邮件：插入指定数量的随机邮件
                           • 测试套件：插入包含各种场景的邮件集合
                        3. 查看 Logcat 确认插入结果
                        4. 测试完成后可清空数据
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
