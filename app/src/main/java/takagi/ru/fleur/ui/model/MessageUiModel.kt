package takagi.ru.fleur.ui.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

/**
 * 消息 UI 模型
 * 用于在 ChatDetail 页面展示单条消息
 *
 * @property id 消息ID
 * @property conversationId 所属对话ID
 * @property senderId 发送者ID
 * @property senderName 发送者名称
 * @property senderAvatar 发送者头像
 * @property content 消息文本内容
 * @property timestamp 发送时间
 * @property status 消息状态
 * @property attachments 附件列表
 * @property replyTo 回复的消息
 * @property isRead 是否已读
 */
@Immutable
data class MessageUiModel(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String?,
    val content: String,
    val timestamp: Instant,
    val status: MessageStatus,
    val attachments: List<AttachmentUiModel>,
    val replyTo: MessageUiModel?,
    val isRead: Boolean
)

/**
 * 消息状态枚举
 */
enum class MessageStatus {
    SENDING,      // 发送中
    SENT,         // 已发送
    DELIVERED,    // 已送达
    READ,         // 已读
    FAILED        // 发送失败
}
