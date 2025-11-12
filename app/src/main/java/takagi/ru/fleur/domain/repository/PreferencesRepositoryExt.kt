package takagi.ru.fleur.domain.repository

import kotlinx.coroutines.flow.first

/**
 * PreferencesRepository 扩展函数
 * 提供便捷的 WebDAV 配置检查方法
 */

/**
 * 检查 WebDAV 是否已启用且配置完整
 * 
 * 此方法会检查：
 * 1. WebDAV 是否已启用
 * 2. WebDAV URL 是否已配置
 * 3. WebDAV 用户名是否已配置
 * 
 * @return 如果 WebDAV 已启用且配置完整则返回 true，否则返回 false
 */
suspend fun PreferencesRepository.isWebDAVEnabled(): Boolean {
    val preferences = getUserPreferences().first()
    return preferences.webdavEnabled && 
           preferences.webdavUrl.isNotBlank() &&
           preferences.webdavUsername.isNotBlank()
}

/**
 * 获取 WebDAV 配置信息
 * 
 * @return WebDAV 配置的三元组 (enabled, url, username)
 */
suspend fun PreferencesRepository.getWebDAVConfig(): Triple<Boolean, String, String> {
    val preferences = getUserPreferences().first()
    return Triple(
        preferences.webdavEnabled,
        preferences.webdavUrl,
        preferences.webdavUsername
    )
}
