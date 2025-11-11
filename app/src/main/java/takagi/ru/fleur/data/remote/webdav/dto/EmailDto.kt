package takagi.ru.fleur.data.remote.webdav.dto

import kotlinx.datetime.Instant

/**
 * 邮件数据传输对象
 * 用于 WebDAV 客户端与服务器之间的数据传输
 */
data class EmailDto(
    val id: String,
    val threadId: String,
    val from: EmailAddressDto,
    val to: List<EmailAddressDto>,
    val cc: List<EmailAddressDto> = emptyList(),
    val bcc: List<EmailAddressDto> = emptyList(),
    val subject: String,
    val bodyHtml: String? = null,
    val bodyPlain: String,
    val attachments: List<AttachmentDto> = emptyList(),
    val timestamp: Instant,
    val flags: EmailFlags = EmailFlags()
)

/**
 * 邮件地址数据传输对象
 */
data class EmailAddressDto(
    val address: String,
    val name: String? = null
)

/**
 * 附件数据传输对象
 */
data class AttachmentDto(
    val id: String,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val contentId: String? = null,
    val data: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttachmentDto

        if (id != other.id) return false
        if (fileName != other.fileName) return false
        if (mimeType != other.mimeType) return false
        if (size != other.size) return false
        if (contentId != other.contentId) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + (contentId?.hashCode() ?: 0)
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * 邮件标记
 */
data class EmailFlags(
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val isAnswered: Boolean = false,
    val isDeleted: Boolean = false,
    val isDraft: Boolean = false
)
