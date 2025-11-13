package takagi.ru.fleur.domain.model

import kotlinx.datetime.Instant

/**
 * 联系人领域模型
 * 用户手动添加和管理的联系人
 * 
 * @property id 联系人唯一标识
 * @property name 联系人姓名
 * @property email 邮箱地址（主要）
 * @property phoneNumber 电话号码（可选）
 * @property organization 组织/公司（可选）
 * @property jobTitle 职位（可选）
 * @property address 地址（可选）
 * @property notes 备注（可选）
 * @property avatarUrl 头像URL（可选）
 * @property isFavorite 是否收藏
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
data class Contact(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String? = null,
    val organization: String? = null,
    val jobTitle: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val avatarUrl: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)
