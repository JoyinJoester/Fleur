package takagi.ru.fleur.domain.model

/**
 * 通知偏好设置
 */
data class NotificationPreferences(
    val enabled: Boolean = true,
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val priority: NotificationPriority = NotificationPriority.HIGH,
    val showPreview: Boolean = true,
    val groupByAccount: Boolean = true
)

/**
 * 通知优先级
 */
enum class NotificationPriority {
    LOW,
    DEFAULT,
    HIGH
}
