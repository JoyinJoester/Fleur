package takagi.ru.fleur.ui.screens.folder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
 * 星标邮件页面
 * 显示用户标记为星标的重要邮件列表
 * 
 * @param onNavigateBack 返回回调
 * @param onNavigateToEmailDetail 导航到邮件详情回调
 * @param viewModel ViewModel
 * @param modifier 修饰符
 */
@Composable
fun StarredScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmailDetail: (String) -> Unit,
    viewModel: FolderViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // 初始化文件夹类型
    LaunchedEffect(Unit) {
        viewModel.initialize(FolderType.STARRED)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // 配置星标邮件页面
    val config = remember {
        FolderConfig(
            folderType = FolderType.STARRED,
            title = "星标邮件",
            emptyStateConfig = EmptyStateConfig(
                icon = Icons.Default.StarBorder,
                title = "暂无星标邮件",
                description = "为重要邮件添加星标，方便日后查找"
            ),
            swipeActions = SwipeActionsConfig(
                leftSwipe = SwipeAction(
                    icon = Icons.Default.StarBorder,
                    backgroundColor = Color(0xFFFFA726), // 橙色
                    action = EmailAction.UNSTAR
                ),
                rightSwipe = SwipeAction(
                    icon = Icons.Default.Archive,
                    backgroundColor = Color(0xFF66BB6A), // 绿色
                    action = EmailAction.ARCHIVE
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
