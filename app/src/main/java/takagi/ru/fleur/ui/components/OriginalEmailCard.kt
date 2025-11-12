package takagi.ru.fleur.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import takagi.ru.fleur.domain.model.Email

/**
 * 原始邮件卡片组件
 * 
 * 用于在回复/转发界面中显示原始邮件的内容
 * 支持展开/折叠功能以节省屏幕空间
 * 
 * @param email 原始邮件对象
 * @param isExpanded 是否展开状态
 * @param onToggleExpanded 切换展开/折叠的回调
 * @param modifier 修饰符
 */
@Composable
fun OriginalEmailCard(
    email: Email,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    SimpleFleurCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 邮件头部信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "原始邮件",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = email.from.formatted(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatTimestamp(email.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        imageVector = if (isExpanded) 
                            Icons.Default.ExpandLess 
                        else 
                            Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "折叠" else "展开"
                    )
                }
            }
            
            // 可展开的邮件内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // 收件人信息
                    Text(
                        text = "收件人: ${email.to.joinToString(", ") { it.formatted() }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (email.cc.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "抄送: ${email.cc.joinToString(", ") { it.formatted() }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // 主题
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "主题: ${email.subject}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // 邮件正文
                    Text(
                        text = getEmailContent(email),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                    
                    // 附件信息
                    if (email.attachments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "附件 (${email.attachments.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        email.attachments.forEach { attachment ->
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${attachment.fileName} (${attachment.formattedSize()})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 获取邮件内容
 * 
 * 优先级：纯文本 > Markdown > HTML（去除标签）> 预览文本
 * 
 * @param email 邮件对象
 * @return 邮件内容文本
 */
private fun getEmailContent(email: Email): String {
    return when {
        email.bodyPlain.isNotBlank() -> email.bodyPlain
        email.bodyMarkdown != null && email.bodyMarkdown.isNotBlank() -> email.bodyMarkdown
        email.bodyHtml != null && email.bodyHtml.isNotBlank() -> stripHtml(email.bodyHtml)
        else -> email.bodyPreview
    }
}

/**
 * 格式化时间戳为可读格式
 * 
 * 格式：yyyy-MM-dd HH:mm:ss
 * 
 * @param timestamp 时间戳
 * @return 格式化后的时间字符串
 */
private fun formatTimestamp(timestamp: Instant): String {
    val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    return String.format(
        "%04d-%02d-%02d %02d:%02d:%02d",
        localDateTime.year,
        localDateTime.monthNumber,
        localDateTime.dayOfMonth,
        localDateTime.hour,
        localDateTime.minute,
        localDateTime.second
    )
}

/**
 * 移除 HTML 标签，转换为纯文本
 * 
 * 这是一个简化的实现，处理常见的 HTML 实体和标签
 * 
 * @param html HTML 内容
 * @return 纯文本内容
 */
private fun stripHtml(html: String): String {
    return html
        // 移除所有 HTML 标签
        .replace(Regex("<[^>]*>"), "")
        // 替换常见的 HTML 实体
        .replace("&nbsp;", " ")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        // 移除多余的空白
        .replace(Regex("\\s+"), " ")
        .trim()
}
