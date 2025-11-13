package takagi.ru.fleur.ui.screens.chat.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import takagi.ru.fleur.ui.model.AttachmentUiModel

/**
 * 图片全屏查看器组件
 * 
 * 支持：
 * - 图片滑动切换
 * - 缩放和平移手势
 * - 顶部工具栏（关闭、分享、下载）
 * - 底部图片索引指示器
 * 
 * @param images 图片列表
 * @param initialIndex 初始显示的图片索引
 * @param onClose 关闭回调
 * @param onShare 分享回调
 * @param onDownload 下载回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImageViewer(
    images: List<AttachmentUiModel>,
    initialIndex: Int = 0,
    onClose: () -> Unit,
    onShare: (AttachmentUiModel) -> Unit = {},
    onDownload: (AttachmentUiModel) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 分页状态
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { images.size }
    )
    
    // 工具栏可见性
    var toolbarVisible by remember { mutableStateOf(true) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 图片分页器
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val image = images[page]
            
            ZoomableImage(
                imageUrl = image.localPath ?: image.downloadUrl ?: "",
                onTap = {
                    toolbarVisible = !toolbarVisible
                }
            )
        }
        
        // 顶部工具栏
        androidx.compose.animation.AnimatedVisibility(
            visible = toolbarVisible,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${images.size}",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(48.dp) // 符合最小触摸目标 48dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // 分享按钮
                    IconButton(
                        onClick = {
                            onShare(images[pagerState.currentPage])
                        },
                        modifier = Modifier.size(48.dp) // 符合最小触摸目标 48dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "分享",
                            tint = Color.White
                        )
                    }
                    
                    // 下载按钮
                    IconButton(
                        onClick = {
                            onDownload(images[pagerState.currentPage])
                        },
                        modifier = Modifier.size(48.dp) // 符合最小触摸目标 48dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "下载",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            )
        }
        
        // 底部索引指示器
        androidx.compose.animation.AnimatedVisibility(
            visible = toolbarVisible && images.size > 1,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it },
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            ImageIndexIndicator(
                currentIndex = pagerState.currentPage,
                totalCount = images.size
            )
        }
    }
}

/**
 * 可缩放图片组件
 * 
 * 支持双指缩放和平移手势
 * 
 * @param imageUrl 图片URL
 * @param onTap 点击回调
 * @param modifier 修饰符
 */
@Composable
private fun ZoomableImage(
    imageUrl: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 缩放和平移状态
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    // 变换状态
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        // 更新缩放
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        
        // 更新偏移
        if (scale > 1f) {
            offset += offsetChange
        } else {
            offset = Offset.Zero
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        // 双击缩放
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2f
                        }
                    }
                )
            }
            .transformable(state = transformableState),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * 图片索引指示器
 * 
 * 显示当前图片的索引和总数
 * 
 * @param currentIndex 当前索引
 * @param totalCount 总数
 * @param modifier 修饰符
 */
@Composable
private fun ImageIndexIndicator(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalCount) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentIndex) 8.dp else 6.dp)
                        .background(
                            color = if (index == currentIndex) {
                                Color.White
                            } else {
                                Color.White.copy(alpha = 0.5f)
                            },
                            shape = MaterialTheme.shapes.extraSmall
                        )
                )
            }
        }
    }
}
