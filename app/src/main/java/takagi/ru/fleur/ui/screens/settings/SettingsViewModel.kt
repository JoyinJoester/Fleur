package takagi.ru.fleur.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import takagi.ru.fleur.domain.model.SwipeAction
import takagi.ru.fleur.domain.model.ThemeMode
import takagi.ru.fleur.domain.repository.PreferencesRepository
import javax.inject.Inject

/**
 * 设置页面 ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val syncQueueManager: takagi.ru.fleur.data.sync.SyncQueueManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadPreferences()
    }
    
    /**
     * 加载偏好设置
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.getUserPreferences().collect { preferences ->
                _uiState.value = SettingsUiState(
                    themeMode = preferences.themeMode,
                    useDynamicColor = preferences.useDynamicColor,
                    syncInterval = preferences.syncInterval,
                    notificationsEnabled = preferences.notificationsEnabled,
                    notificationSound = preferences.notificationSound,
                    notificationVibration = preferences.notificationVibration,
                    swipeRightAction = preferences.swipeRightAction,
                    swipeLeftAction = preferences.swipeLeftAction
                )
            }
        }
    }
    
    /**
     * 设置主题模式
     */
    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(themeMode)
        }
    }
    
    /**
     * 设置动态配色
     */
    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDynamicColor(enabled)
        }
    }
    
    /**
     * 设置同步间隔
     */
    fun setSyncInterval(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setSyncInterval(minutes)
        }
    }
    
    /**
     * 设置通知开关
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled)
        }
    }
    
    /**
     * 设置通知声音
     */
    fun setNotificationSound(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationSound(enabled)
        }
    }
    
    /**
     * 设置通知震动
     */
    fun setNotificationVibration(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationVibration(enabled)
        }
    }
    
    /**
     * 设置右滑操作
     */
    fun setSwipeRightAction(action: SwipeAction) {
        viewModelScope.launch {
            preferencesRepository.setSwipeRightAction(action)
        }
    }
    
    /**
     * 设置左滑操作
     */
    fun setSwipeLeftAction(action: SwipeAction) {
        viewModelScope.launch {
            preferencesRepository.setSwipeLeftAction(action)
        }
    }
    
    /**
     * 重置为默认设置
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            preferencesRepository.resetToDefaults()
        }
    }
    
    /**
     * 设置 WebDAV 启用状态（本地优先架构）
     */
    fun setWebdavEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setWebDAVEnabled(enabled)
            _uiState.value = _uiState.value.copy(webdavEnabled = enabled)
        }
    }
    
    /**
     * 触发手动同步（本地优先架构）
     */
    fun triggerManualSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncError = null)
            
            try {
                // 处理同步队列
                val successCount = syncQueueManager.processSyncQueue()
                
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    lastSyncTime = "刚刚",
                    syncError = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncError = e.message ?: "同步失败"
                )
            }
        }
    }
    
    /**
     * 显示 WebDAV 配置对话框
     */
    fun showWebdavConfigDialog() {
        // TODO: 实现 WebDAV 配置对话框
        // 这里可以导航到专门的 WebDAV 配置页面
    }
}

/**
 * 设置页面 UI 状态（本地优先架构）
 */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val syncInterval: Int = 15,
    val notificationsEnabled: Boolean = true,
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val swipeRightAction: SwipeAction = SwipeAction.ARCHIVE,
    val swipeLeftAction: SwipeAction = SwipeAction.DELETE,
    // WebDAV 同步配置（本地优先架构）
    val webdavEnabled: Boolean = false,
    val webdavUrl: String = "",
    val webdavUsername: String = "",
    val isSyncing: Boolean = false,
    val pendingSyncCount: Int = 0,
    val lastSyncTime: String? = null,
    val syncError: String? = null
)
