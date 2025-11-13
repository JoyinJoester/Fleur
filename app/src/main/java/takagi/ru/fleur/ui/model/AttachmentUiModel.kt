package takagi.ru.fleur.ui.model

import androidx.compose.runtime.Immutable

/**
 * 附件 UI 模型
 * 用于在消息中展示附件信息
 *
 * @property id 附件ID
 * @property fileName 文件名
 * @property fileSize 格式化后的文件大小
 * @property mimeType MIME类型
 * @property thumbnailUrl 缩略图URL (图片)
 * @property downloadUrl 下载URL
 * @property localPath 本地路径
 * @property downloadProgress 下载进度 (0-1)
 * @property isImage 是否为图片
 * @property isDownloaded 是否已下载
 */
@Immutable
data class AttachmentUiModel(
    val id: String,
    val fileName: String,
    val fileSize: String,
    val mimeType: String,
    val thumbnailUrl: String?,
    val downloadUrl: String?,
    val localPath: String?,
    val downloadProgress: Float?,
    val isImage: Boolean,
    val isDownloaded: Boolean
)
