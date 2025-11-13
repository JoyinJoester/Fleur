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
 * 
 * @param message 消息数据
 * @param isSent 是否为发送的消息
 * @param showAvatar 是否显示头像
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
            onClick = onClick,
            onLongClick = onLongClick,
            onImageClick = onImageClick,
            haptic = haptic
        )
    }
}

/**
 * 消息气泡内容
 * 
 * 分离出来以便应用动画
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubbleContent(
    message: MessageUiModel,
    isSent: Boolean,
    showAvatar: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onImageClick: (Int) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
    ) {
        // 接收消息：左侧显示头像
        if (!isSent && showAvatar) {
            MessageAvatar(
                name = message.senderName,
                avatarUrl = message.senderAvatar,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        
        // 消息气泡
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isSent) Alignment.End else Alignment.Start
        ) {
            // 发件人名称（接收消息时显示）
            if (!isSent && showAvatar) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                )
            }
            
            // 回复引用（如果有）
            message.replyTo?.let { replyMessage ->
                ReplyReference(
                    replyMessage = replyMessage,
                    isSent = isSent,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            // 气泡卡片
            Surface(
                modifier = Modifier.combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                ),
                shape = if (isSent) {
                    // 发送消息：右侧圆角小
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 4.dp
                    )
                } else {
                    // 接收消息：左侧圆角小
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                },
                color = if (isSent) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // 消息内容
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSent) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
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
                    
                    // 底部信息：时间戳和状态
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        // 时间戳
                        Text(
                            text = formatMessageTime(message.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSent) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                        
                        // 发送状态指示器（仅发送的消息）
                        if (isSent) {
                            MessageStatusIndicator(status = message.status)
                        }
                    }
                }
            }
        }
        
        // 发送消息：右侧显示头像
        if (isSent && showAvatar) {
            MessageAvatar(
                name = "我",
                avatarUrl = null,
                modifier = Modifier.padding(start = 8.dp)
            )
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
            .background(MaterialTheme.colorScheme.primary),
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
        modifier = modifier.widthIn(max = 280.dp),
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
