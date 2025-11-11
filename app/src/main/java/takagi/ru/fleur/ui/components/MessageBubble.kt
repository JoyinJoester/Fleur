package takagi.ru.fleur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import takagi.ru.fleur.ui.model.EmailUiModel
import takagi.ru.fleur.ui.theme.FleurElevation
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * 消息气泡组件
 * 用于聊天视图显示邮件
 * 
 * @param email 邮件UI模型
 * @param isSent 是否为发送的消息（true=右侧，false=左侧）
 * @param showAvatar 是否显示头像
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun MessageBubble(
    email: EmailUiModel,
    isSent: Boolean,
    showAvatar: Boolean = true,
    onClick: () -> Unit = {},
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
            Avatar(
                name = email.fromName,
                color = email.accountColor,
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
                    text = email.fromName.ifEmpty { email.fromAddress },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                )
            }
            
            // 气泡卡片
            Card(
                modifier = Modifier,
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
                colors = CardDefaults.cardColors(
                    containerColor = if (isSent) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = FleurElevation.Level2
                ),
                onClick = onClick
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // 主题（如果不为空）
                    if (email.subject.isNotEmpty()) {
                        Text(
                            text = email.subject,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.padding(top = 4.dp))
                    }
                    
                    // 消息内容
                    Text(
                        text = email.preview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // 时间戳
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        text = formatMessageTime(email.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
        
        // 发送消息：右侧显示头像
        if (isSent && showAvatar) {
            Avatar(
                name = "我",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * 头像组件
 * 显示首字母
 */
@Composable
private fun Avatar(
    name: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            color = androidx.compose.ui.graphics.Color.White
        )
    }
}

/**
 * 格式化消息时间
 * 显示具体时间
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
