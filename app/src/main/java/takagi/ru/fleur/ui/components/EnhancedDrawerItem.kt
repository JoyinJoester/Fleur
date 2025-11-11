package takagi.ru.fleur.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.ui.theme.DrawerAnimations
import takagi.ru.fleur.ui.theme.DrawerDimens

/**
 * 增强版抽屉项组件
 * 支持选中状态、左侧指示条、圆角、悬停效果和 Badge
 * 
 * @param icon 图标
 * @param label 标签文本
 * @param badge 可选的未读数 Badge
 * @param isSelected 是否为选中状态
 * @param onClick 点击回调
 */
@Composable
fun EnhancedDrawerItem(
    icon: ImageVector,
    label: String,
    badge: Int? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 悬停状态（用于桌面端）
    var isHovered by remember { mutableStateOf(false) }
    
    // 图标缩放动画（悬停时放大）
    val iconScale by animateFloatAsState(
        targetValue = if (isHovered) DrawerAnimations.HoverIconScale else DrawerAnimations.NormalScale,
        animationSpec = DrawerAnimations.hoverSpring(),
        label = "icon_scale"
    )
    
    // 根据选中状态设置背景色
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    // 语义标签
    val badgeText = if (badge != null && badge > 0) {
        ", $badge 条未读"
    } else {
        ""
    }
    val contentDesc = "$label$badgeText${if (isSelected) ", 已选中" else ""}"
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                this.contentDescription = contentDesc
                role = Role.Button
            }
    ) {
        // 左侧选中指示条
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(DrawerDimens.FolderItemIndicatorWidth)
                    .height(DrawerDimens.MinTouchTargetSize)
                    .align(Alignment.CenterStart)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(
                            topEnd = DrawerDimens.FolderItemCornerRadius,
                            bottomEnd = DrawerDimens.FolderItemCornerRadius
                        )
                    )
            )
        }
        
        // 主内容区域
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isSelected) DrawerDimens.FolderItemIndicatorWidth else 0.dp
                )
                .clip(RoundedCornerShape(DrawerDimens.FolderItemCornerRadius))
                .clickable(onClick = onClick),
            color = backgroundColor,
            shape = RoundedCornerShape(DrawerDimens.FolderItemCornerRadius)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = DrawerDimens.FolderItemHorizontalPadding,
                        vertical = DrawerDimens.FolderItemVerticalPadding
                    )
                    .heightIn(min = DrawerDimens.MinTouchTargetSize),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 左侧：图标和标签
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 图标（带缩放动画）
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(DrawerDimens.FolderItemIconSize)
                            .scale(iconScale),
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            contentColor
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(DrawerDimens.FolderItemIconTextSpacing))
                    
                    // 标签文本
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor
                    )
                }
                
                // 右侧：Badge（如果有）
                if (badge != null && badge > 0) {
                    EnhancedBadge(count = badge)
                }
            }
        }
    }
}

/**
 * 增强版 Badge 组件
 * 支持淡入淡出动画和数字格式化
 * 
 * @param count 未读数量
 */
@Composable
private fun EnhancedBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    // Badge 显示文本（>99 显示 "99+"）
    val badgeText = if (count > 99) "99+" else count.toString()
    
    AnimatedVisibility(
        visible = count > 0,
        enter = DrawerAnimations.badgeEnterAnimation(),
        exit = DrawerAnimations.badgeExitAnimation()
    ) {
        Surface(
            modifier = modifier
                .widthIn(min = DrawerDimens.FolderItemBadgeMinWidth)
                .padding(DrawerDimens.FolderItemBadgePadding),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.error
        ) {
            Box(
                modifier = Modifier.padding(
                    horizontal = 6.dp,
                    vertical = 2.dp
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}
