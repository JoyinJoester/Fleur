package takagi.ru.fleur.domain.repository

import kotlinx.coroutines.flow.Flow
import takagi.ru.fleur.domain.model.SwipeAction
import takagi.ru.fleur.domain.model.ThemeMode
import takagi.ru.fleur.domain.model.UserPreferences
import takagi.ru.fleur.domain.model.ViewMode

/**
 * 偏好设置仓库接口
 * 定义用户偏好设置的数据操作
 */
interface PreferencesRepository {
    
    /**
     * 获取用户偏好设置
     * @return Flow<UserPreferences> 偏好设置流
     */
    fun getUserPreferences(): Flow<UserPreferences>
    
    /**
     * 设置视图模式
     * @param viewMode 视图模式
     */
    suspend fun setViewMode(viewMode: ViewMode)
    
    /**
     * 设置主题模式
     * @param themeMode 主题模式
     */
    suspend fun setThemeMode(themeMode: ThemeMode)
    
    /**
     * 设置是否使用动态配色
     * @param enabled 是否启用
     */
    suspend fun setDynamicColor(enabled: Boolean)
    
    /**
     * 设置同步间隔
     * @param intervalMinutes 间隔时间（分钟）
     */
    suspend fun setSyncInterval(intervalMinutes: Int)
    
    /**
     * 设置是否启用通知
     * @param enabled 是否启用
     */
    suspend fun setNotificationsEnabled(enabled: Boolean)
    
    /**
     * 设置通知声音
     * @param enabled 是否启用
     */
    suspend fun setNotificationSound(enabled: Boolean)
    
    /**
     * 设置通知震动
     * @param enabled 是否启用
     */
    suspend fun setNotificationVibration(enabled: Boolean)
    
    /**
     * 设置右滑操作
     * @param action 滑动操作
     */
    suspend fun setSwipeRightAction(action: SwipeAction)
    
    /**
     * 设置左滑操作
     * @param action 滑动操作
     */
    suspend fun setSwipeLeftAction(action: SwipeAction)
    
    /**
     * 重置所有设置为默认值
     */
    suspend fun resetToDefaults()
    
    /**
     * 设置 WebDAV 启用状态
     * @param enabled 是否启用
     */
    suspend fun setWebDAVEnabled(enabled: Boolean)
    
    /**
     * 设置 WebDAV 服务器 URL
     * @param url 服务器 URL
     */
    suspend fun setWebDAVUrl(url: String)
    
    /**
     * 设置 WebDAV 用户名
     * @param username 用户名
     */
    suspend fun setWebDAVUsername(username: String)
    
    /**
     * 检查 WebDAV 是否已配置且启用
     * @return 是否已配置且启用
     */
    suspend fun isWebDAVConfigured(): Boolean
}
