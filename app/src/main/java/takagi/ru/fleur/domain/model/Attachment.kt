package takagi.ru.fleur.domain.model

/**
 * 附件模型
 * @property id 附件唯一标识
 * @property emailId 所属邮件ID
 * @property fileName 文件名
 * @property mimeType MIME类型
 * @property size 文件大小（字节）
 * @property url 下载URL（可选）
 * @property localPath 本地缓存路径（可选）
 */
data class Attachment(
    val id: String,
    val emailId: String,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val url: String? = null,
    val localPath: String? = null
) {
    /**
     * 格式化文件大小显示
     */
    fun formattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
        }
    }
    
    /**
     * 是否为图片
     */
    fun isImage(): Boolean = mimeType.startsWith("image/")
    
    /**
     * 是否已下载到本地
     */
    fun isDownloaded(): Boolean = localPath != null
}
