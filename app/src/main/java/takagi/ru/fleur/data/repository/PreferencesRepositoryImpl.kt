package takagi.ru.fleur.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import takagi.ru.fleur.data.local.datastore.dataStore
import takagi.ru.fleur.domain.model.SwipeAction
import takagi.ru.fleur.domain.model.ThemeMode
import takagi.ru.fleur.domain.model.UserPreferences
import takagi.ru.fleur.domain.model.ViewMode
import takagi.ru.fleur.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 偏好设置仓库实现
 * 使用 DataStore 存储用户偏好
 */
@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {
    
    private val dataStore = context.dataStore
    
    override fun getUserPreferences(): Flow<UserPreferences> {
        return dataStore.data.map { preferences ->
            UserPreferences(
                viewMode = ViewMode.valueOf(
                    preferences[KEY_VIEW_MODE] ?: ViewMode.LIST.name
                ),
                themeMode = ThemeMode.valueOf(
                    preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
                ),
                useDynamicColor = preferences[KEY_USE_DYNAMIC_COLOR] ?: true,
                syncInterval = preferences[KEY_SYNC_INTERVAL] ?: 15,
                notificationsEnabled = preferences[KEY_NOTIFICATIONS_ENABLED] ?: true,
                notificationSound = preferences[KEY_NOTIFICATION_SOUND] ?: true,
                notificationVibration = preferences[KEY_NOTIFICATION_VIBRATION] ?: true,
                swipeRightAction = SwipeAction.valueOf(
                    preferences[KEY_SWIPE_RIGHT_ACTION] ?: SwipeAction.ARCHIVE.name
                ),
                swipeLeftAction = SwipeAction.valueOf(
                    preferences[KEY_SWIPE_LEFT_ACTION] ?: SwipeAction.DELETE.name
                )
            )
        }
    }
    
    override suspend fun setViewMode(viewMode: ViewMode) {
        dataStore.edit { preferences ->
            preferences[KEY_VIEW_MODE] = viewMode.name
        }
    }
    
    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = themeMode.name
        }
    }
    
    override suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_USE_DYNAMIC_COLOR] = enabled
        }
    }
    
    override suspend fun setSyncInterval(intervalMinutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_SYNC_INTERVAL] = intervalMinutes
        }
    }
    
    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    override suspend fun setNotificationSound(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_SOUND] = enabled
        }
    }
    
    override suspend fun setNotificationVibration(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_VIBRATION] = enabled
        }
    }
    
    override suspend fun setSwipeRightAction(action: SwipeAction) {
        dataStore.edit { preferences ->
            preferences[KEY_SWIPE_RIGHT_ACTION] = action.name
        }
    }
    
    override suspend fun setSwipeLeftAction(action: SwipeAction) {
        dataStore.edit { preferences ->
            preferences[KEY_SWIPE_LEFT_ACTION] = action.name
        }
    }
    
    override suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    companion object {
        private val KEY_VIEW_MODE = stringPreferencesKey("view_mode")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        private val KEY_SYNC_INTERVAL = intPreferencesKey("sync_interval")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_NOTIFICATION_SOUND = booleanPreferencesKey("notification_sound")
        private val KEY_NOTIFICATION_VIBRATION = booleanPreferencesKey("notification_vibration")
        private val KEY_SWIPE_RIGHT_ACTION = stringPreferencesKey("swipe_right_action")
        private val KEY_SWIPE_LEFT_ACTION = stringPreferencesKey("swipe_left_action")
    }
}
