package takagi.ru.fleur.ui.screens.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.ui.model.AttachmentUiModel

/**
 * 文件预览行组件
 * 
 * 在 MessageInputBar 上方显示选中的文件预览
 * 支持移除文件功能
 * 
 * @param attachments 附件列表
 * @param onRemove 移除附件回调
 * @param modifier 修饰符
 */
@Composable
fun FilePreviewRow(
    attachments: List<AttachmentUiModel>,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 只显示非图片附件
    val fileAttachments = attachments.filter { !it.isImage }
    
    AnimatedVisibility(
        visible = fileAttachments.isNotEmpty(),
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = fileAttachments,
                    key = { it.id }
                ) { attachment ->
                    FilePreviewItem(
                        attachment = attachment,
                        onRemove = { onRemove(attachment.id) }
                    )
                }
            }
        }
    }
}

/**
 * 文件预览项
 * 
 * @param attachment 附件
 * @param onRemove 移除回调
 */
@Composable
private fun FilePreviewItem(
    attachment: AttachmentUiModel,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文件类型图标
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = getFileIcon(attachment.mimeType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // 文件信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = attachment.fileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = attachment.fileSize,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 移除按钮
            Surface(
                modifier = Modifier.size(20.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                onClick = onRemove
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "移除",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * 根据 MIME 类型获取文件图标
 * 
 * @param mimeType MIME 类型
 * @return 对应的图标
 */
private fun getFileIcon(mimeType: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        mimeType.startsWith("application/pdf") -> Icons.Default.PictureAsPdf
        mimeType.startsWith("application/msword") ||
        mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml") -> 
            Icons.Default.Description
        mimeType.startsWith("application/vnd.ms-excel") ||
        mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml") -> 
            Icons.Default.Description
        mimeType.startsWith("text/") -> Icons.Default.Description
        else -> Icons.Default.InsertDriveFile
    }
}
