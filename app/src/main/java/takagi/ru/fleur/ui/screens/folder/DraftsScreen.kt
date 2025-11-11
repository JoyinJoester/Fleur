package takagi.ru.fleur.ui.screens.folder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
 * 草稿箱页面
 * 显示用户保存的草稿邮件列表
 * 
 * @param onNavigateBack 返回回调
 * @param onNavigateToEmailDetail 导航到邮件详情回调（草稿会导航到撰写页面）
 * @param onNavigateToCompose 导航到撰写页面回调
 * @param viewModel ViewModel
 * @param modifier 修饰符
 */
@Composable
fun DraftsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmailDetail: (String) -> Unit,
    onNavigateToCompose: () -> Unit,
    viewModel: FolderViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // 初始化文件夹类型
    LaunchedEffect(Unit) {
        viewModel.initialize(FolderType.DRAFTS)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // 配置草稿箱页面
    val config = remember {
        FolderConfig(
            folderType = FolderType.DRAFTS,
            title = "草稿箱",
            emptyStateConfig = EmptyStateConfig(
                icon = Icons.Default.Edit,
                title = "暂无草稿",
                description = "您保存的草稿将显示在这里",
                actionButton = ActionButton(
                    text = "撰写邮件",
                    onClick = onNavigateToCompose
                )
            ),
            swipeActions = SwipeActionsConfig(
                leftSwipe = SwipeAction(
                    icon = Icons.Default.Delete,
                    backgroundColor = Color(0xFFEF5350), // 红色
                    action = EmailAction.DELETE
                ),
                rightSwipe = null
            ),
            showFab = true,
            fabIcon = Icons.Default.Edit,
            fabAction = onNavigateToCompose
        )
    }
    
    FolderScreenTemplate(
        config = config,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToEmailDetail = onNavigateToEmailDetail, // 点击草稿会导航到撰写页面
        onNavigateToCompose = onNavigateToCompose,
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
