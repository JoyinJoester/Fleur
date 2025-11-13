package takagi.ru.fleur.data.mapper

import kotlinx.datetime.Instant
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.EmailAddress
import takagi.ru.fleur.ui.model.ContactUiModel

/**
 * 联系人映射器
 * 负责将 Email 和 EmailAddress 转换为 ContactUiModel
 */
object ContactMapper {
    
    /**
     * 从邮件地址创建联系人
     * @param emailAddress 邮件地址
     * @param lastContactTime 最后联系时间
     * @param conversationId 关联的对话ID
     * @return ContactUiModel 联系人UI模型
     */
    fun fromEmailAddress(
        emailAddress: EmailAddress,
        lastContactTime: Instant? = null,
        conversationId: String? = null
    ): ContactUiModel {
        return ContactUiModel(
            id = emailAddress.address,
            name = emailAddress.name ?: emailAddress.address,
            email = emailAddress.address,
            avatarUrl = null,
            phoneNumber = null,
            address = null,
            notes = null,
            isOnline = false,
            lastContactTime = lastContactTime,
            isFavorite = false,
            conversationId = conversationId
        )
    }
    
    /**
     * 从邮件创建联系人（使用发件人信息）
     * @param email 邮件
     * @return ContactUiModel 联系人UI模型
     */
    fun fromEmail(email: Email): ContactUiModel {
        return fromEmailAddress(
            emailAddress = email.from,
            lastContactTime = email.timestamp,
            conversationId = email.threadId
        )
    }
    
    /**
     * 合并多个联系人（去重并合并信息）
     * 按邮箱地址分组，保留最新的联系时间和最完整的信息
     * @param contacts 联系人列表
     * @return List<ContactUiModel> 合并后的联系人列表
     */
    fun mergeContacts(contacts: List<ContactUiModel>): List<ContactUiModel> {
        return contacts
            .groupBy { it.email }
            .map { (_, contactGroup) ->
                contactGroup.reduce { acc, contact ->
                    acc.copy(
                        // 优先使用非空且非邮箱地址的姓名
                        name = if (contact.name.isNotBlank() && contact.name != contact.email) {
                            contact.name
                        } else {
                            acc.name
                        },
                        // 保留最新的联系时间
                        lastContactTime = maxOfOrNull(
                            acc.lastContactTime ?: Instant.DISTANT_PAST,
                            contact.lastContactTime ?: Instant.DISTANT_PAST
                        ),
                        // 保留最新的对话ID
                        conversationId = contact.conversationId ?: acc.conversationId,
                        // 合并其他可选字段（优先使用非空值）
                        avatarUrl = contact.avatarUrl ?: acc.avatarUrl,
                        phoneNumber = contact.phoneNumber ?: acc.phoneNumber,
                        address = contact.address ?: acc.address,
                        notes = contact.notes ?: acc.notes,
                        // 保留收藏状态（任一为true则为true）
                        isFavorite = acc.isFavorite || contact.isFavorite
                    )
                }
            }
            .sortedBy { it.name.lowercase() }
    }
    
    /**
     * 从邮件列表提取所有联系人（包括发件人和收件人）
     * @param emails 邮件列表
     * @return List<ContactUiModel> 联系人列表
     */
    fun extractContactsFromEmails(emails: List<Email>): List<ContactUiModel> {
        val contacts = mutableListOf<ContactUiModel>()
        
        emails.forEach { email ->
            // 添加发件人
            contacts.add(fromEmail(email))
            
            // 添加所有收件人
            email.to.forEach { recipient ->
                contacts.add(
                    fromEmailAddress(
                        emailAddress = recipient,
                        lastContactTime = email.timestamp,
                        conversationId = email.threadId
                    )
                )
            }
            
            // 添加抄送人
            email.cc.forEach { recipient ->
                contacts.add(
                    fromEmailAddress(
                        emailAddress = recipient,
                        lastContactTime = email.timestamp,
                        conversationId = email.threadId
                    )
                )
            }
        }
        
        // 合并去重
        return mergeContacts(contacts)
    }
    
    /**
     * 辅助函数：获取两个可空Instant的最大值
     */
    private fun maxOfOrNull(a: Instant, b: Instant): Instant {
        return if (a > b) a else b
    }
}
