package takagi.ru.fleur.domain.model

import androidx.compose.ui.graphics.Color

/**
 * 邮件账户模型
 * @property id 账户唯一标识
 * @property email 邮箱地址
 * @property displayName 显示名称
 * @property color 账户标识颜色
 * @property isDefault 是否为默认账户
 * @property imapConfig IMAP 服务器配置
 * @property smtpConfig SMTP 服务器配置
 * @property createdAt 账户创建时间戳（毫秒）
 */
data class Account(
    val id: String,
    val email: String,
    val displayName: String,
    val color: Color,
    val isDefault: Boolean = false,
    val imapConfig: ImapConfig,
    val smtpConfig: SmtpConfig,
    val createdAt: Long = System.currentTimeMillis()
)
