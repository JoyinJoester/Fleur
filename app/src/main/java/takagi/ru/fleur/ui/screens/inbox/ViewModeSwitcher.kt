package takagi.ru.fleur.ui.screens.inbox

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import takagi.ru.fleur.domain.model.Email

/**
 * 视图模式切换器
 * 使用 AnimatedContent 实现平滑的视图切换动画
 */
@Composable
fun ViewModeSwitcher(
    viewMode: ViewMode,
    emails: List<Email>,
    isRefreshing: Boolean,
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
    AnimatedContent(
        targetState = viewMode,
        modifier = modifier,
        transitionSpec = {
            // 300ms 淡入淡出 + 滑动动画
            (fadeIn() + slideInHorizontally { width -> width })
                .togetherWith(fadeOut() + slideOutHorizontally { width -> -width })
        },
        label = "view_mode_switch"
    ) { mode ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (mode) {
                ViewMode.LIST -> {
                    EmailListView(
                        emails = emails,
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        onLoadMore = onLoadMore,
                        onEmailClick = onEmailClick,
                        onEmailLongPress = onEmailLongPress,
                        onArchive = onArchive,
                        onDelete = onDelete,
                        onMarkRead = onMarkRead,
                        onMarkUnread = onMarkUnread,
                        onStar = onStar,
                        isMultiSelectMode = isMultiSelectMode,
                        selectedEmailIds = selectedEmailIds,
                        swipeRightAction = swipeRightAction,
                        swipeLeftAction = swipeLeftAction
                    )
                }
                ViewMode.CHAT -> {
                    EmailChatView(
                        emails = emails,
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        onLoadMore = onLoadMore,
                        onEmailClick = onEmailClick
                    )
                }
            }
        }
    }
}
