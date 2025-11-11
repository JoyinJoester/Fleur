package takagi.ru.fleur.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 安全凭证存储
 * 使用 EncryptedSharedPreferences 和 Android Keystore 加密存储密码
 */
@Singleton
class SecureCredentialStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * 保存账户密码
     * @param accountId 账户ID
     * @param password 密码（将被加密存储）
     */
    fun savePassword(accountId: String, password: String) {
        encryptedPrefs.edit()
            .putString(getPasswordKey(accountId), password)
            .apply()
    }
    
    /**
     * 获取账户密码
     * @param accountId 账户ID
     * @return 密码（解密后），如果不存在返回null
     */
    fun getPassword(accountId: String): String? {
        return encryptedPrefs.getString(getPasswordKey(accountId), null)
    }
    
    /**
     * 删除账户密码
     * @param accountId 账户ID
     */
    fun deletePassword(accountId: String) {
        encryptedPrefs.edit()
            .remove(getPasswordKey(accountId))
            .apply()
    }
    
    /**
     * 检查账户密码是否存在
     * @param accountId 账户ID
     * @return true表示密码已保存
     */
    fun hasPassword(accountId: String): Boolean {
        return encryptedPrefs.contains(getPasswordKey(accountId))
    }
    
    /**
     * 清除所有密码
     * 谨慎使用！
     */
    fun clearAllPasswords() {
        encryptedPrefs.edit().clear().apply()
    }
    
    /**
     * 获取密码存储的键名
     */
    private fun getPasswordKey(accountId: String): String {
        return "${KEY_PREFIX}$accountId"
    }
    
    companion object {
        private const val PREFS_NAME = "fleur_credentials"
        private const val KEY_PREFIX = "password_"
    }
}
