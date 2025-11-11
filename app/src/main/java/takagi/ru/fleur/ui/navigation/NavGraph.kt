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
                onNavigateToReply = { replyToId ->
                    navController.navigate(Screen.Compose.createRouteWithReply(replyToId))
                }
            )
        }
        
        // 撰写邮件
        composable(
            route = Screen.Compose.route,
            arguments = listOf(
                navArgument("replyToId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("forwardId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val replyToId = backStackEntry.arguments?.getString("replyToId")
            val forwardId = backStackEntry.arguments?.getString("forwardId")
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
