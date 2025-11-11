package takagi.ru.fleur.ui.screens.folder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
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
 * 已发送页面
 * 显示用户已发送的邮件列表
 * 
 * @param onNavigateBack 返回回调
 * @param onNavigateToEmailDetail 导航到邮件详情回调
 * @param viewModel ViewModel
 * @param modifier 修饰符
 */
@Composable
fun SentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmailDetail: (String) -> Unit,
    viewModel: FolderViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // 初始化文件夹类型
    LaunchedEffect(Unit) {
        viewModel.initialize(FolderType.SENT)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // 配置已发送页面
    val config = remember {
        FolderConfig(
            folderType = FolderType.SENT,
            title = "已发送",
            emptyStateConfig = EmptyStateConfig(
                icon = Icons.Default.Send,
                title = "暂无已发送邮件",
                description = "您发送的邮件将显示在这里"
            ),
            swipeActions = SwipeActionsConfig(
                leftSwipe = SwipeAction(
                    icon = Icons.Default.Delete,
                    backgroundColor = Color(0xFFEF5350), // 红色
                    action = EmailAction.DELETE
                ),
                rightSwipe = null
            ),
            showFab = false
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
