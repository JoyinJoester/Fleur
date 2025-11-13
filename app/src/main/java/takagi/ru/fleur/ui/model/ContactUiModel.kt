package takagi.ru.fleur.ui.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

/**
 * 联系人 UI 模型
 * 用于在 UI 层展示联系人信息
 * 
 * @property id 联系人唯一标识（使用邮箱地址作为ID）
 * @property name 联系人姓名
 * @property email 邮箱地址
 * @property avatarUrl 头像URL（可选）
 * @property phoneNumber 电话号码（可选）
 * @property address 地址（可选）
 * @property notes 备注（可选）
 * @property isOnline 是否在线
 * @property lastContactTime 最后联系时间
 * @property isFavorite 是否收藏
 * @property conversationId 关联的对话ID（可选）
 */
@Immutable
data class ContactUiModel(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val isOnline: Boolean = false,
    val lastContactTime: Instant? = null,
    val isFavorite: Boolean = false,
    val conversationId: String? = null
)
