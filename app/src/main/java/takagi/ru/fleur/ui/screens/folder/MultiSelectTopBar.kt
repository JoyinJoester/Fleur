package takagi.ru.fleur.ui.screens.folder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 多选模式顶部应用栏
 * 显示已选邮件数量和批量操作按钮
 * 
 * @param selectedCount 已选邮件数量
 * @param onExitMultiSelect 退出多选模式回调
 * @param onDelete 删除回调
 * @param onArchive 归档回调
 * @param onMarkRead 标记已读回调
 * @param onMarkUnread 标记未读回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectTopBar(
    selectedCount: Int,
    onExitMultiSelect: () -> Unit,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onMarkRead: () -> Unit,
    onMarkUnread: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = { 
            Text("已选择 $selectedCount 项") 
        },
        navigationIcon = {
            IconButton(onClick = onExitMultiSelect) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "取消选择"
                )
            }
        },
        actions = {
            // 标记已读按钮
            IconButton(
                onClick = onMarkRead,
                enabled = selectedCount > 0
            ) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = "标记已读"
                )
            }
            
            // 标记未读按钮
            IconButton(
                onClick = onMarkUnread,
                enabled = selectedCount > 0
            ) {
                Icon(
                    imageVector = Icons.Default.MarkEmailUnread,
                    contentDescription = "标记未读"
                )
            }
            
            // 归档按钮
            IconButton(
                onClick = onArchive,
                enabled = selectedCount > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = "归档"
                )
            }
            
            // 删除按钮
            IconButton(
                onClick = onDelete,
                enabled = selectedCount > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}
