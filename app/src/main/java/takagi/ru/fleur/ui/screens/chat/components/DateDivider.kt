package takagi.ru.fleur.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期分隔线组件
 * 
 * 在消息列表中显示日期分隔，用于区分不同日期的消息
 * 
 * 显示格式：
 * - 今天：显示 "今天"
 * - 昨天：显示 "昨天"
 * - 本周：显示星期（如 "周一"）
 * - 本年：显示月日（如 "12月25日"）
 * - 更早：显示完整日期（如 "2023年12月25日"）
 * 
 * @param timestamp 时间戳
 * @param modifier 修饰符
 */
@Composable
fun DateDivider(
    timestamp: Instant,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // 日期标签
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = formatDate(timestamp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 格式化日期
 * 
 * @param timestamp 时间戳
 * @return 格式化后的日期字符串
 */
private fun formatDate(timestamp: Instant): String {
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
    
    // 判断是否在本年
    val isThisYear = now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)
    
    return when {
        isToday -> {
            // 今天
            "今天"
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
        isThisYear -> {
            // 本年：显示月日
            SimpleDateFormat("M月d日", Locale.getDefault())
                .format(Date.from(timestamp.toJavaInstant()))
        }
        else -> {
            // 更早：显示完整日期
            SimpleDateFormat("yyyy年M月d日", Locale.getDefault())
                .format(Date.from(timestamp.toJavaInstant()))
        }
    }
}

/**
 * 判断两个时间戳是否在同一天
 * 
 * @param timestamp1 时间戳1
 * @param timestamp2 时间戳2
 * @return 是否在同一天
 */
fun isSameDay(timestamp1: Instant, timestamp2: Instant): Boolean {
    val cal1 = Calendar.getInstance().apply {
        time = Date.from(timestamp1.toJavaInstant())
    }
    val cal2 = Calendar.getInstance().apply {
        time = Date.from(timestamp2.toJavaInstant())
    }
    
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
