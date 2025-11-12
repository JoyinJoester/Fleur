package takagi.ru.fleur.ui.screens.folder

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import takagi.ru.fleur.ui.components.EmailListItemSkeleton
import takagi.ru.fleur.ui.components.SwipeableEmailItem
import takagi.ru.fleur.ui.theme.FleurAnimation

/**
 * 文件夹页面模板
 * 所有文件夹页面（已发送、草稿箱、星标、归档、垃圾箱）的共享模板
 * 
 * @param config 文件夹配置
 * @param uiState UI 状态
 * @param onNavigateBack 返回回调
 * @param onNavigateToEmailDetail 导航到邮件详情回调
 * @param onNavigateToCompose 导航到撰写页面回调
 * @param onRefresh 刷新回调
 * @param onLoadMore 加载更多回调
 * @param onEmailAction 单个邮件操作回调
 * @param onBatchAction 批量邮件操作回调
 * @param onEnterMultiSelect 进入多选模式回调
 * @param onExitMultiSelect 退出多选模式回调
 * @param onToggleSelection 切换选中状态回调
 * @param onDismissError 关闭错误提示回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreenTemplate(
    config: FolderConfig,
    uiState: FolderUiState,
    onNavigateBack: () -> Unit,
    onNavigateToEmailDetail: (String) -> Unit,
    onNavigateToCompose: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onEmailAction: (String, EmailAction) -> Unit,
    onBatchAction: (List<String>, EmailAction) -> Unit,
    onEnterMultiSelect: (String) -> Unit,
    onExitMultiSelect: () -> Unit,
    onToggleSelection: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    
    // 检测滚动状态（用于延迟加载图片）
    val isScrolling by remember {
        derivedStateOf {
            listState.isScrollInProgress
        }
    }
    
    // 首次加载状态（用于显示骨架屏）
    var showSkeleton by remember { mutableStateOf(true) }
    
    // 500ms 后隐藏骨架屏
    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading && uiState.emails.isEmpty()) {
            showSkeleton = true
        } else {
            delay(500)
            showSkeleton = false
        }
    }
    
    // 下拉刷新状态
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }
    
    // 刷新完成后重置状态
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }
    
    // 检测滚动到底部，触发分页加载
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && 
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3 &&
            !uiState.isLoading &&
            uiState.hasMorePages
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }
    
    // 显示错误消息（红色强调色，带重试按钮）
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            val result = snackbarHostState.showSnackbar(
                message = error.getUserMessage(),
                actionLabel = "重试",
                withDismissAction = true
            )
            
            if (result == SnackbarResult.ActionPerformed) {
                // 用户点击了重试按钮
                onRefresh()
            }
            
            onDismissError()
        }
    }
    
    // 显示操作成功的 Snackbar（绿色强调色，带撤销按钮）
    LaunchedEffect(uiState.showUndoSnackbar) {
        if (uiState.showUndoSnackbar && uiState.lastAction != null) {
            val action = uiState.lastAction
            val message = getActionSuccessMessage(action.action, action.emailIds.size)
            
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (action.canUndo) "撤销" else null,
                withDismissAction = true
            )
            
            if (result == SnackbarResult.ActionPerformed && action.canUndo) {
                // 用户点击了撤销按钮
                // 这里需要通过 ViewModel 处理撤销逻辑
                // 暂时不实现，因为需要添加新的回调参数
            }
        }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            if (uiState.isMultiSelectMode) {
                // 多选模式工具栏
                MultiSelectTopBar(
                    selectedCount = uiState.selectedEmailIds.size,
                    onExitMultiSelect = onExitMultiSelect,
                    onDelete = {
                        onBatchAction(uiState.selectedEmailIds.toList(), EmailAction.DELETE)
                    },
                    onArchive = {
                        onBatchAction(uiState.selectedEmailIds.toList(), EmailAction.ARCHIVE)
                    },
                    onMarkRead = {
                        onBatchAction(uiState.selectedEmailIds.toList(), EmailAction.MARK_READ)
                    },
                    onMarkUnread = {
                        onBatchAction(uiState.selectedEmailIds.toList(), EmailAction.MARK_UNREAD)
                    }
                )
            } else {
                // 普通顶部应用栏
                TopAppBar(
                    title = { Text(config.title) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        // 自定义操作按钮
                        config.topBarActions.forEach { action ->
                            IconButton(onClick = action.onClick) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.contentDescription
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            // 根据配置显示 FAB
            if (config.showFab && config.fabIcon != null && config.fabAction != null) {
                FloatingActionButton(
                    onClick = config.fabAction,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = config.fabIcon,
                        contentDescription = "浮动操作按钮"
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                // 根据消息类型选择颜色
                val isError = uiState.error != null
                val isSuccess = uiState.showUndoSnackbar
                
                val containerColor = when {
                    isError -> MaterialTheme.colorScheme.errorContainer
                    isSuccess -> Color(0xFF4CAF50) // 绿色强调色
                    else -> MaterialTheme.colorScheme.inverseSurface
                }
                
                val contentColor = when {
                    isError -> MaterialTheme.colorScheme.onErrorContainer
                    isSuccess -> Color.White
                    else -> MaterialTheme.colorScheme.inverseOnSurface
                }
                
                Snackbar(
                    snackbarData = data,
                    containerColor = containerColor,
                    contentColor = contentColor,
                    actionColor = contentColor
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 主内容区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                when {
                // 加载中状态（首次加载）- 显示骨架屏
                uiState.isLoading && uiState.emails.isEmpty() && showSkeleton -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(8) { index ->
                            EmailListItemSkeleton()
                        }
                    }
                }
                
                // 错误状态
                uiState.error != null && uiState.emails.isEmpty() -> {
                    FolderErrorState(
                        error = uiState.error,
                        onRetry = onRefresh
                    )
                }
                
                // 空状态
                uiState.emails.isEmpty() && !uiState.isLoading -> {
                    FolderEmptyState(config = config.emptyStateConfig)
                }
                
                // 邮件列表
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.emails,
                            key = { email -> email.id }
                        ) { email ->
                            val index = uiState.emails.indexOf(email)
                            
                            // 交错淡入动画
                            val alpha by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = FleurAnimation.FAST_DURATION,
                                    delayMillis = index * FleurAnimation.STAGGER_DELAY
                                ),
                                label = "emailItemFadeIn"
                            )
                            
                            SwipeableEmailItem(
                                email = email,
                                isSelected = email.id in uiState.selectedEmailIds,
                                isMultiSelectMode = uiState.isMultiSelectMode,
                                isScrolling = isScrolling,
                                leftSwipeAction = config.swipeActions.leftSwipe,
                                rightSwipeAction = config.swipeActions.rightSwipe,
                                onClick = {
                                    if (uiState.isMultiSelectMode) {
                                        onToggleSelection(email.id)
                                    } else {
                                        onNavigateToEmailDetail(email.id)
                                    }
                                },
                                onLongClick = {
                                    if (!uiState.isMultiSelectMode) {
                                        onEnterMultiSelect(email.id)
                                    }
                                },
                                onSwipeAction = { action ->
                                    onEmailAction(email.id, action)
                                },
                                onStar = {
                                    // 根据当前星标状态切换：已标星则取消，未标星则添加
                                    val action = if (email.isStarred) {
                                        EmailAction.UNSTAR
                                    } else {
                                        EmailAction.STAR
                                    }
                                    onEmailAction(email.id, action)
                                },
                                modifier = Modifier.alpha(alpha)
                            )
                        }
                        
                        // 加载更多指示器
                        if (uiState.isLoading && uiState.emails.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
                }
                
                // 下拉刷新指示器
                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
            
            // 顶部线性进度指示器（同步数据时显示）
            if (uiState.isRefreshing || (uiState.isLoading && uiState.emails.isNotEmpty())) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            // 操作执行中的遮罩层（当正在执行批量操作时显示）
            // 注意：这里简化处理，实际应该在 ViewModel 中添加 isOperating 状态
            // 暂时使用 isLoading && isMultiSelectMode 作为判断条件
            OperationOverlay(
                isOperating = uiState.isLoading && uiState.isMultiSelectMode
            )
        }
    }
}

/**
 * 操作执行中的遮罩层
 * 显示半透明遮罩和进度指示器
 * 
 * @param isOperating 是否正在执行操作
 * @param modifier 修饰符
 */
@Composable
private fun OperationOverlay(
    isOperating: Boolean,
    modifier: Modifier = Modifier
) {
    if (isOperating) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 获取操作成功消息
 */
private fun getActionSuccessMessage(action: EmailAction, count: Int): String {
    val countText = if (count > 1) "$count 封邮件" else "邮件"
    return when (action) {
        EmailAction.DELETE -> "已将 $countText 移至垃圾箱"
        EmailAction.ARCHIVE -> "已归档 $countText"
        EmailAction.UNARCHIVE -> "已将 $countText 移至收件箱"
        EmailAction.STAR -> "已为 $countText 添加星标"
        EmailAction.UNSTAR -> "已取消 $countText 的星标"
        EmailAction.RESTORE -> "已恢复 $countText"
        EmailAction.MARK_READ -> "已标记为已读"
        EmailAction.MARK_UNREAD -> "已标记为未读"
    }
}
