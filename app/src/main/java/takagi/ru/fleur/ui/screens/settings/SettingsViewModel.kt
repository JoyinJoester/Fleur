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
    private val preferencesRepository: PreferencesRepository
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
}

/**
 * 设置页面 UI 状态
 */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val syncInterval: Int = 15,
    val notificationsEnabled: Boolean = true,
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val swipeRightAction: SwipeAction = SwipeAction.ARCHIVE,
    val swipeLeftAction: SwipeAction = SwipeAction.DELETE
)
