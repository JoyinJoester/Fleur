package takagi.ru.fleur.data.mapper

import takagi.ru.fleur.domain.model.Attachment
import takagi.ru.fleur.ui.model.AttachmentUiModel

/**
 * 附件映射器
 * 将附件转换为附件 UI 模型
 */
object AttachmentMapper {
    
    /**
     * 从附件创建附件 UI 模型
     * 
     * @param attachment 附件对象
     * @return 附件 UI 模型
     */
    fun fromAttachment(attachment: Attachment): AttachmentUiModel {
        return AttachmentUiModel(
            id = attachment.id,
            fileName = attachment.fileName,
            fileSize = attachment.formattedSize(),
            mimeType = attachment.mimeType,
            thumbnailUrl = if (attachment.isImage()) attachment.url else null,
            downloadUrl = attachment.url,
            localPath = attachment.localPath,
            downloadProgress = null, // 初始状态无下载进度
            isImage = attachment.isImage(),
            isDownloaded = attachment.isDownloaded()
        )
    }
    
    /**
     * 批量转换附件列表
     * 
     * @param attachments 附件列表
     * @return 附件 UI 模型列表
     */
    fun fromAttachmentList(attachments: List<Attachment>): List<AttachmentUiModel> {
        return attachments.map { fromAttachment(it) }
    }
}
