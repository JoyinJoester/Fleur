package takagi.ru.fleur.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.EmailAddress
import takagi.ru.fleur.domain.usecase.SendEmailUseCase
import takagi.ru.fleur.ui.components.FleurNavigationDrawer
import takagi.ru.fleur.ui.components.FleurBottomNavigationBar
import takagi.ru.fleur.ui.components.ComposeBottomSheet
import android.util.Log
import java.util.UUID

/**
 * 应用脚手架
 * 管理 Navigation Drawer 和主导航
 * 
 * @param navController 导航控制器
 * @param currentAccount 当前账户
 * @param accounts 账户列表
 * @param unreadCounts 未读数统计
 * @param onSwitchAccount 切换账户回调
 * @param sendEmailUseCase 发送邮件用例
 */
@Composable
fun AppScaffold(
    navController: NavHostController,
    currentAccount: Account?,
    accounts: List<Account>,
    unreadCounts: Map<String, Int> = emptyMap(),
    onSwitchAccount: (String) -> Unit,
    sendEmailUseCase: SendEmailUseCase,
    modifier: Modifier = Modifier
) {
    var drawerVisible by remember { mutableStateOf(false) }
    var selectedBottomItem by remember { mutableStateOf(0) }
    var showComposeSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                FleurBottomNavigationBar(
                    selectedItem = selectedBottomItem,
                    onItemSelected = { index ->
                        selectedBottomItem = index
                        when (index) {
                            0 -> navController.navigate(Screen.Inbox.route) {
                                popUpTo(Screen.Inbox.route) { inclusive = true }
                            }
                            1 -> { /* TODO: Chat 页面 */ }
                            2 -> { /* TODO: Contacts 页面 */ }
                            3 -> { /* TODO: Calendar 页面 */ }
                        }
                    }
                )
            }
        ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 主导航内容
            NavGraph(
                navController = navController,
                onMenuClick = { drawerVisible = true },
                onComposeClick = { showComposeSheet = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            
            // Navigation Drawer
            FleurNavigationDrawer(
                visible = drawerVisible,
                currentAccount = currentAccount,
                accounts = accounts,
                unreadCounts = unreadCounts,
                onDismiss = { drawerVisible = false },
                onNavigateToInbox = {
                    navController.navigate(Screen.Inbox.route) {
                        popUpTo(Screen.Inbox.route) { inclusive = true }
                    }
                    selectedBottomItem = 0
                },
                onNavigateToSent = {
                    navController.navigate(Screen.Sent.route)
                    drawerVisible = false
                },
                onNavigateToDrafts = {
                    navController.navigate(Screen.Drafts.route)
                    drawerVisible = false
                },
                onNavigateToStarred = {
                    navController.navigate(Screen.Starred.route)
                    drawerVisible = false
                },
                onNavigateToArchive = {
                    navController.navigate(Screen.Archive.route)
                    drawerVisible = false
                },
                onNavigateToTrash = {
                    navController.navigate(Screen.Trash.route)
                    drawerVisible = false
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAccountManagement = {
                    navController.navigate(Screen.AccountManagement.route)
                },
                onSwitchAccount = onSwitchAccount
            )
            }
        }
        
        // 撰写邮件 Bottom Sheet - 在最外层，覆盖底部导航栏
        ComposeBottomSheet(
            visible = showComposeSheet,
            onDismiss = { showComposeSheet = false },
            onSend = { to, subject, bodyPlain, bodyMarkdown, bodyHtml, contentType ->
                scope.launch {
                    try {
                        // 检查当前账户
                        if (currentAccount == null) {
                            snackbarHostState.showSnackbar("请先登录账户")
                            return@launch
                        }
                        
                        // 解析收件人
                        val toAddresses = parseEmailAddresses(to)
                        if (toAddresses.isEmpty()) {
                            snackbarHostState.showSnackbar("收件人格式错误")
                            return@launch
                        }
                        
                        // 创建邮件对象
                        val email = Email(
                            id = UUID.randomUUID().toString(),
                            threadId = UUID.randomUUID().toString(),
                            accountId = currentAccount.id,
                            from = EmailAddress(
                                name = currentAccount.displayName,
                                address = currentAccount.email
                            ),
                            to = toAddresses,
                            cc = emptyList(),
                            bcc = emptyList(),
                            subject = subject,
                            bodyPreview = bodyPlain.take(200),
                            bodyPlain = bodyPlain,
                            bodyHtml = bodyHtml,
                            bodyMarkdown = bodyMarkdown,
                            contentType = contentType,
                            attachments = emptyList(),
                            timestamp = Clock.System.now(),
                            isRead = true,
                            isStarred = false,
                            labels = emptyList()
                        )
                        
                        Log.d("AppScaffold", "发送邮件: ${email.subject} to ${email.to.joinToString()}")
                        
                        // 发送邮件
                        val result = sendEmailUseCase(email)
                        
                        result.fold(
                            onSuccess = {
                                Log.d("AppScaffold", "邮件发送成功")
                                snackbarHostState.showSnackbar("邮件已发送")
                                showComposeSheet = false
                            },
                            onFailure = { error ->
                                Log.e("AppScaffold", "邮件发送失败: ${error.message}", error)
                                snackbarHostState.showSnackbar("发送失败: ${error.message}")
                            }
                        )
                    } catch (e: Exception) {
                        Log.e("AppScaffold", "发送邮件异常", e)
                        snackbarHostState.showSnackbar("发送失败: ${e.message}")
                    }
                }
            }
        )
    }
}

/**
 * 解析邮件地址字符串
 * 支持逗号、分号分隔
 */
private fun parseEmailAddresses(addresses: String): List<EmailAddress> {
    return addresses.split("[,;]".toRegex())
        .map { it.trim() }
        .filter { it.isNotBlank() && it.contains("@") }
        .map { address ->
            EmailAddress(
                name = address.substringBefore("@"),
                address = address
            )
        }
}


