package takagi.ru.fleur.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.ImapConfig
import takagi.ru.fleur.domain.model.SmtpConfig

/**
 * FleurNavigationDrawer 使用示例
 * 
 * 此文件展示如何在应用中集成和使用 Navigation Drawer
 */

/**
 * 示例 1: 基本使用
 * 
 * 在主界面中集成 Navigation Drawer
 */
@Composable
fun NavigationDrawerBasicExample() {
    var drawerVisible by remember { mutableStateOf(false) }
    
    // 模拟账户数据
    val currentAccount = Account(
        id = "1",
        email = "user@example.com",
        displayName = "用户名",
        color = Color(0xFF1976D2),
        isDefault = true,
        imapConfig = ImapConfig(
            host = "imap.example.com",
            port = 993,
            useSsl = true,
            username = "user@example.com"
        ),
        smtpConfig = SmtpConfig(
            host = "smtp.example.com",
            port = 465,
            useSsl = true,
            username = "user@example.com"
        )
    )
    
    val accounts = listOf(
        currentAccount,
        Account(
            id = "2",
            email = "work@company.com",
            displayName = "工作邮箱",
            color = Color(0xFF4CAF50),
            isDefault = false,
            imapConfig = ImapConfig(
                host = "imap.company.com",
                port = 993,
                useSsl = true,
                username = "work@company.com"
            ),
            smtpConfig = SmtpConfig(
                host = "smtp.company.com",
                port = 465,
                useSsl = true,
                username = "work@company.com"
            )
        )
    )
    
    // 未读数统计
    val unreadCounts = mapOf(
        "inbox" to 23,
        "drafts" to 2
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 主内容区域
        // YourMainContent(onMenuClick = { drawerVisible = true })
        
        // Navigation Drawer
        FleurNavigationDrawer(
            visible = drawerVisible,
            currentAccount = currentAccount,
            accounts = accounts,
            unreadCounts = unreadCounts,
            onDismiss = { drawerVisible = false },
            onNavigateToInbox = {
                // 导航到收件箱
                drawerVisible = false
            },
            onNavigateToSent = {
                // 导航到已发送
                drawerVisible = false
            },
            onNavigateToDrafts = {
                // 导航到草稿箱
                drawerVisible = false
            },
            onNavigateToStarred = {
                // 导航到星标邮件
                drawerVisible = false
            },
            onNavigateToArchive = {
                // 导航到归档
                drawerVisible = false
            },
            onNavigateToTrash = {
                // 导航到垃圾箱
                drawerVisible = false
            },
            onNavigateToSettings = {
                // 导航到设置
                drawerVisible = false
            },
            onNavigateToAccountManagement = {
                // 导航到账户管理
                drawerVisible = false
            },
            onSwitchAccount = { accountId ->
                // 切换账户
                println("切换到账户: $accountId")
            }
        )
    }
}

/**
 * 示例 2: 自适应布局
 * 
 * 根据屏幕宽度自动显示/隐藏 Navigation Drawer
 * 宽度 >= 600dp: 始终显示
 * 宽度 < 600dp: 通过菜单按钮切换
 */
@Composable
fun NavigationDrawerAdaptiveExample() {
    // TODO: 实现自适应布局逻辑
    // 使用 WindowSizeClass 或 BoxWithConstraints 检测屏幕宽度
    // if (screenWidth >= 600.dp) {
    //     // 显示持久化的 Navigation Drawer
    // } else {
    //     // 使用模态 Navigation Drawer
    // }
}

/**
 * 示例 3: 集成到 ViewModel
 * 
 * 从 ViewModel 获取账户和未读数数据
 */
@Composable
fun NavigationDrawerWithViewModelExample() {
    // val viewModel: MainViewModel = hiltViewModel()
    // val accounts by viewModel.accounts.collectAsState()
    // val currentAccount by viewModel.currentAccount.collectAsState()
    // val unreadCounts by viewModel.unreadCounts.collectAsState()
    
    // FleurNavigationDrawer(
    //     visible = drawerVisible,
    //     currentAccount = currentAccount,
    //     accounts = accounts,
    //     unreadCounts = unreadCounts,
    //     ...
    // )
}

/**
 * 关键特性说明:
 * 
 * 1. 毛玻璃效果
 *    - 使用 drawerGlassmorphism() 修饰符
 *    - 10dp blur + 85% opacity
 *    - 自动适配浅色/深色模式
 * 
 * 2. 滑入动画
 *    - 250ms 动画时长
 *    - DecelerateEasing 缓动曲线
 *    - 从左侧滑入/滑出
 * 
 * 3. 背景遮罩
 *    - 40% black opacity
 *    - 点击遮罩关闭抽屉
 * 
 * 4. 未读数 Badge
 *    - 显示在文件夹项右侧
 *    - 超过 99 显示 "99+"
 *    - 使用 Material 3 Badge 组件
 * 
 * 5. 账户切换
 *    - 显示账户颜色指示器
 *    - 默认账户显示星标
 *    - 支持快速切换账户
 * 
 * 6. 自适应布局
 *    - 宽度 >= 600dp: 持久化显示（TODO）
 *    - 宽度 < 600dp: 模态显示
 */
