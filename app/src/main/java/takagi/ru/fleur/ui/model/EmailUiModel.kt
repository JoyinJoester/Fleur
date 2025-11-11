package takagi.ru.fleur.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Instant

/**
 * 邮件 UI 模型
 * 用于在 UI 层展示邮件信息
 * 
 * @property id 邮件ID
 * @property threadId 线程ID
 * @property accountColor 账户颜色标识
 * @property fromName 发件人名称
 * @property fromAddress 发件人地址
 * @property subject 主题
 * @property preview 预览文本
 * @property timestamp 时间戳
 * @property isRead 是否已读
 * @property isStarred 是否星标
 * @property hasAttachments 是否有附件
 * @property attachmentCount 附件数量
 */
@Immutable
data class EmailUiModel(
    val id: String,
    val threadId: String,
    val accountColor: Color,
    val fromName: String,
    val fromAddress: String,
    val subject: String,
    val preview: String,
    val timestamp: Instant,
    val isRead: Boolean,
    val isStarred: Boolean,
    val hasAttachments: Boolean,
    val attachmentCount: Int = 0
)
