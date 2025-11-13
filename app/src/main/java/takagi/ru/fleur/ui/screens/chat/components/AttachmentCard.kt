package takagi.ru.fleur.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import takagi.ru.fleur.ui.model.AttachmentUiModel

/**
 * 附件卡片组件
 * 
 * 用于在消息气泡中显示图片附件
 * 支持：
 * - 使用 Coil 加载图片
 * - 显示缩略图
 * - 点击打开全屏查看
 * - 显示加载状态和错误
 * 
 * @param attachment 附件
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun AttachmentCard(
    attachment: AttachmentUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachment.isImage) {
        ImageAttachmentCard(
            attachment = attachment,
            onClick = onClick,
            modifier = modifier
        )
    } else {
        FileAttachmentCard(
            attachment = attachment,
            onClick = onClick,
            modifier = modifier
        )
    }
}

/**
 * 图片附件卡片
 * 
 * @param attachment 附件
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
private fun ImageAttachmentCard(
    attachment: AttachmentUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .widthIn(max = 300.dp)
            .heightIn(max = 400.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        SubcomposeAsyncImage(
            model = attachment.localPath ?: attachment.thumbnailUrl ?: attachment.downloadUrl,
            contentDescription = attachment.fileName,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        ) {
            val state = painter.state
            when (state) {
                is AsyncImagePainter.State.Loading -> {
                    // 加载状态
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                            
                            Text(
                                text = "加载中...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                is AsyncImagePainter.State.Error -> {
                    // 错误状态
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = "加载失败",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Text(
                                text = "图片加载失败",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            
                            Text(
                                text = "点击重试",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                is AsyncImagePainter.State.Success -> {
                    // 成功加载
                    SubcomposeAsyncImageContent()
                }
                
                else -> {
                    // 空状态
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 文件附件卡片
 * 
 * @param attachment 附件
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
private fun FileAttachmentCard(
    attachment: AttachmentUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .widthIn(max = 300.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文件图标
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = getFileTypeIcon(attachment.mimeType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = attachment.fileSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // 显示文件类型
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = getFileTypeLabel(attachment.mimeType),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 下载进度或图标
            if (attachment.downloadProgress != null && attachment.downloadProgress > 0f) {
                CircularProgressIndicator(
                    progress = { attachment.downloadProgress },
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else if (!attachment.isDownloaded) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "下载",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "打开",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
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
private fun getFileTypeIcon(mimeType: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        mimeType.startsWith("application/pdf") -> Icons.Default.PictureAsPdf
        mimeType.startsWith("application/msword") ||
        mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml") -> 
            Icons.Default.Description
        mimeType.startsWith("application/vnd.ms-excel") ||
        mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml") -> 
            Icons.Default.TableChart
        mimeType.startsWith("text/") -> Icons.Default.Description
        mimeType.startsWith("application/zip") ||
        mimeType.startsWith("application/x-rar") -> Icons.Default.FolderZip
        else -> Icons.Default.InsertDriveFile
    }
}

/**
 * 根据 MIME 类型获取文件类型标签
 * 
 * @param mimeType MIME 类型
 * @return 文件类型标签
 */
private fun getFileTypeLabel(mimeType: String): String {
    return when {
        mimeType.startsWith("application/pdf") -> "PDF"
        mimeType.startsWith("application/msword") -> "DOC"
        mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml") -> "DOCX"
        mimeType.startsWith("application/vnd.ms-excel") -> "XLS"
        mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml") -> "XLSX"
        mimeType.startsWith("text/plain") -> "TXT"
        mimeType.startsWith("application/zip") -> "ZIP"
        mimeType.startsWith("application/x-rar") -> "RAR"
        else -> "文件"
    }
}

/**
 * 多图片网格布局
 * 
 * 用于显示多张图片附件
 * 
 * @param attachments 图片附件列表
 * @param onImageClick 图片点击回调
 * @param modifier 修饰符
 */
@Composable
fun ImageAttachmentGrid(
    attachments: List<AttachmentUiModel>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageAttachments = attachments.filter { it.isImage }
    
    when (imageAttachments.size) {
        0 -> {
            // 无图片
        }
        
        1 -> {
            // 单张图片
            AttachmentCard(
                attachment = imageAttachments[0],
                onClick = { onImageClick(0) },
                modifier = modifier
            )
        }
        
        2 -> {
            // 两张图片，横向排列
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                imageAttachments.forEachIndexed { index, attachment ->
                    AttachmentCard(
                        attachment = attachment,
                        onClick = { onImageClick(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        3 -> {
            // 三张图片，1+2 布局
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AttachmentCard(
                    attachment = imageAttachments[0],
                    onClick = { onImageClick(0) }
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AttachmentCard(
                        attachment = imageAttachments[1],
                        onClick = { onImageClick(1) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    AttachmentCard(
                        attachment = imageAttachments[2],
                        onClick = { onImageClick(2) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        4 -> {
            // 四张图片，2x2 网格
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AttachmentCard(
                        attachment = imageAttachments[0],
                        onClick = { onImageClick(0) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    AttachmentCard(
                        attachment = imageAttachments[1],
                        onClick = { onImageClick(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AttachmentCard(
                        attachment = imageAttachments[2],
                        onClick = { onImageClick(2) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    AttachmentCard(
                        attachment = imageAttachments[3],
                        onClick = { onImageClick(3) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        else -> {
            // 更多图片，显示前 4 张 + 剩余数量
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AttachmentCard(
                        attachment = imageAttachments[0],
                        onClick = { onImageClick(0) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    AttachmentCard(
                        attachment = imageAttachments[1],
                        onClick = { onImageClick(1) },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AttachmentCard(
                        attachment = imageAttachments[2],
                        onClick = { onImageClick(2) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        AttachmentCard(
                            attachment = imageAttachments[3],
                            onClick = { onImageClick(3) }
                        )
                        
                        // 显示剩余数量
                        if (imageAttachments.size > 4) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onImageClick(3) },
                                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = "+${imageAttachments.size - 4}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
