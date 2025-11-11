package takagi.ru.fleur.ui.screens.folder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 垃圾箱页面
 * 显示用户删除的邮件列表
 * 
 * @param onNavigateBack 返回回调
 * @param onNavigateToEmailDetail 导航到邮件详情回调
 * @param viewModel ViewModel
 * @param modifier 修饰符
 */
@Composable
fun TrashScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmailDetail: (String) -> Unit,
    viewModel: FolderViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // 初始化文件夹类型
    LaunchedEffect(Unit) {
        viewModel.initialize(FolderType.TRASH)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // 配置垃圾箱页面
    val config = remember {
        FolderConfig(
            folderType = FolderType.TRASH,
            title = "垃圾箱",
            emptyStateConfig = EmptyStateConfig(
                icon = Icons.Default.Delete,
                title = "垃圾箱为空",
                description = "删除的邮件将显示在这里"
            ),
            swipeActions = SwipeActionsConfig(
                leftSwipe = null,
                rightSwipe = SwipeAction(
                    icon = Icons.Default.RestoreFromTrash,
                    backgroundColor = Color(0xFF66BB6A), // 绿色
                    action = EmailAction.RESTORE
                )
            ),
            showFab = false,
            topBarActions = listOf(
                TopBarAction(
                    icon = Icons.Default.DeleteForever,
                    contentDescription = "清空垃圾箱",
                    onClick = {
                        // TODO: 显示确认对话框后清空垃圾箱
                        // 这里需要在 ViewModel 中添加 emptyTrash() 方法
                    }
                )
            )
        )
    }
    
    FolderScreenTemplate(
        config = config,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToEmailDetail = onNavigateToEmailDetail,
        onNavigateToCompose = {},
        onRefresh = { viewModel.refreshEmails() },
        onLoadMore = { viewModel.loadNextPage() },
        onEmailAction = { emailId, action ->
            viewModel.performAction(emailId, action)
        },
        onBatchAction = { emailIds, action ->
            viewModel.performBatchAction(emailIds, action)
        },
        onEnterMultiSelect = { emailId ->
            viewModel.enterMultiSelectMode(emailId)
        },
        onExitMultiSelect = {
            viewModel.exitMultiSelectMode()
        },
        onToggleSelection = { emailId ->
            viewModel.toggleEmailSelection(emailId)
        },
        onDismissError = {
            viewModel.dismissError()
        },
        modifier = modifier
    )
}
