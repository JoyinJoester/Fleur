package takagi.ru.fleur.data.remote.webdav.mapper

import takagi.ru.fleur.data.remote.webdav.dto.AttachmentDto
import takagi.ru.fleur.data.remote.webdav.dto.EmailAddressDto
import takagi.ru.fleur.data.remote.webdav.dto.EmailDto
import takagi.ru.fleur.data.remote.webdav.dto.EmailFlags
import takagi.ru.fleur.domain.model.Attachment
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.EmailAddress

/**
 * DTO 和 Domain Model 之间的映射器
 */
object DtoMapper {
    
    /**
     * EmailDto -> Email
     */
    fun EmailDto.toDomain(accountId: String): Email {
        return Email(
            id = id,
            threadId = threadId,
            accountId = accountId,
            from = from.toDomain(),
            to = to.map { it.toDomain() },
            cc = cc.map { it.toDomain() },
            bcc = bcc.map { it.toDomain() },
            subject = subject,
            bodyPreview = bodyPlain.take(200),
            bodyHtml = bodyHtml,
            bodyPlain = bodyPlain,
            attachments = attachments.map { it.toDomain(id) },
            timestamp = timestamp,
            isRead = flags.isRead,
            isStarred = flags.isStarred,
            labels = emptyList()
        )
    }
    
    /**
     * Email -> EmailDto
     */
    fun Email.toDto(): EmailDto {
        return EmailDto(
            id = id,
            threadId = threadId,
            from = from.toDto(),
            to = to.map { it.toDto() },
            cc = cc.map { it.toDto() },
            bcc = bcc.map { it.toDto() },
            subject = subject,
            bodyHtml = bodyHtml,
            bodyPlain = bodyPlain,
            attachments = attachments.map { it.toDto() },
            timestamp = timestamp,
            flags = EmailFlags(
                isRead = isRead,
                isStarred = isStarred
            )
        )
    }
    
    /**
     * EmailAddressDto -> EmailAddress
     */
    fun EmailAddressDto.toDomain(): EmailAddress {
        return EmailAddress(
            address = address,
            name = name
        )
    }
    
    /**
     * EmailAddress -> EmailAddressDto
     */
    fun EmailAddress.toDto(): EmailAddressDto {
        return EmailAddressDto(
            address = address,
            name = name
        )
    }
    
    /**
     * AttachmentDto -> Attachment
     */
    fun AttachmentDto.toDomain(emailId: String): Attachment {
        return Attachment(
            id = id,
            emailId = emailId,
            fileName = fileName,
            mimeType = mimeType,
            size = size,
            url = null,
            localPath = null
        )
    }
    
    /**
     * Attachment -> AttachmentDto
     */
    fun Attachment.toDto(): AttachmentDto {
        return AttachmentDto(
            id = id,
            fileName = fileName,
            mimeType = mimeType,
            size = size
        )
    }
}
