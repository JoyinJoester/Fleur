package takagi.ru.fleur.ui.screens.chat.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import takagi.ru.fleur.ui.model.MessageStatus
import takagi.ru.fleur.ui.model.MessageUiModel
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * 增强版消息气泡组件
 * 
 * 用于 ChatDetail 页面显示消息，支持：
 * - 长按手势
 * - 发送状态指示器
 * - 回复引用显示
 * - 附件显示
 * - 优化的气泡样式
 * - 消息分组优化
 * - Telegram 风格布局
 * 
 * @param message 消息数据
 * @param isSent 是否为发送的消息
 * @param showAvatar 是否显示头像（已废弃，仅保留参数兼容性）
 * @param showSenderName 是否显示发件人名称（已废弃，仅保留参数兼容性）
 * @param showTimestamp 是否显示时间戳（默认为 true）
 * @param isGroupedWithPrevious 是否与上一条消息分组
 * @param isGroupedWithNext 是否与下一条消息分组
 * @param onClick 点击回调
 * @param onLongClick 长按回调
 * @param onImageClick 图片点击回调，参数为图片索引
 * @param modifier 修饰符
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedMessageBubble(
    message: MessageUiModel,
    isSent: Boolean,
    showAvatar: Boolean = true,
    showSenderName: Boolean = true,
    showTimestamp: Boolean = true,
    isGroupedWithPrevious: Boolean = false,
    isGroupedWithNext: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onImageClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    // 消息出现动画
    androidx.compose.animation.AnimatedVisibility(
        visible = true,
        enter = androidx.compose.animation.fadeIn(
            animationSpec = androidx.compose.animation.core.tween(300)
        ) + androidx.compose.animation.slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = androidx.compose.animation.core.tween(300)
        ),
        modifier = modifier
    ) {
        MessageBubbleContent(
            message = message,
            isSent = isSent,
            showAvatar = showAvatar,
            showSenderName = showSenderName,
            showTimestamp = showTimestamp,
            isGroupedWithPrevious = isGroupedWithPrevious,
            isGroupedWithNext = isGroupedWithNext,
            onClick = onClick,
            onLongClick = onLongClick,
            onImageClick = onImageClick,
            haptic = haptic
        )
    }
}

/**
 * 消息气泡内容 - Telegram 风格
 * 
 * 简化布局：只显示气泡,时间戳在气泡内右上角
 * 不显示头像和发件人名称
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubbleContent(
    message: MessageUiModel,
    isSent: Boolean,
    showAvatar: Boolean,
    showSenderName: Boolean,
    showTimestamp: Boolean,
    isGroupedWithPrevious: Boolean,
    isGroupedWithNext: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onImageClick: (Int) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
    ) {
        // 消息气泡卡片（时间戳已集成在内）
        MessageBubbleCard(
            message = message,
            isSent = isSent,
            showTimestamp = showTimestamp,
            isGroupedWithPrevious = isGroupedWithPrevious,
            isGroupedWithNext = isGroupedWithNext,
            onClick = onClick,
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick()
            },
            onImageClick = onImageClick
        )
    }
}

/**
 * 消息气泡卡片组件 - Telegram 风格
 * 
 * 包含消息内容、时间戳、回复引用、附件和状态指示器
 * 时间戳显示在气泡内右上角
 * 支持连续消息的圆角处理：只有最后一条显示尖角
 * 
 * @param message 消息数据
 * @param isSent 是否为发送的消息
 * @param showTimestamp 是否显示时间戳
 * @param isGroupedWithPrevious 是否与上一条消息分组
 * @param isGroupedWithNext 是否与下一条消息分组
 * @param onClick 点击回调
 * @param onLongClick 长按回调
 * @param onImageClick 图片点击回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubbleCard(
    message: MessageUiModel,
    isSent: Boolean,
    showTimestamp: Boolean = true,
    isGroupedWithPrevious: Boolean = false,
    isGroupedWithNext: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 根据分组情况决定气泡形状
    val bubbleShape = when {
        // 不在分组中：所有角都是圆角，只有对应方向的角是尖的
        !isGroupedWithPrevious && !isGroupedWithNext -> {
            if (isSent) {
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 4.dp  // 右下尖角
                )
            } else {
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 4.dp,  // 左下尖角
                    bottomEnd = 16.dp
                )
            }
        }
        // 分组中的第一条：顶部圆角，底部圆角（无尖角）
        !isGroupedWithPrevious && isGroupedWithNext -> {
            RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp)
        }
        // 分组中的最后一条：顶部圆角，底部有尖角
        isGroupedWithPrevious && !isGroupedWithNext -> {
            if (isSent) {
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 4.dp  // 右下尖角
                )
            } else {
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 4.dp,  // 左下尖角
                    bottomEnd = 16.dp
                )
            }
        }
        // 分组中的中间消息：全部圆角
        else -> {
            RoundedCornerShape(16.dp)
        }
    }
    
    Surface(
        modifier = modifier
            .widthIn(min = 80.dp, max = 280.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .semantics {
                // 消息气泡的无障碍描述
                val bubbleDescription = buildString {
                    append("消息：${message.content}")
                    if (message.attachments.isNotEmpty()) {
                        append("，包含 ${message.attachments.size} 个附件")
                    }
                    if (isSent) {
                        val statusText = when (message.status) {
                            takagi.ru.fleur.ui.model.MessageStatus.SENDING -> "发送中"
                            takagi.ru.fleur.ui.model.MessageStatus.SENT -> "已发送"
                            takagi.ru.fleur.ui.model.MessageStatus.DELIVERED -> "已送达"
                            takagi.ru.fleur.ui.model.MessageStatus.READ -> "已读"
                            takagi.ru.fleur.ui.model.MessageStatus.FAILED -> "发送失败"
                        }
                        append("，状态：$statusText")
                    }
                }
                contentDescription = bubbleDescription
            },
        shape = bubbleShape,
        color = if (isSent) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = 1.dp
    ) {
        // 使用 Box 布局实现时间戳和状态指示器的定位
        Box {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // 回复引用（如果有）
                message.replyTo?.let { replyMessage ->
                    ReplyReference(
                        replyMessage = replyMessage,
                        isSent = isSent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 消息内容和时间戳行
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 消息内容
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSent) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    // 时间戳（右上角）
                    if (showTimestamp) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatMessageTime(message.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSent) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            }
                        )
                    }
                }
                
                // 附件（如果有）
                if (message.attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 使用 ImageAttachmentGrid 显示图片附件
                    val imageAttachments = message.attachments.filter { it.isImage }
                    if (imageAttachments.isNotEmpty()) {
                        ImageAttachmentGrid(
                            attachments = imageAttachments,
                            onImageClick = onImageClick
                        )
                    }
                    
                    // 显示非图片附件
                    val fileAttachments = message.attachments.filter { !it.isImage }
                    fileAttachments.forEach { attachment ->
                        Spacer(modifier = Modifier.height(4.dp))
                        AttachmentCard(
                            attachment = attachment,
                            onClick = {
                                // TODO: 下载或打开文件
                            }
                        )
                    }
                }
                
                // 为状态指示器预留底部空间（仅发送消息）
                if (isSent) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // 状态指示器：绝对定位在右下角（仅发送消息）
            if (isSent) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 4.dp, bottom = 4.dp)
                ) {
                    MessageStatusIndicator(status = message.status)
                }
            }
        }
    }
}

/**
 * 消息头像组件
 * 
 * @param name 名称
 * @param avatarUrl 头像URL（可选）
 * @param modifier 修饰符
 */
@Composable
private fun MessageAvatar(
    name: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .semantics {
                contentDescription = "$name 的头像"
            },
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl != null) {
            // TODO: 使用 Coil 加载头像
            // AsyncImage(model = avatarUrl, contentDescription = name)
            
            // 暂时显示首字母
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        } else {
            // 显示首字母
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 回复引用组件
 * 
 * 显示被回复的消息预览
 * 
 * @param replyMessage 被回复的消息
 * @param isSent 是否为发送的消息
 * @param modifier 修饰符
 */
@Composable
private fun ReplyReference(
    replyMessage: MessageUiModel,
    isSent: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isSent) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 左侧竖线
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(2.dp)
                    )
            )
            
            // 回复内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = replyMessage.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = replyMessage.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}



/**
 * 消息状态指示器
 * 
 * 显示消息的发送状态
 * 
 * @param status 消息状态
 */
@Composable
private fun MessageStatusIndicator(
    status: MessageStatus
) {
    when (status) {
        MessageStatus.SENDING -> {
            // 发送中
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "发送中",
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
        }
        MessageStatus.SENT, MessageStatus.DELIVERED -> {
            // 已发送/已送达
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "已发送",
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
        }
        MessageStatus.READ -> {
            // 已读（双勾）
            Row(
                horizontalArrangement = Arrangement.spacedBy((-4).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已读",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        MessageStatus.FAILED -> {
            // 发送失败
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "发送失败",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * 格式化消息时间
 * 
 * @param timestamp 时间戳
 * @return 格式化后的时间字符串
 */
private fun formatMessageTime(timestamp: Instant): String {
    val now = Clock.System.now()
    val duration = now - timestamp
    
    val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = localDateTime.hour
    val minute = localDateTime.minute
    
    val timeStr = String.format("%02d:%02d", hour, minute)
    
    return when {
        duration < 24.hours -> timeStr
        duration < 2.days -> "昨天 $timeStr"
        duration < 7.days -> {
            val dayOfWeek = when (localDateTime.dayOfWeek.value) {
                1 -> "周一"
                2 -> "周二"
                3 -> "周三"
                4 -> "周四"
                5 -> "周五"
                6 -> "周六"
                7 -> "周日"
                else -> ""
            }
            "$dayOfWeek $timeStr"
        }
        else -> "${localDateTime.monthNumber}/${localDateTime.dayOfMonth} $timeStr"
    }
}

/**
 * 消息显示配置
 * 
 * 用于控制消息气泡的显示元素（头像、时间戳、发件人名称）
 * 
 * @param showAvatar 是否显示头像
 * @param showTimestamp 是否显示时间戳
 * @param showSenderName 是否显示发件人名称
 */
data class MessageDisplayConfig(
    val showAvatar: Boolean,
    val showTimestamp: Boolean,
    val showSenderName: Boolean
)

/**
 * 计算消息显示配置
 * 
 * 根据消息在分组中的位置决定显示哪些元素：
 * - 分组内第一条消息：显示时间戳和发件人名称，不显示头像
 * - 分组内最后一条消息：显示头像，不显示时间戳和发件人名称
 * - 分组内中间消息：都不显示
 * - 单独消息（不在分组内）：全部显示
 * 
 * @param currentMessage 当前消息
 * @param previousMessage 前一条消息（可选）
 * @param nextMessage 后一条消息（可选）
 * @return 消息显示配置
 */
fun calculateMessageDisplayConfig(
    currentMessage: MessageUiModel,
    previousMessage: MessageUiModel?,
    nextMessage: MessageUiModel?
): MessageDisplayConfig {
    val isGroupedWithPrevious = previousMessage != null && 
        isSameGroup(currentMessage, previousMessage)
    val isGroupedWithNext = nextMessage != null && 
        isSameGroup(currentMessage, nextMessage)
    
    return when {
        // 单独消息（不在分组内）
        !isGroupedWithPrevious && !isGroupedWithNext -> {
            MessageDisplayConfig(
                showAvatar = true,
                showTimestamp = true,
                showSenderName = true
            )
        }
        // 分组内第一条消息
        !isGroupedWithPrevious && isGroupedWithNext -> {
            MessageDisplayConfig(
                showAvatar = false,
                showTimestamp = true,
                showSenderName = true
            )
        }
        // 分组内最后一条消息
        isGroupedWithPrevious && !isGroupedWithNext -> {
            MessageDisplayConfig(
                showAvatar = true,
                showTimestamp = false,
                showSenderName = false
            )
        }
        // 分组内中间消息
        else -> {
            MessageDisplayConfig(
                showAvatar = false,
                showTimestamp = false,
                showSenderName = false
            )
        }
    }
}

/**
 * 判断两条消息是否属于同一分组
 * 
 * 分组条件：
 * 1. 同一发送者
 * 2. 时间间隔小于 5 分钟
 * 
 * @param message1 第一条消息
 * @param message2 第二条消息
 * @return 是否属于同一分组
 */
fun isSameGroup(
    message1: MessageUiModel,
    message2: MessageUiModel
): Boolean {
    // 检查是否同一发送者
    if (message1.senderId != message2.senderId) {
        return false
    }
    
    // 检查时间间隔是否小于 5 分钟
    val timeDiff = kotlin.math.abs(
        (message1.timestamp - message2.timestamp).inWholeMinutes
    )
    
    return timeDiff < 5
}
