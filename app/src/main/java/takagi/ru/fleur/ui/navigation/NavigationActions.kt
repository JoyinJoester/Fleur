package takagi.ru.fleur.ui.navigation

import androidx.navigation.NavHostController

/**
 * 导航操作封装
 * 提供类型安全的导航方法
 */
class NavigationActions(private val navController: NavHostController) {
    
    /**
     * 导航到收件箱
     */
    fun navigateToInbox() {
        navController.navigate(Screen.Inbox.route) {
            popUpTo(Screen.Inbox.route) { inclusive = true }
        }
    }
    
    /**
     * 导航到邮件详情
     */
    fun navigateToEmailDetail(emailId: String) {
        navController.navigate(Screen.EmailDetail.createRoute(emailId))
    }
    
    /**
     * 导航到撰写邮件
     */
    fun navigateToCompose(replyToId: String? = null, forwardId: String? = null) {
        navController.navigate(Screen.Compose.createRoute(replyToId, forwardId))
    }
    
    /**
     * 导航到搜索
     */
    fun navigateToSearch() {
        navController.navigate(Screen.Search.route)
    }
    
    /**
     * 导航到账户管理
     */
    fun navigateToAccountManagement() {
        navController.navigate(Screen.AccountManagement.route)
    }
    
    /**
     * 导航到添加账户
     */
    fun navigateToAddAccount() {
        navController.navigate(Screen.AddAccount.route)
    }
    
    /**
     * 导航到设置
     */
    fun navigateToSettings() {
        navController.navigate(Screen.Settings.route)
    }
    
    /**
     * 返回上一页
     */
    fun navigateBack() {
        navController.popBackStack()
    }
}
