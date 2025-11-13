package takagi.ru.fleur.data.local.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.datetime.Instant
import takagi.ru.fleur.data.local.entity.AccountEntity
import takagi.ru.fleur.data.local.entity.AttachmentEntity
import takagi.ru.fleur.data.local.entity.ContactEntity
import takagi.ru.fleur.data.local.entity.EmailEntity
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.Attachment
import takagi.ru.fleur.domain.model.Contact
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.EmailAddress
import takagi.ru.fleur.domain.model.WebDAVConfig

/**
 * Entity 和 Domain Model 之间的映射器
 */
object EntityMapper {
    
    private val gson = Gson()
    
    // ========== Email 映射 ==========
    
    /**
     * EmailEntity -> Email
     */
    fun EmailEntity.toDomain(attachments: List<Attachment> = emptyList()): Email {
        return Email(
            id = id,
            threadId = threadId,
            accountId = accountId,
            from = EmailAddress(
                address = fromAddress,
                name = fromName
            ),
            to = parseEmailAddresses(toAddresses),
            cc = parseEmailAddresses(ccAddresses),
            bcc = parseEmailAddresses(bccAddresses),
            subject = subject,
            bodyPreview = bodyPreview,
            bodyPlain = bodyPlain,
            bodyHtml = bodyHtml,
            bodyMarkdown = bodyMarkdown,
            contentType = contentType,
            attachments = attachments,
            timestamp = Instant.fromEpochMilliseconds(timestamp),
            isRead = isRead,
            isStarred = isStarred,
            labels = parseLabels(labels)
        )
    }
    
    /**
     * Email -> EmailEntity
     */
    fun Email.toEntity(): EmailEntity {
        return EmailEntity(
            id = id,
            threadId = threadId,
            accountId = accountId,
            fromAddress = from.address,
            fromName = from.name,
            toAddresses = serializeEmailAddresses(to),
            ccAddresses = serializeEmailAddresses(cc),
            bccAddresses = serializeEmailAddresses(bcc),
            subject = subject,
            bodyPreview = bodyPreview,
            bodyPlain = bodyPlain,
            bodyHtml = bodyHtml,
            bodyMarkdown = bodyMarkdown,
            contentType = contentType,
            timestamp = timestamp.toEpochMilliseconds(),
            isRead = isRead,
            isStarred = isStarred,
            labels = serializeLabels(labels)
        )
    }
    
    // ========== Account 映射 ==========
    
    /**
     * AccountEntity -> Account
     */
    fun AccountEntity.toDomain(): Account {
        return Account(
            id = id,
            email = email,
            displayName = displayName,
            color = Color(color),
            isDefault = isDefault,
            webdavConfig = WebDAVConfig(
                serverUrl = serverUrl,
                port = port,
                username = username,
                useSsl = useSsl,
                calendarPath = calendarPath,
                contactsPath = contactsPath
            )
        )
    }
    
    /**
     * Account -> AccountEntity
     */
    fun Account.toEntity(): AccountEntity {
        return AccountEntity(
            id = id,
            email = email,
            displayName = displayName,
            color = color.toArgb(),
            isDefault = isDefault,
            serverUrl = webdavConfig.serverUrl,
            port = webdavConfig.port,
            username = webdavConfig.username,
            useSsl = webdavConfig.useSsl,
            calendarPath = webdavConfig.calendarPath,
            contactsPath = webdavConfig.contactsPath
        )
    }
    
    // ========== Attachment 映射 ==========
    
    /**
     * AttachmentEntity -> Attachment
     */
    fun AttachmentEntity.toDomain(): Attachment {
        return Attachment(
            id = id,
            emailId = emailId,
            fileName = fileName,
            mimeType = mimeType,
            size = size,
            url = url,
            localPath = localPath
        )
    }
    
    /**
     * Attachment -> AttachmentEntity
     */
    fun Attachment.toEntity(): AttachmentEntity {
        return AttachmentEntity(
            id = id,
            emailId = emailId,
            fileName = fileName,
            mimeType = mimeType,
            size = size,
            url = url,
            localPath = localPath
        )
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 解析邮件地址列表（JSON）
     */
    private fun parseEmailAddresses(json: String?): List<EmailAddress> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val type = object : TypeToken<List<Map<String, String>>>() {}.type
            val list: List<Map<String, String>> = gson.fromJson(json, type)
            list.map { map ->
                EmailAddress(
                    address = map["address"] ?: "",
                    name = map["name"]
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 序列化邮件地址列表为JSON
     */
    private fun serializeEmailAddresses(addresses: List<EmailAddress>): String {
        if (addresses.isEmpty()) return "[]"
        val list = addresses.map { mapOf("address" to it.address, "name" to it.name) }
        return gson.toJson(list)
    }
    
    /**
     * 解析标签列表（JSON）
     */
    private fun parseLabels(json: String?): List<String> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 序列化标签列表为JSON
     */
    private fun serializeLabels(labels: List<String>): String? {
        if (labels.isEmpty()) return null
        return gson.toJson(labels)
    }
    
    // ========== Contact 映射 ==========
    
    /**
     * ContactEntity -> Contact
     */
    fun toContact(entity: ContactEntity): Contact {
        return Contact(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            phoneNumber = entity.phoneNumber,
            organization = entity.organization,
            jobTitle = entity.jobTitle,
            address = entity.address,
            notes = entity.notes,
            avatarUrl = entity.avatarUrl,
            isFavorite = entity.isFavorite,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
            updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt)
        )
    }
    
    /**
     * Contact -> ContactEntity
     */
    fun toContactEntity(contact: Contact): ContactEntity {
        return ContactEntity(
            id = contact.id,
            name = contact.name,
            email = contact.email,
            phoneNumber = contact.phoneNumber,
            organization = contact.organization,
            jobTitle = contact.jobTitle,
            address = contact.address,
            notes = contact.notes,
            avatarUrl = contact.avatarUrl,
            isFavorite = contact.isFavorite,
            createdAt = contact.createdAt.toEpochMilliseconds(),
            updatedAt = contact.updatedAt.toEpochMilliseconds()
        )
    }
}
