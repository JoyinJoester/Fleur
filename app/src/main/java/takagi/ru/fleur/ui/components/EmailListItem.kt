package takagi.ru.fleur.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.ui.theme.FleurAnimation
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmailListItem(
    email: Email,
    isSelected: Boolean = false,
    animationIndex: Int = 0,
    isScrolling: Boolean = false,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit = {},
    onStar: () -> Unit = {},
    onArchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Log.d("EmailListItem", "渲染邮件: ${email.id}")
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp) // 固定高度，符合M3规范
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {
                    Log.d("EmailListItem", "点击邮件: ${email.id}")
                    onItemClick()
                },
                onLongClick = {
                    Log.d("EmailListItem", "长按邮件: ${email.id}")
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onItemLongClick()
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 未读指示器（条件显示，带淡入淡出动画）
                AnimatedVisibility(
                    visible = !email.isRead,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = FleurAnimation.FAST_DURATION,
                            easing = FleurAnimation.FastOutSlowIn
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = FleurAnimation.FAST_DURATION,
                            easing = FleurAnimation.FastOutSlowIn
                        )
                    )
                ) {
                    UnreadIndicator(
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                
                // 头像
                AvatarWithInitial(
                    name = email.from.name ?: email.from.address,
                    showPlaceholder = false, // 始终显示头像，不使用占位符
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
            
            // 邮件内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // 第一行：发件人和时间/星标列
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = email.from.name ?: email.from.address,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Normal
                        ),
                        // 未读：使用onSurface（高对比度），已读：使用onSurfaceVariant（较淡）
                        color = if (!email.isRead) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 时间戳
                    TimeStamp(timestamp = email.timestamp)
                }
                
                // 第二行：主题
                Text(
                    text = email.subject.ifEmpty { "(无主题)" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (!email.isRead) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    // 未读：使用onSurface（高对比度），已读：使用onSurfaceVariant（较淡）
                    color = if (!email.isRead) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 第三行：预览
                Text(
                    text = email.bodyPreview,
                    style = MaterialTheme.typography.bodySmall,
                    // 未读：70%透明度，已读：50%透明度（更淡）
                    color = if (!email.isRead) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
                // 右侧间距
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // 星标按钮 - 常驻右下角，可点击
            IconButton(
                onClick = {
                    Log.d("EmailListItem", "点击星标: ${email.id}")
                    onStar()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 4.dp)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = if (email.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (email.isStarred) "取消星标" else "添加星标",
                    tint = if (email.isStarred) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 头像组件 - 显示首字母
 * 支持滚动时显示占位符以提升性能
 */
@Composable
private fun AvatarWithInitial(
    name: String,
    showPlaceholder: Boolean = false,
    modifier: Modifier = Modifier
) {
    val initial = getInitial(name)
    val backgroundColor = getColorForName(name)
    
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (showPlaceholder) 
                    MaterialTheme.colorScheme.surfaceVariant 
                else 
                    backgroundColor
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!showPlaceholder) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 获取名称的首字符
 */
private fun getInitial(name: String): String {
    return if (name.isNotEmpty()) {
        val firstChar = name.trim().first()
        // 如果是中文，直接返回第一个字符
        if (firstChar.code > 255) {
            firstChar.toString()
        } else {
            // 如果是英文，返回大写首字母
            firstChar.uppercaseChar().toString()
        }
    } else {
        "?"
    }
}

/**
 * 根据名称生成一个稳定的颜色
 */
private fun getColorForName(name: String): Color {
    val colors = listOf(
        Color(0xFF1976D2), // 蓝色
        Color(0xFF388E3C), // 绿色
        Color(0xFFD32F2F), // 红色
        Color(0xFF7B1FA2), // 紫色
        Color(0xFFF57C00), // 橙色
        Color(0xFF0097A7), // 青色
        Color(0xFFC2185B), // 粉色
        Color(0xFF5D4037), // 棕色
        Color(0xFF455A64), // 蓝灰色
        Color(0xFF689F38)  // 浅绿色
    )
    
    val hash = name.hashCode()
    val index = (hash and 0x7FFFFFFF) % colors.size
    return colors[index]
}

/**
 * 格式化时间戳
 */
private fun formatTimestamp(timestamp: Instant): String {
    val timestampMillis = timestamp.toEpochMilliseconds()
    val now = System.currentTimeMillis()
    val diff = now - timestampMillis
    
    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            format.format(Date(timestampMillis))
        }
        diff < 7 * 24 * 60 * 60 * 1000 -> {
            val format = SimpleDateFormat("EEE HH:mm", Locale.getDefault())
            format.format(Date(timestampMillis))
        }
        else -> {
            val format = SimpleDateFormat("MM/dd", Locale.getDefault())
            format.format(Date(timestampMillis))
        }
    }
}

/**
 * 未读指示器组件
 * 显示4dp宽的蓝色指示条，带有左侧圆角和淡入淡出动画
 */
@Composable
private fun UnreadIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(4.dp)
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(
                    topStart = 2.dp,
                    bottomStart = 2.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp
                )
            )
    )
}

/**
 * 时间戳显示组件
 * 
 * @param timestamp 邮件时间戳
 * @param modifier 修饰符
 */
@Composable
private fun TimeStamp(
    timestamp: Instant,
    modifier: Modifier = Modifier
) {
    Text(
        text = formatTimestamp(timestamp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
