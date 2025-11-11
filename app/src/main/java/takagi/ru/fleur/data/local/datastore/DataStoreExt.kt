package takagi.ru.fleur.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore 扩展
 * 创建单例的 DataStore 实例
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fleur_preferences")
