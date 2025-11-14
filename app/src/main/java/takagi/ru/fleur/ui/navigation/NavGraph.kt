package takagi.ru.fleur.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import takagi.ru.fleur.ui.screens.account.AccountManagementScreen
import takagi.ru.fleur.ui.screens.account.AddAccountScreen
import takagi.ru.fleur.ui.screens.compose.ComposeScreen
import takagi.ru.fleur.ui.screens.detail.EmailDetailScreen
import takagi.ru.fleur.ui.screens.folder.ArchiveScreen
import takagi.ru.fleur.ui.screens.folder.DraftsScreen
import takagi.ru.fleur.ui.screens.folder.SentScreen
import takagi.ru.fleur.ui.screens.folder.StarredScreen
import takagi.ru.fleur.ui.screens.folder.TrashScreen
import takagi.ru.fleur.ui.screens.inbox.InboxScreen
import takagi.ru.fleur.ui.screens.search.SearchScreen
import takagi.ru.fleur.ui.screens.settings.SettingsScreen
import takagi.ru.fleur.ui.theme.pageEnterAnimation
import takagi.ru.fleur.ui.theme.pageExitAnimation

/**
 * 应用导航图
 * 
 * @param navController 导航控制器
 * @param startDestination 起始目的地
 * @param onMenuClick 菜单点击回调
 * @param modifier 修饰符
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Inbox.route,
    onMenuClick: () -> Unit = {},
    onComposeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { pageEnterAnimation() },
        exitTransition = { pageExitAnimation() }
    ) {
        // 收件箱
        composable(Screen.Inbox.route) {
            InboxScreen(
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToCompose = { onComposeClick() },
                onNavigateToEmailDetail = { emailId ->
                    navController.navigate(Screen.EmailDetail.createRoute(emailId))
                },
                onNavigateToAccountManagement = { navController.navigate(Screen.AccountManagement.route) },
                onMenuClick = onMenuClick
            )
        }
        
        // 邮件详情
        composable(
            route = Screen.EmailDetail.route,
            arguments = listOf(
                navArgument("emailId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val emailId = backStackEntry.arguments?.getString("emailId") ?: ""
            EmailDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCompose = { referenceEmailId, mode ->
                    // 根据撰写模式构建正确的路由
                    val route = when (mode) {
                        takagi.ru.fleur.domain.model.ComposeMode.REPLY -> 
                            Screen.Compose.createReplyRoute(referenceEmailId)
                        takagi.ru.fleur.domain.model.ComposeMode.REPLY_ALL -> 
                            Screen.Compose.createReplyAllRoute(referenceEmailId)
                        takagi.ru.fleur.domain.model.ComposeMode.FORWARD -> 
                            Screen.Compose.createForwardRoute(referenceEmailId)
                        takagi.ru.fleur.domain.model.ComposeMode.DRAFT -> 
                            Screen.Compose.createDraftRoute(referenceEmailId)
                        takagi.ru.fleur.domain.model.ComposeMode.NEW -> 
                            Screen.Compose.createRoute()
                    }
                    navController.navigate(route)
                }
            )
        }
        
        // 撰写邮件
        composable(
            route = Screen.Compose.route,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("referenceId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            // 参数会通过 SavedStateHandle 传递给 ViewModel
            ComposeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 搜索
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailDetail = { emailId ->
                    navController.navigate(Screen.EmailDetail.createRoute(emailId))
                }
            )
        }
        
        // 账户管理
        composable(Screen.AccountManagement.route) {
            AccountManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddAccount = { navController.navigate(Screen.AddAccount.route) }
            )
        }
        
        // 添加账户
        composable(Screen.AddAccount.route) {
            AddAccountScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 设置
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 已发送
        composable(Screen.Sent.route) {
            SentScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailDetail = { emailId ->
                    navController.navigate(Screen.EmailDetail.createRoute(emailId))
                }
            )
        }
        
        // 草稿箱
        composable(Screen.Drafts.route) {
            DraftsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailDetail = { emailId ->
                    // 草稿点击应该导航到撰写页面
                    navController.navigate(Screen.Compose.createRoute())
                },
                onNavigateToCompose = { onComposeClick() }
            )
        }
        
        // 星标邮件
        composable(Screen.Starred.route) {
            StarredScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailDetail = { emailId ->
                    navController.navigate(Screen.EmailDetail.createRoute(emailId))
                }
            )
        }
        
        // 归档
        composable(Screen.Archive.route) {
            ArchiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailDetail = { emailId ->
                    navController.navigate(Screen.EmailDetail.createRoute(emailId))
                }
            )
        }
        
        // 垃圾箱
        composable(Screen.Trash.route) {
            TrashScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailDetail = { emailId ->
                    navController.navigate(Screen.EmailDetail.createRoute(emailId))
                }
            )
        }
        
        // 对话列表（Chat）
        composable(Screen.Chat.route) {
            takagi.ru.fleur.ui.screens.chat.ChatScreen(
                onNavigateToDetail = { conversationId ->
                    navController.navigate(Screen.ChatDetail.createRoute(conversationId))
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onMenuClick = onMenuClick
            )
        }
        
        // 联系人
        composable(Screen.Contacts.route) {
            takagi.ru.fleur.ui.screens.contacts.ContactsScreen(
                navController = navController,
                onMenuClick = onMenuClick
            )
        }
        
        // 添加联系人
        composable(Screen.AddContact.route) {
            takagi.ru.fleur.ui.screens.contacts.AddContactScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 对话详情
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            takagi.ru.fleur.ui.screens.chat.ChatDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToImageViewer = { messageId, imageIndex ->
                    navController.navigate(
                        Screen.ImageViewer.createRoute(messageId, imageIndex)
                    )
                },
                onNavigateToCompose = { referenceEmailId, mode ->
                    navController.navigate(
                        Screen.Compose.createRoute(mode, referenceEmailId)
                    )
                }
            )
        }
        
        // 图片查看器
        composable(
            route = Screen.ImageViewer.route,
            arguments = listOf(
                navArgument("messageId") { type = NavType.StringType },
                navArgument("imageIndex") { type = NavType.IntType }
            ),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) { backStackEntry ->
            val messageId = backStackEntry.arguments?.getString("messageId") ?: ""
            val imageIndex = backStackEntry.arguments?.getInt("imageIndex") ?: 0
            
            takagi.ru.fleur.ui.screens.chat.ImageViewerScreen(
                messageId = messageId,
                initialImageIndex = imageIndex,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * 占位符界面
 * 用于开发阶段
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
