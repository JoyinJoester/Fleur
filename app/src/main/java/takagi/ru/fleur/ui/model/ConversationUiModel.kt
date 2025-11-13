package takagi.ru.fleur.ui.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

/**
 * 对话 UI 模型
 * 用于在 Chat 页面展示对话列表
 *
 * @property id 对话ID (threadId)
 * @property contactName 联系人名称
 * @property contactEmail 联系人邮箱
 * @property contactAvatar 头像URL
 * @property lastMessage 最后一条消息预览
 * @property lastMessageTime 最后消息时间
 * @property unreadCount 未读消息数
 * @property hasAttachment 是否包含附件
 * @property isPinned 是否置顶
 */
@Immutable
data class ConversationUiModel(
    val id: String,
    val contactName: String,
    val contactEmail: String,
    val contactAvatar: String?,
    val lastMessage: String,
    val lastMessageTime: Instant,
    val unreadCount: Int,
    val hasAttachment: Boolean,
    val isPinned: Boolean = false
)
