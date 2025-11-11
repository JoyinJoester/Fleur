package takagi.ru.fleur.ui.navigation

/**
 * 应用路由定义
 */
sealed class Screen(val route: String) {
    /**
     * 收件箱
     */
    object Inbox : Screen("inbox")
    
    /**
     * 邮件详情
     * 参数: emailId
     */
    object EmailDetail : Screen("email/{emailId}") {
        fun createRoute(emailId: String) = "email/$emailId"
    }
    
    /**
     * 撰写邮件
     * 可选参数: replyToId, forwardId
     */
    object Compose : Screen("compose?replyToId={replyToId}&forwardId={forwardId}") {
        fun createRoute(replyToId: String? = null, forwardId: String? = null): String {
            val params = mutableListOf<String>()
            replyToId?.let { params.add("replyToId=$it") }
            forwardId?.let { params.add("forwardId=$it") }
            return if (params.isEmpty()) "compose" else "compose?${params.joinToString("&")}"
        }
        
        fun createRouteWithReply(replyToId: String) = createRoute(replyToId = replyToId)
        fun createRouteWithForward(forwardId: String) = createRoute(forwardId = forwardId)
    }
    
    /**
     * 搜索
     */
    object Search : Screen("search")
    
    /**
     * 账户管理
     */
    object AccountManagement : Screen("accounts")
    
    /**
     * 添加账户
     */
    object AddAccount : Screen("accounts/add")
    
    /**
     * 设置
     */
    object Settings : Screen("settings")
    
    /**
     * 已发送
     */
    object Sent : Screen("sent")
    
    /**
     * 草稿箱
     */
    object Drafts : Screen("drafts")
    
    /**
     * 星标邮件
     */
    object Starred : Screen("starred")
    
    /**
     * 归档
     */
    object Archive : Screen("archive")
    
    /**
     * 垃圾箱
     */
    object Trash : Screen("trash")
}
