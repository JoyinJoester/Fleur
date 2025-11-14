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
     * 可选参数: mode (撰写模式), referenceId (引用邮件ID)
     * 
     * mode 可选值: NEW, REPLY, REPLY_ALL, FORWARD, DRAFT
     */
    object Compose : Screen("compose?mode={mode}&referenceId={referenceId}") {
        /**
         * 创建撰写邮件路由
         * 
         * @param mode 撰写模式 (NEW, REPLY, REPLY_ALL, FORWARD, DRAFT)
         * @param referenceId 引用的邮件ID（回复、转发或草稿时使用）
         * @return 完整的路由字符串
         */
        fun createRoute(mode: String? = null, referenceId: String? = null): String {
            val params = mutableListOf<String>()
            mode?.let { params.add("mode=$it") }
            referenceId?.let { params.add("referenceId=$it") }
            return if (params.isEmpty()) "compose" else "compose?${params.joinToString("&")}"
        }
        
        /**
         * 创建回复路由
         * 
         * @param emailId 要回复的邮件ID
         * @return 回复模式的路由字符串
         */
        fun createReplyRoute(emailId: String) = createRoute(mode = "REPLY", referenceId = emailId)
        
        /**
         * 创建全部回复路由
         * 
         * @param emailId 要回复的邮件ID
         * @return 全部回复模式的路由字符串
         */
        fun createReplyAllRoute(emailId: String) = createRoute(mode = "REPLY_ALL", referenceId = emailId)
        
        /**
         * 创建转发路由
         * 
         * @param emailId 要转发的邮件ID
         * @return 转发模式的路由字符串
         */
        fun createForwardRoute(emailId: String) = createRoute(mode = "FORWARD", referenceId = emailId)
        
        /**
         * 创建草稿编辑路由
         * 
         * @param draftId 草稿邮件ID
         * @return 草稿编辑模式的路由字符串
         */
        fun createDraftRoute(draftId: String) = createRoute(mode = "DRAFT", referenceId = draftId)
        
        // 保留旧方法以兼容现有代码
        @Deprecated("使用 createReplyRoute 替代", ReplaceWith("createReplyRoute(replyToId)"))
        fun createRouteWithReply(replyToId: String) = createReplyRoute(replyToId)
        
        @Deprecated("使用 createForwardRoute 替代", ReplaceWith("createForwardRoute(forwardId)"))
        fun createRouteWithForward(forwardId: String) = createForwardRoute(forwardId)
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
    
    /**
     * 对话列表（Chat）
     */
    object Chat : Screen("chat")
    
    /**
     * 对话详情
     * 参数: conversationId (联系人邮箱，需要URL编码)
     */
    object ChatDetail : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String): String {
            // 对邮箱地址进行URL编码以支持特殊字符
            val encodedId = java.net.URLEncoder.encode(conversationId, "UTF-8")
            return "chat/$encodedId"
        }
    }
    
    /**
     * 联系人
     */
    object Contacts : Screen("contacts")
    
    /**
     * 添加联系人
     */
    object AddContact : Screen("add_contact")
    
    /**
     * 图片查看器
     * 参数: messageId, imageIndex
     */
    object ImageViewer : Screen("image_viewer/{messageId}/{imageIndex}") {
        fun createRoute(messageId: String, imageIndex: Int) = 
            "image_viewer/$messageId/$imageIndex"
    }
}
