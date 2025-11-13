package takagi.ru.fleur.ui.screens.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 附件选择底部弹窗
 * 
 * 提供拍照、图片、文件选项
 * 应用 Material 3 样式
 * 
 * @param onDismiss 关闭回调
 * @param onCameraCapture 拍照回调
 * @param onImageSelect 选择图片回调
 * @param onFileSelect 选择文件回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onCameraCapture: () -> Unit,
    onImageSelect: () -> Unit,
    onFileSelect: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // 标题
            Text(
                text = "添加附件",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            
            // 拍照选项
            AttachmentOption(
                icon = Icons.Default.CameraAlt,
                title = "拍照",
                description = "使用相机拍摄照片",
                onClick = {
                    onCameraCapture()
                    onDismiss()
                }
            )
            
            // 图片选项
            AttachmentOption(
                icon = Icons.Default.Image,
                title = "图片",
                description = "从相册选择图片",
                onClick = {
                    onImageSelect()
                    onDismiss()
                }
            )
            
            // 文件选项
            AttachmentOption(
                icon = Icons.Default.InsertDriveFile,
                title = "文件",
                description = "选择文档或其他文件",
                onClick = {
                    onFileSelect()
                    onDismiss()
                }
            )
        }
    }
}

/**
 * 附件选项项
 * 
 * @param icon 图标
 * @param title 标题
 * @param description 描述
 * @param onClick 点击回调
 */
@Composable
private fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // 文本
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
