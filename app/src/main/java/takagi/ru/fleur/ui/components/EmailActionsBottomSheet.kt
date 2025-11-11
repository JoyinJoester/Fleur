package takagi.ru.fleur.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.ui.theme.bottomSheetGlassmorphism

/**
 * 邮件操作 Bottom Sheet
 * 
 * 提供邮件的快捷操作菜单：
 * - 回复
 * - 转发
 * - 归档
 * - 标记星标
 * - 删除
 * 
 * 特性:
 * - 毛玻璃效果：12dp blur + 90% opacity
 * - 滑入动画：300ms
 * - 支持手势拖拽关闭
 * 
 * @param onDismiss 关闭回调
 * @param onReply 回复回调
 * @param onForward 转发回调
 * @param onArchive 归档回调
 * @param onToggleStar 切换星标回调
 * @param onDelete 删除回调
 * @param isStarred 是否已标记星标
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailActionsBottomSheet(
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onArchive: () -> Unit,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit,
    isStarred: Boolean = false,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.bottomSheetGlassmorphism(),
        containerColor = Color.Transparent,
        dragHandle = {
            // 自定义拖拽手柄
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ) {}
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // 标题
            Text(
                text = "邮件操作",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 操作项
            BottomSheetActionItem(
                icon = Icons.Default.Reply,
                label = "回复",
                onClick = {
                    onReply()
                    onDismiss()
                }
            )
            
            BottomSheetActionItem(
                icon = Icons.Default.Forward,
                label = "转发",
                onClick = {
                    onForward()
                    onDismiss()
                }
            )
            
            BottomSheetActionItem(
                icon = Icons.Default.Archive,
                label = "归档",
                onClick = {
                    onArchive()
                    onDismiss()
                }
            )
            
            BottomSheetActionItem(
                icon = if (isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                label = if (isStarred) "取消星标" else "标记星标",
                onClick = {
                    onToggleStar()
                    onDismiss()
                }
            )
            
            BottomSheetActionItem(
                icon = Icons.Default.Delete,
                label = "删除",
                iconTint = MaterialTheme.colorScheme.error,
                onClick = {
                    onDelete()
                    onDismiss()
                }
            )
        }
    }
}

/**
 * Bottom Sheet 操作项
 * 
 * @param icon 图标
 * @param label 标签
 * @param iconTint 图标颜色
 * @param onClick 点击回调
 */
@Composable
private fun BottomSheetActionItem(
    icon: ImageVector,
    label: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (iconTint == MaterialTheme.colorScheme.error) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}
