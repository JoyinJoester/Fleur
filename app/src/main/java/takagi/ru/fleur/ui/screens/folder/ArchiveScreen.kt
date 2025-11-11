package takagi.ru.fleur.ui.screens.folder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
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
 * 归档页面
 * 显示用户归档的邮件列表
 * 
 * @param onNavigateBack 返回回调
 * @param onNavigateToEmailDetail 导航到邮件详情回调
 * @param viewModel ViewModel
 * @param modifier 修饰符
 */
@Composable
fun ArchiveScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmailDetail: (String) -> Unit,
    viewModel: FolderViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // 初始化文件夹类型
    LaunchedEffect(Unit) {
        viewModel.initialize(FolderType.ARCHIVE)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // 配置归档页面
    val config = remember {
        FolderConfig(
            folderType = FolderType.ARCHIVE,
            title = "归档",
            emptyStateConfig = EmptyStateConfig(
                icon = Icons.Default.Archive,
                title = "暂无归档邮件",
                description = "归档的邮件将显示在这里"
            ),
            swipeActions = SwipeActionsConfig(
                leftSwipe = SwipeAction(
                    icon = Icons.Default.Delete,
                    backgroundColor = Color(0xFFEF5350), // 红色
                    action = EmailAction.DELETE
                ),
                rightSwipe = SwipeAction(
                    icon = Icons.Default.Unarchive,
                    backgroundColor = Color(0xFF42A5F5), // 蓝色
                    action = EmailAction.UNARCHIVE
                )
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
