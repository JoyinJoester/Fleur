package takagi.ru.fleur.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import takagi.ru.fleur.ui.theme.ArchiveGreen
import takagi.ru.fleur.ui.theme.DeleteRed
import takagi.ru.fleur.ui.theme.FleurAnimation
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * 可滑动的邮件项组件
 * 支持左右滑动手势进行归档和删除操作
 * M3E优化：400ms动画，DecelerateEasing缓动，30%触发阈值，触觉反馈
 * 
 * @param email 邮件对象
 * @param isSelected 是否选中
 * @param isMultiSelectMode 是否多选模式
 * @param isScrolling 是否正在滚动（用于延迟加载图片）
 * @param leftSwipeAction 左滑操作配置
 * @param rightSwipeAction 右滑操作配置
 * @param onClick 点击回调
 * @param onLongClick 长按回调
 * @param onSwipeAction 滑动操作回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableEmailItem(
    email: takagi.ru.fleur.domain.model.Email,
    isSelected: Boolean = false,
    isMultiSelectMode: Boolean = false,
    isScrolling: Boolean = false,
    leftSwipeAction: takagi.ru.fleur.ui.screens.folder.SwipeAction? = null,
    rightSwipeAction: takagi.ru.fleur.ui.screens.folder.SwipeAction? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onSwipeAction: (takagi.ru.fleur.ui.screens.folder.EmailAction) -> Unit,
    onStar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    
    // 如果没有配置滑动操作，直接显示邮件项
    if (leftSwipeAction == null && rightSwipeAction == null) {
        EmailListItem(
            email = email,
            isSelected = isSelected,
            isScrolling = isScrolling,
            onItemClick = onClick,
            onItemLongClick = onLongClick,
            onStar = onStar,
            modifier = modifier
        )
        return
    }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // 右滑操作
                    rightSwipeAction?.let { action ->
                        if (!hasTriggeredHaptic) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            hasTriggeredHaptic = true
                        }
                        onSwipeAction(action.action)
                        false // 返回 false，让卡片自动回弹，由 ViewModel 更新列表来移除 item
                    } ?: false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // 左滑操作
                    leftSwipeAction?.let { action ->
                        if (!hasTriggeredHaptic) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            hasTriggeredHaptic = true
                        }
                        onSwipeAction(action.action)
                        false // 返回 false，让卡片自动回弹，由 ViewModel 更新列表来移除 item
                    } ?: false
                }
                SwipeToDismissBoxValue.Settled -> {
                    hasTriggeredHaptic = false
                    false
                }
            }
        },
        // 设置滑动阈值为30%
        positionalThreshold = { distance -> distance * 0.3f }
    )
    
    // 监听滑动进度，在达到阈值时触发触觉反馈
    LaunchedEffect(dismissState.progress) {
        if (dismissState.progress >= 0.3f && !hasTriggeredHaptic) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            hasTriggeredHaptic = true
        } else if (dismissState.progress < 0.3f && hasTriggeredHaptic) {
            hasTriggeredHaptic = false
        }
    }
    
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = rightSwipeAction != null,
        enableDismissFromEndToStart = leftSwipeAction != null,
        backgroundContent = {
            SwipeBackground(
                dismissState = dismissState,
                leftSwipeAction = leftSwipeAction,
                rightSwipeAction = rightSwipeAction
            )
        },
        content = {
            EmailListItem(
                email = email,
                isSelected = isSelected,
                isScrolling = isScrolling,
                onItemClick = onClick,
                onItemLongClick = onLongClick,
                onStar = onStar
            )
        }
    )
}

/**
 * 滑动背景
 * 根据滑动方向显示不同的背景色和图标
 * 使用DecelerateEasing缓动曲线，400ms动画时长
 * 实现滑动进度实时反馈：背景色渐变、图标缩放
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(
    dismissState: SwipeToDismissBoxState,
    leftSwipeAction: takagi.ru.fleur.ui.screens.folder.SwipeAction?,
    rightSwipeAction: takagi.ru.fleur.ui.screens.folder.SwipeAction?
) {
    val direction = dismissState.dismissDirection
    val progress = dismissState.progress
    
    // 根据滑动方向和配置确定背景颜色和图标
    val (targetColor, icon) = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> {
            rightSwipeAction?.let { 
                it.backgroundColor to it.icon 
            } ?: (Color.Transparent to null)
        }
        SwipeToDismissBoxValue.EndToStart -> {
            leftSwipeAction?.let { 
                it.backgroundColor to it.icon 
            } ?: (Color.Transparent to null)
        }
        SwipeToDismissBoxValue.Settled -> Color.Transparent to null
    }
    
    // 实时背景色渐变：根据滑动进度从透明到目标颜色
    val backgroundColor = if (direction != SwipeToDismissBoxValue.Settled && targetColor != Color.Transparent) {
        lerp(Color.Transparent, targetColor, progress.coerceIn(0f, 1f))
    } else {
        Color.Transparent
    }
    
    // 图标缩放：根据滑动进度实时缩放
    // 0% -> 0.8x, 30% -> 1.0x, 100% -> 1.3x
    val iconScale = when {
        progress < 0.3f -> 0.8f + (progress / 0.3f) * 0.2f // 0.8 -> 1.0
        else -> 1.0f + ((progress - 0.3f) / 0.7f) * 0.3f // 1.0 -> 1.3
    }
    
    // 图标透明度：根据滑动进度实时调整
    val iconAlpha = progress.coerceIn(0f, 1f)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = when (direction) {
            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
            SwipeToDismissBoxValue.Settled -> Alignment.Center
        }
    ) {
        // 背景容器，与卡片大小一致
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .background(backgroundColor),
            contentAlignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.Settled -> Alignment.Center
            }
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .scale(iconScale)
                        .alpha(iconAlpha)
                )
            }
        }
    }
}
