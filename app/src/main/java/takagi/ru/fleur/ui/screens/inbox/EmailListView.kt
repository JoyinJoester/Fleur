package takagi.ru.fleur.ui.screens.inbox

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.ui.components.AnimatedListItem
import takagi.ru.fleur.ui.components.EmailListItem
import takagi.ru.fleur.ui.components.EmailListItemSkeleton
import takagi.ru.fleur.ui.components.SwipeableEmailItem
import takagi.ru.fleur.ui.model.toUiModel
import takagi.ru.fleur.ui.theme.FleurAnimation

private const val TAG = "EmailListView"

/**
 * 邮件列表视图（传统视图）
 * 使用 LazyColumn 显示邮件列表，支持交错进入动画和位置变化动画
 * M3E优化：300ms下拉刷新动画，触觉反馈
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EmailListView(
    emails: List<Email>,
    isRefreshing: Boolean,
    isLoading: Boolean = false,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onEmailClick: (String) -> Unit,
    onEmailLongPress: (String) -> Unit,
    onArchive: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onMarkRead: (String) -> Unit = {},
    onMarkUnread: (String) -> Unit = {},
    onStar: (String) -> Unit = {},
    isMultiSelectMode: Boolean = false,
    selectedEmailIds: Set<String> = emptySet(),
    swipeRightAction: takagi.ru.fleur.domain.model.SwipeAction = takagi.ru.fleur.domain.model.SwipeAction.ARCHIVE,
    swipeLeftAction: takagi.ru.fleur.domain.model.SwipeAction = takagi.ru.fleur.domain.model.SwipeAction.DELETE,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val haptic = LocalHapticFeedback.current
    
    // UI层最后的安全检查：对邮件列表进行去重
    // 这是防止LazyColumn崩溃的最后一道防线
    val uniqueEmails = remember(emails) {
        val originalSize = emails.size
        val deduped = emails.distinctBy { it.id }
        val duplicateCount = originalSize - deduped.size
        
        if (duplicateCount > 0) {
            Log.e(TAG, "UI层发现 $duplicateCount 个重复邮件！原始数量: $originalSize, 去重后: ${deduped.size}")
            // 记录重复的邮件ID用于调试
            val emailIds = emails.map { it.id }
            val duplicateIds = emailIds.groupingBy { it }.eachCount().filter { it.value > 1 }
            Log.e(TAG, "重复的邮件ID: $duplicateIds")
        }
        
        deduped
    }
    
    // 检测滚动状态（用于延迟加载图片）
    val isScrolling by remember {
        derivedStateOf {
            listState.isScrollInProgress
        }
    }
    
    // 首次加载状态（用于显示骨架屏）
    var showSkeleton by remember { mutableStateOf(true) }
    
    // 500ms 后隐藏骨架屏
    LaunchedEffect(isLoading) {
        if (isLoading && emails.isEmpty()) {
            showSkeleton = true
        } else {
            delay(500)
            showSkeleton = false
        }
    }
    
    // 监听滚动位置，实现分页加载
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= uniqueEmails.size - 5
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }
    
    // 处理下拉刷新
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }
    
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            // M3E优化：触发触觉反馈
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onRefresh()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        // 显示骨架屏或邮件列表
        if (isLoading && uniqueEmails.isEmpty() && showSkeleton) {
            // 首次加载显示骨架屏
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(8) { index ->
                    EmailListItemSkeleton()
                }
            }
        } else {
            // 显示邮件列表（使用去重后的列表）
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = uniqueEmails,
                    key = { email -> email.id }
                ) { email ->
                    val index = uniqueEmails.indexOf(email)
                    
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
                        isSelected = email.id in selectedEmailIds,
                        isMultiSelectMode = isMultiSelectMode,
                        isScrolling = isScrolling,
                        leftSwipeAction = createSwipeActionConfig(swipeLeftAction),
                        rightSwipeAction = createSwipeActionConfig(swipeRightAction),
                        onClick = { 
                            android.util.Log.d("EmailListView", "点击邮件: ${email.id}")
                            onEmailClick(email.id)
                        },
                        onLongClick = { onEmailLongPress(email.id) },
                        onSwipeAction = { action ->
                            when (action) {
                                takagi.ru.fleur.ui.screens.folder.EmailAction.ARCHIVE -> onArchive(email.id)
                                takagi.ru.fleur.ui.screens.folder.EmailAction.DELETE -> onDelete(email.id)
                                takagi.ru.fleur.ui.screens.folder.EmailAction.MARK_READ -> onMarkRead(email.id)
                                takagi.ru.fleur.ui.screens.folder.EmailAction.MARK_UNREAD -> onMarkUnread(email.id)
                                takagi.ru.fleur.ui.screens.folder.EmailAction.STAR -> onStar(email.id)
                                else -> {}
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alpha)
                            .animateItemPlacement()
                    )
                }
            }
        }
        
        // M3E优化：下拉刷新指示器，使用MaterialTheme颜色
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 将 domain 层的 SwipeAction 转换为 UI 层的滑动配置
 */
private fun createSwipeActionConfig(
    action: takagi.ru.fleur.domain.model.SwipeAction
): takagi.ru.fleur.ui.screens.folder.SwipeAction {
    return when (action) {
        takagi.ru.fleur.domain.model.SwipeAction.ARCHIVE -> takagi.ru.fleur.ui.screens.folder.SwipeAction(
            icon = Icons.Default.Archive,
            backgroundColor = Color(0xFF66BB6A), // 绿色
            action = takagi.ru.fleur.ui.screens.folder.EmailAction.ARCHIVE
        )
        takagi.ru.fleur.domain.model.SwipeAction.DELETE -> takagi.ru.fleur.ui.screens.folder.SwipeAction(
            icon = Icons.Default.Delete,
            backgroundColor = Color(0xFFEF5350), // 红色
            action = takagi.ru.fleur.ui.screens.folder.EmailAction.DELETE
        )
        takagi.ru.fleur.domain.model.SwipeAction.MARK_READ -> takagi.ru.fleur.ui.screens.folder.SwipeAction(
            icon = Icons.Default.MarkEmailRead,
            backgroundColor = Color(0xFF42A5F5), // 蓝色
            action = takagi.ru.fleur.ui.screens.folder.EmailAction.MARK_READ
        )
        takagi.ru.fleur.domain.model.SwipeAction.MARK_UNREAD -> takagi.ru.fleur.ui.screens.folder.SwipeAction(
            icon = Icons.Default.MarkEmailUnread,
            backgroundColor = Color(0xFFAB47BC), // 紫色
            action = takagi.ru.fleur.ui.screens.folder.EmailAction.MARK_UNREAD
        )
        takagi.ru.fleur.domain.model.SwipeAction.STAR -> takagi.ru.fleur.ui.screens.folder.SwipeAction(
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFFFFA726), // 橙色
            action = takagi.ru.fleur.ui.screens.folder.EmailAction.STAR
        )
    }
}
