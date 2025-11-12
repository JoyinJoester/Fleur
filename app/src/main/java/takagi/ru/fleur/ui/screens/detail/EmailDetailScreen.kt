package takagi.ru.fleur.ui.screens.detail

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.ui.components.EmailContentRenderer
import takagi.ru.fleur.ui.theme.FleurAnimation
import kotlin.math.abs

/**
 * 邮件详情页面 - M3E增强设计
 * 
 * 特性：
 * - 可折叠工具栏 (Collapsing Toolbar)
 * - 玻璃拟态设计 (Glassmorphism)
 * - 流畅动画效果 (Smooth Animations)
 * - 智能手势交互 (Smart Gestures)
 * - 响应式布局 (Responsive Layout)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCompose: (String, takagi.ru.fleur.domain.model.ComposeMode) -> Unit,
    viewModel: EmailDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // 设置导航回调
    LaunchedEffect(Unit) {
        viewModel.setNavigationCallback(onNavigateToCompose)
    }
    
    // FAB可见性控制
    val isFabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 || 
            !listState.isScrollInProgress
        }
    }
    
    // 显示错误消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error.getUserMessage())
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        text = uiState.email?.subject ?: "邮件详情",
                        maxLines = if (scrollBehavior.state.collapsedFraction > 0.5f) 1 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    // 星标按钮
                    IconButton(onClick = { viewModel.toggleStar() }) {
                        Icon(
                            imageVector = if (uiState.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "星标",
                            tint = if (uiState.isStarred) Color(0xFFFFA000) else LocalContentColor.current
                        )
                    }
                    
                    // 更多操作
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "更多")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("归档") },
                            leadingIcon = { Icon(Icons.Outlined.Archive, null) },
                            onClick = { 
                                viewModel.archive { onNavigateBack() }
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("标记为未读") },
                            leadingIcon = { Icon(Icons.Outlined.MarkEmailUnread, null) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("移动到") },
                            leadingIcon = { Icon(Icons.Outlined.DriveFileMove, null) },
                            onClick = { showMenu = false }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { 
                                viewModel.delete { onNavigateBack() }
                                showMenu = false
                            }
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                )
            )
        },
        floatingActionButton = {
            // 回复 FAB 带淡入淡出动画
            AnimatedVisibility(
                visible = isFabVisible && uiState.email != null,
                enter = scaleIn(animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.reply() },
                    icon = { Icon(Icons.Default.Reply, "回复") },
                    text = { Text("回复") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        bottomBar = {
            // 底部操作栏
            if (uiState.email != null) {
                BottomActionBar(
                    onReplyAll = { viewModel.replyAll() },
                    onForward = { viewModel.forward() }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                // 骨架屏加载动画
                EmailDetailSkeleton(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.email != null -> {
                EmailDetailContent(
                    email = uiState.email!!,
                    listState = listState,
                    onReply = { viewModel.reply() },
                    onReplyAll = { viewModel.replyAll() },
                    onForward = { viewModel.forward() },
                    onArchive = { viewModel.archive { onNavigateBack() } },
                    onDelete = { viewModel.delete { onNavigateBack() } },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

/**
 * 邮件详情内容区域
 * 支持滑动手势、视差效果
 */
@Composable
private fun EmailDetailContent(
    email: Email,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onReply: () -> Unit,
    onReplyAll: () -> Unit,
    onForward: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var swipeOffset by remember { mutableStateOf(0f) }
    var showSwipeActions by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                swipeOffset > 200 -> {
                                    // 向右滑动 - 回复
                                    onReply()
                                }
                                swipeOffset < -200 -> {
                                    // 向左滑动 - 归档
                                    onArchive()
                                }
                            }
                            swipeOffset = 0f
                            showSwipeActions = false
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            swipeOffset += dragAmount
                            showSwipeActions = abs(swipeOffset) > 50
                        }
                    )
                }
        ) {
            item {
                // 发件人信息卡片（增强玻璃拟态）
                EnhancedSenderInfoCard(email)
            }
            
            item {
                // 邮件正文
                EmailBodyCard(email)
            }
            
            if (email.attachments.isNotEmpty()) {
                item {
                    // 附件列表
                    EnhancedAttachmentsList(email.attachments)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // 滑动操作提示
        AnimatedVisibility(
            visible = showSwipeActions,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            SwipeActionHint(swipeOffset)
        }
    }
}

/**
 * 增强的发件人信息卡片
 * 玻璃拟态 + 渐变背景
 */
@Composable
private fun EnhancedSenderInfoCard(email: Email) {
    var isExpanded by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isExpanded) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        isExpanded = !isExpanded
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Box {
            // 渐变背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isExpanded) 220.dp else 180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 主题
                Text(
                    text = email.subject,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Divider(
                    modifier = Modifier.alpha(0.3f),
                    thickness = 1.dp
                )
                
                // 发件人
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 头像占位符
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = email.from.name?.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = email.from.name ?: email.from.address,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = email.from.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 收件人（可展开）
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (email.to.isNotEmpty()) {
                            Text(
                                text = "收件人：${email.to.joinToString { it.formatted() }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (email.cc.isNotEmpty()) {
                            Text(
                                text = "抄送：${email.cc.joinToString { it.formatted() }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // 时间戳
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = email.timestamp.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 邮件正文卡片
 * 支持HTML和纯文本渲染
 */
@Composable
private fun EmailBodyCard(email: Email) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 使用统一的邮件内容渲染器,支持 HTML/Markdown/纯文本
            EmailContentRenderer(
                bodyText = email.bodyPlain,
                bodyMarkdown = email.bodyMarkdown,
                bodyHtml = email.bodyHtml,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 增强的附件列表
 * 带图标、大小和下载状态
 */
@Composable
private fun EnhancedAttachmentsList(attachments: List<takagi.ru.fleur.domain.model.Attachment>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 附件统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.AttachFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "附件",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${attachments.size}个",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 附件列表
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(attachments) { attachment ->
                    EnhancedAttachmentItem(attachment)
                }
            }
        }
    }
}

/**
 * 增强的附件项
 * 带类型图标和悬浮效果
 */
@Composable
private fun EnhancedAttachmentItem(attachment: takagi.ru.fleur.domain.model.Attachment) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "attachment_scale"
    )
    
    Card(
        modifier = Modifier
            .width(140.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = {
                        // TODO: 显示附件操作菜单
                    }
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 1.dp else 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 文件类型图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getAttachmentColor(attachment.mimeType)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAttachmentIcon(attachment.mimeType),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // 文件名
            Text(
                text = attachment.fileName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 文件大小
            Text(
                text = attachment.formattedSize(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 底部操作栏
 */
@Composable
private fun BottomActionBar(
    onReplyAll: () -> Unit,
    onForward: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onReplyAll,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.ReplyAll, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("全部回复")
            }
            
            OutlinedButton(
                onClick = onForward,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.Forward, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("转发")
            }
        }
    }
}

/**
 * 滑动操作提示
 */
@Composable
private fun SwipeActionHint(swipeOffset: Float) {
    val icon = if (swipeOffset > 0) Icons.Outlined.Reply else Icons.Outlined.Archive
    val text = if (swipeOffset > 0) "回复" else "归档"
    val backgroundColor = if (swipeOffset > 0) 
        MaterialTheme.colorScheme.primaryContainer 
    else 
        MaterialTheme.colorScheme.tertiaryContainer
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 骨架屏加载动画
 */
@Composable
private fun EmailDetailSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 发件人卡片骨架
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
        )
        
        // 正文卡片骨架
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
        )
    }
}

/**
 * 根据MIME类型获取附件图标
 */
private fun getAttachmentIcon(mimeType: String): ImageVector {
    return when {
        mimeType.startsWith("image/") -> Icons.Outlined.Image
        mimeType.startsWith("video/") -> Icons.Outlined.VideoFile
        mimeType.startsWith("audio/") -> Icons.Outlined.AudioFile
        mimeType.contains("pdf") -> Icons.Outlined.PictureAsPdf
        mimeType.contains("word") || mimeType.contains("document") -> Icons.Outlined.Description
        mimeType.contains("sheet") || mimeType.contains("excel") -> Icons.Outlined.TableChart
        mimeType.contains("zip") || mimeType.contains("rar") -> Icons.Outlined.FolderZip
        else -> Icons.Outlined.AttachFile
    }
}

/**
 * 根据MIME类型获取附件颜色
 */
private fun getAttachmentColor(mimeType: String): Color {
    return when {
        mimeType.startsWith("image/") -> Color(0xFF4CAF50)  // Green
        mimeType.startsWith("video/") -> Color(0xFFF44336)  // Red
        mimeType.startsWith("audio/") -> Color(0xFF9C27B0)  // Purple
        mimeType.contains("pdf") -> Color(0xFFE53935)       // Dark Red
        mimeType.contains("word") || mimeType.contains("document") -> Color(0xFF2196F3)  // Blue
        mimeType.contains("sheet") || mimeType.contains("excel") -> Color(0xFF4CAF50)    // Green
        mimeType.contains("zip") || mimeType.contains("rar") -> Color(0xFFFF9800)        // Orange
        else -> Color(0xFF757575)  // Grey
    }
}
