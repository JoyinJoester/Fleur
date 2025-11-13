package takagi.ru.fleur.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import takagi.ru.fleur.ui.model.ConversationUiModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 对话列表项组件
 * 
 * 显示单个对话的信息，包括：
 * - 联系人头像
 * - 联系人名称
 * - 最后消息预览
 * - 时间戳
 * - 未读徽章
 * - 附件图标
 * 
 * @param conversation 对话数据
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun ConversationItem(
    conversation: ConversationUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 头像
            ContactAvatar(
                name = conversation.contactName,
                avatarUrl = conversation.contactAvatar,
                modifier = Modifier.size(56.dp)
            )
            
            // 内容区域
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 第一行：联系人名称和时间
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 联系人名称
                    Text(
                        text = conversation.contactName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (conversation.unreadCount > 0) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 时间戳
                    Text(
                        text = formatTimestamp(conversation.lastMessageTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (conversation.unreadCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 第二行：最后消息预览和未读徽章
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 消息预览
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // 未读徽章和附件图标
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 附件图标
                        if (conversation.hasAttachment) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "有附件",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        // 未读徽章
                        if (conversation.unreadCount > 0) {
                            UnreadBadge(count = conversation.unreadCount)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 联系人头像组件
 * 
 * 如果有头像URL则显示图片，否则显示首字母
 * 
 * @param name 联系人名称
 * @param avatarUrl 头像URL（可选）
 * @param modifier 修饰符
 */
@Composable
private fun ContactAvatar(
    name: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl != null) {
            // TODO: 使用 Coil 加载头像图片
            // AsyncImage(model = avatarUrl, contentDescription = name)
            
            // 暂时显示首字母
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        } else {
            // 显示首字母
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 未读徽章组件
 * 
 * 显示未读消息数量
 * 
 * @param count 未读数量
 * @param modifier 修饰符
 */
@Composable
private fun UnreadBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 格式化时间戳
 * 
 * - 今天：显示时间（如 "14:30"）
 * - 昨天：显示 "昨天"
 * - 本周：显示星期（如 "周一"）
 * - 更早：显示日期（如 "12/25"）
 * 
 * @param timestamp 时间戳
 * @return 格式化后的时间字符串
 */
private fun formatTimestamp(timestamp: Instant): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply {
        time = Date.from(timestamp.toJavaInstant())
    }
    
    // 判断是否是今天
    val isToday = now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                  now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)
    
    // 判断是否是昨天
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val isYesterday = yesterday.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                      yesterday.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)
    
    // 判断是否在本周
    val weekAgo = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -7)
    }
    val isThisWeek = messageTime.after(weekAgo)
    
    return when {
        isToday -> {
            // 今天：显示时间
            SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date.from(timestamp.toJavaInstant()))
        }
        isYesterday -> {
            // 昨天
            "昨天"
        }
        isThisWeek -> {
            // 本周：显示星期
            val dayOfWeek = when (messageTime.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "周日"
                Calendar.MONDAY -> "周一"
                Calendar.TUESDAY -> "周二"
                Calendar.WEDNESDAY -> "周三"
                Calendar.THURSDAY -> "周四"
                Calendar.FRIDAY -> "周五"
                Calendar.SATURDAY -> "周六"
                else -> ""
            }
            dayOfWeek
        }
        else -> {
            // 更早：显示日期
            SimpleDateFormat("MM/dd", Locale.getDefault())
                .format(Date.from(timestamp.toJavaInstant()))
        }
    }
}
