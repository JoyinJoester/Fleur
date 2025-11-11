package takagi.ru.fleur.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.ui.theme.bottomSheetGlassmorphism

/**
 * 附件操作 Bottom Sheet
 * 
 * 提供附件的操作选项：
 * - 预览/打开
 * - 下载
 * - 分享
 * - 保存到相册（仅图片）
 * 
 * 特性:
 * - 毛玻璃效果：12dp blur + 90% opacity
 * - 滑入动画：300ms
 * - 支持手势拖拽关闭
 * 
 * @param attachmentName 附件名称
 * @param attachmentSize 附件大小（格式化后的字符串，如 "2.5 MB"）
 * @param isImage 是否为图片类型
 * @param onDismiss 关闭回调
 * @param onOpen 打开/预览回调
 * @param onDownload 下载回调
 * @param onShare 分享回调
 * @param onSaveToGallery 保存到相册回调（仅图片）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentOptionsBottomSheet(
    attachmentName: String,
    attachmentSize: String,
    isImage: Boolean = false,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onSaveToGallery: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.bottomSheetGlassmorphism(),
        containerColor = Color.Transparent,
        dragHandle = {
            // 自定义拖拽手柄
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ) {}
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // 附件信息
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = attachmentName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = attachmentSize,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 操作项
            AttachmentActionItem(
                icon = if (isImage) Icons.Default.Visibility else Icons.Default.OpenInNew,
                label = if (isImage) "预览" else "打开",
                onClick = {
                    onOpen()
                    onDismiss()
                }
            )
            
            AttachmentActionItem(
                icon = Icons.Default.Download,
                label = "下载",
                onClick = {
                    onDownload()
                    onDismiss()
                }
            )
            
            AttachmentActionItem(
                icon = Icons.Default.Share,
                label = "分享",
                onClick = {
                    onShare()
                    onDismiss()
                }
            )
            
            // 仅图片显示保存到相册选项
            if (isImage && onSaveToGallery != null) {
                AttachmentActionItem(
                    icon = Icons.Default.Image,
                    label = "保存到相册",
                    onClick = {
                        onSaveToGallery()
                        onDismiss()
                    }
                )
            }
        }
    }
}

/**
 * 附件操作项
 * 
 * @param icon 图标
 * @param label 标签
 * @param onClick 点击回调
 */
@Composable
private fun AttachmentActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
