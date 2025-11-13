package takagi.ru.fleur.ui.screens.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.ui.model.MessageUiModel

/**
 * 消息操作底部弹窗
 * 
 * 显示消息的操作选项：
 * - 复制文本
 * - 回复
 * - 转发
 * - 删除
 * 
 * @param visible 是否显示
 * @param message 要操作的消息
 * @param onDismiss 关闭回调
 * @param onCopy 复制回调
 * @param onReply 回复回调
 * @param onForward 转发回调
 * @param onDelete 删除回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionsBottomSheet(
    visible: Boolean,
    message: MessageUiModel?,
    onDismiss: () -> Unit,
    onCopy: (MessageUiModel) -> Unit,
    onReply: (MessageUiModel) -> Unit,
    onForward: (MessageUiModel) -> Unit,
    onDelete: (MessageUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible && message != null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // 标题
                Text(
                    text = "消息操作",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                
                // 复制
                ActionItem(
                    icon = Icons.Default.ContentCopy,
                    text = "复制",
                    onClick = {
                        onCopy(message)
                        onDismiss()
                    }
                )
                
                // 回复
                ActionItem(
                    icon = Icons.Default.Reply,
                    text = "回复",
                    onClick = {
                        onReply(message)
                        onDismiss()
                    }
                )
                
                // 转发
                ActionItem(
                    icon = Icons.Default.Forward,
                    text = "转发",
                    onClick = {
                        onForward(message)
                        onDismiss()
                    }
                )
                
                // 分隔线
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // 删除（危险操作，使用错误颜色）
                ActionItem(
                    icon = Icons.Default.Delete,
                    text = "删除",
                    textColor = MaterialTheme.colorScheme.error,
                    iconTint = MaterialTheme.colorScheme.error,
                    onClick = {
                        onDelete(message)
                        onDismiss()
                    }
                )
            }
        }
    }
}

/**
 * 操作项组件
 * 
 * @param icon 图标
 * @param text 文本
 * @param textColor 文本颜色
 * @param iconTint 图标颜色
 * @param onClick 点击回调
 */
@Composable
private fun ActionItem(
    icon: ImageVector,
    text: String,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}
