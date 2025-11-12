package takagi.ru.fleur.domain.model

/**
 * 用户偏好设置
 * @property viewMode 视图模式
 * @property themeMode 主题模式
 * @property useDynamicColor 是否使用动态配色
 * @property syncInterval 同步间隔（分钟）
 * @property notificationsEnabled 是否启用通知
 * @property notificationSound 是否启用通知声音
 * @property notificationVibration 是否启用通知震动
 * @property swipeRightAction 右滑操作
 * @property swipeLeftAction 左滑操作
 * @property webdavEnabled 是否启用 WebDAV 同步
 * @property webdavUrl WebDAV 服务器 URL
 * @property webdavUsername WebDAV 用户名
 */
data class UserPreferences(
    val viewMode: ViewMode = ViewMode.LIST,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val syncInterval: Int = 15,
    val notificationsEnabled: Boolean = true,
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val swipeRightAction: SwipeAction = SwipeAction.ARCHIVE,
    val swipeLeftAction: SwipeAction = SwipeAction.DELETE,
    // WebDAV 配置
    val webdavEnabled: Boolean = false,
    val webdavUrl: String = "",
    val webdavUsername: String = ""
)

/**
 * 视图模式
 */
enum class ViewMode {
    LIST,    // 传统列表视图
    CHAT     // 聊天气泡视图
}

/**
 * 主题模式
 */
enum class ThemeMode {
    LIGHT,   // 浅色模式
    DARK,    // 深色模式
    SYSTEM   // 跟随系统
}

/**
 * 滑动操作
 */
enum class SwipeAction {
    ARCHIVE,      // 归档
    DELETE,       // 删除
    MARK_READ,    // 标记已读
    MARK_UNREAD,  // 标记未读
    STAR          // 星标
}
