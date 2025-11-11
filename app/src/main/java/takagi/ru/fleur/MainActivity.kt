package takagi.ru.fleur

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import takagi.ru.fleur.domain.usecase.SendEmailUseCase
import takagi.ru.fleur.notification.NotificationService
import takagi.ru.fleur.ui.navigation.AppScaffold
import takagi.ru.fleur.ui.navigation.Screen
import takagi.ru.fleur.ui.theme.FleurTheme
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.fleur.ui.screens.inbox.InboxViewModel
import javax.inject.Inject

/**
 * Fleur 主Activity
 * 应用入口点
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var sendEmailUseCase: SendEmailUseCase
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // 权限结果处理
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 请求通知权限（Android 13+）
        requestNotificationPermission()
        
        setContent {
            FleurTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var startDestination by remember { mutableStateOf<String>(Screen.Inbox.route) }
                    
                    // 处理通知点击
                    LaunchedEffect(Unit) {
                        handleNotificationIntent(intent)?.let { destination ->
                            startDestination = destination
                        }
                    }
                    
                    // 使用 AppScaffold 以支持侧边栏
                    AppScaffold(
                        navController = navController,
                        currentAccount = null, // TODO: 从 ViewModel 获取当前账户
                        accounts = emptyList(), // TODO: 从 ViewModel 获取账户列表
                        unreadCounts = emptyMap(), // TODO: 从 ViewModel 获取未读数
                        onSwitchAccount = { /* TODO: 实现账户切换 */ },
                        sendEmailUseCase = sendEmailUseCase
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // 处理新的通知点击
        handleNotificationIntent(intent)
    }
    
    /**
     * 请求通知权限
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    /**
     * 处理通知 Intent
     */
    private fun handleNotificationIntent(intent: Intent?): String? {
        val emailId = intent?.getStringExtra(NotificationService.EXTRA_EMAIL_ID)
        val action = intent?.getStringExtra("action")
        
        return when {
            emailId != null && action == "reply" -> {
                // 跳转到撰写页面（回复）
                "compose/$emailId"
            }
            emailId != null -> {
                // 跳转到邮件详情页
                "email_detail/$emailId"
            }
            else -> null
        }
    }
}