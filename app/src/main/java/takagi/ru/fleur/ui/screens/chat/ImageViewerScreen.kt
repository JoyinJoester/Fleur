package takagi.ru.fleur.ui.screens.chat

import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.fleur.ui.screens.chat.components.ImageViewer

/**
 * 图片查看器页面
 * 
 * 全屏显示消息中的图片附件
 * 支持滑动切换、缩放、分享和下载
 * 
 * @param messageId 消息ID
 * @param initialImageIndex 初始显示的图片索引
 * @param onNavigateBack 返回回调
 * @param viewModel ViewModel
 * @param modifier 修饰符
 */
@Composable
fun ImageViewerScreen(
    messageId: String,
    initialImageIndex: Int,
    onNavigateBack: () -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // 查找对应的消息
    val message = remember(uiState.messages, messageId) {
        uiState.messages.find { it.id == messageId }
    }
    
    // 获取图片附件列表
    val imageAttachments = remember(message) {
        message?.attachments?.filter { it.isImage } ?: emptyList()
    }
    
    if (imageAttachments.isNotEmpty()) {
        ImageViewer(
            images = imageAttachments,
            initialIndex = initialImageIndex.coerceIn(0, imageAttachments.size - 1),
            onClose = onNavigateBack,
            onShare = { attachment ->
                // 分享图片
                shareImage(context, attachment.localPath ?: attachment.downloadUrl ?: "")
            },
            onDownload = { attachment ->
                // 下载图片
                // TODO: 实现图片下载功能
            },
            modifier = modifier
        )
    } else {
        // 如果没有图片，直接返回
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
    }
}

/**
 * 分享图片
 * 
 * @param context 上下文
 * @param imagePath 图片路径
 */
private fun shareImage(context: android.content.Context, imagePath: String) {
    try {
        val uri = android.net.Uri.parse(imagePath)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "分享图片"))
    } catch (e: Exception) {
        // 分享失败
        e.printStackTrace()
    }
}
