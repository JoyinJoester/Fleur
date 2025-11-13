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
import android.util.Log

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
                            1 -> navController.navigate(Screen.Chat.route) {
                                popUpTo(Screen.Chat.route) { inclusive = true }
                            }
                            2 -> navController.navigate(Screen.Contacts.route) {
                                popUpTo(Screen.Contacts.route) { inclusive = true }
                            }
                            3 -> { /* TODO: Calendar 页面 */ }
                        }
                    }
                )
            }
        ) { paddingValues ->
            // 主导航内容
            NavGraph(
                navController = navController,
                onMenuClick = { drawerVisible = true },
                onComposeClick = { 
                    // Navigate to compose screen
                    navController.navigate(Screen.Compose.createRoute())
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            
        }
        
        // Navigation Drawer - 在 Scaffold 外层，覆盖所有内容
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


