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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import takagi.ru.fleur.ui.model.AttachmentUiModel

/**
 * 图片预览行组件
 * 
 * 在 MessageInputBar 上方显示选中的图片缩略图
 * 支持移除图片功能
 * 
 * @param attachments 附件列表
 * @param onRemove 移除附件回调
 * @param modifier 修饰符
 */
@Composable
fun ImagePreviewRow(
    attachments: List<AttachmentUiModel>,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 只显示图片附件
    val imageAttachments = attachments.filter { it.isImage }
    
    AnimatedVisibility(
        visible = imageAttachments.isNotEmpty(),
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
                    items = imageAttachments,
                    key = { it.id }
                ) { attachment ->
                    ImagePreviewItem(
                        attachment = attachment,
                        onRemove = { onRemove(attachment.id) }
                    )
                }
            }
        }
    }
}

/**
 * 图片预览项
 * 
 * @param attachment 附件
 * @param onRemove 移除回调
 */
@Composable
private fun ImagePreviewItem(
    attachment: AttachmentUiModel,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier.size(80.dp)
    ) {
        // 图片缩略图
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp
        ) {
            AsyncImage(
                model = attachment.localPath ?: attachment.thumbnailUrl,
                contentDescription = attachment.fileName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // 移除按钮
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(24.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            tonalElevation = 4.dp,
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
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
