package takagi.ru.fleur.ui.screens.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.material.icons.filled.BugReport

/**
 * 收件箱主界面
 * 整合 ViewModel 和视图组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToCompose: () -> Unit,
    onNavigateToEmailDetail: (String) -> Unit,
    onNavigateToAccountManagement: () -> Unit,
    onMenuClick: () -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAccountFilter by remember { mutableStateOf(false) }
    var hasInsertedTestData by remember { mutableStateOf(false) }
    
    // M3E优化：创建滚动行为以实现TopAppBar的滚动阴影效果（0dp → 4dp）
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // 首次启动时插入测试数据（只执行一次）
    LaunchedEffect(hasInsertedTestData) {
        if (!hasInsertedTestData && uiState.emails.isEmpty() && !uiState.isLoading) {
            viewModel.insertTestData("default_account")
            hasInsertedTestData = true
        }
    }
    
    // 显示错误消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.getUserMessage()
            )
            viewModel.clearError()
        }
    }
    
    // 显示同步错误消息
    LaunchedEffect(uiState.syncError) {
        uiState.syncError?.let { error ->
            snackbarHostState.showSnackbar(
                message = "同步失败: ${error.getUserMessage()}"
            )
        }
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), // 连接滚动行为
        topBar = {
            if (uiState.isMultiSelectMode) {
                MultiSelectToolbar(
                    visible = true,
                    selectedCount = uiState.selectedEmailIds.size,
                    onClose = { viewModel.exitMultiSelectMode() },
                    onDelete = { viewModel.deleteSelectedEmails() },
                    onArchive = { viewModel.archiveSelectedEmails() },
                    onMarkAsRead = { viewModel.markSelectedAsRead(true) }
                )
            } else {
                InboxTopAppBar(
                    onMenuClick = onMenuClick,
                    onSearchClick = onNavigateToSearch,
                    onAccountClick = onNavigateToAccountManagement,
                    onFilterClick = { showAccountFilter = true },
                    scrollBehavior = scrollBehavior // 传递滚动行为以实现阴影效果
                )
            }
        },
        floatingActionButton = {
            // 撰写邮件按钮
            FloatingActionButton(
                onClick = onNavigateToCompose,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "撰写邮件"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 离线指示器
            if (uiState.isOffline) {
                takagi.ru.fleur.ui.components.OfflineIndicator(
                    isOffline = uiState.isOffline,
                    pendingOperationCount = uiState.pendingOperationCount,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // 同步状态指示器
            takagi.ru.fleur.ui.components.SyncStatusIndicator(
                isSyncing = uiState.isSyncing
            )
            
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.emails.isEmpty() -> {
                        // 初始加载状态 - M3E优化：显示骨架屏
                        androidx.compose.foundation.lazy.LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // 显示加载中文本
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                    uiState.emails.isEmpty() -> {
                        // 空状态 - M3E优化：友好的空状态设计
                        takagi.ru.fleur.ui.components.EmptyInboxState(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        // 显示邮件列表
                        ViewModeSwitcher(
                            viewMode = uiState.viewMode,
                            emails = uiState.emails,
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = { viewModel.refreshEmails() },
                            onLoadMore = { viewModel.loadNextPage() },
                            onEmailClick = { emailId ->
                                android.util.Log.d("InboxScreen", "onEmailClick 触发: $emailId, 多选模式: ${uiState.isMultiSelectMode}")
                                if (uiState.isMultiSelectMode) {
                                    viewModel.toggleEmailSelection(emailId)
                                } else {
                                    android.util.Log.d("InboxScreen", "导航到邮件详情: $emailId")
                                    onNavigateToEmailDetail(emailId)
                                }
                            },
                            onEmailLongPress = { emailId ->
                                viewModel.enterMultiSelectMode(emailId)
                            },
                            onArchive = { emailId -> viewModel.archiveEmail(emailId) },
                            onDelete = { emailId -> viewModel.deleteEmail(emailId) },
                            onMarkRead = { emailId -> viewModel.markAsRead(emailId) },
                            onMarkUnread = { emailId -> viewModel.markAsUnread(emailId) },
                            onStar = { emailId -> viewModel.toggleStar(emailId) },
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            selectedEmailIds = uiState.selectedEmailIds,
                            swipeRightAction = uiState.swipeRightAction,
                            swipeLeftAction = uiState.swipeLeftAction
                        )
                    }
                }
                
                // 账户过滤器下拉菜单
                if (showAccountFilter) {
                    AccountFilterMenu(
                        selectedAccountId = uiState.selectedAccountId,
                        onAccountSelected = { accountId ->
                            viewModel.filterByAccount(accountId)
                            showAccountFilter = false
                        },
                        onDismiss = { showAccountFilter = false }
                    )
                }
            }
        }
    }
}

/**
 * 收件箱顶部应用栏
 * Chrome 风格：菜单按钮 + 圆角搜索框 + 头像
 * M3E优化：48dp搜索框高度，滚动阴影效果
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxTopAppBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAccountClick: () -> Unit,
    onFilterClick: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 搜索框（占据大部分宽度，M3E优化：48dp高度）
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .size(height = 48.dp, width = 0.dp)
                ) {
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = {
                            Text("搜索邮件")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "搜索"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSearchClick() },
                        enabled = false, // 点击时跳转到搜索页面
                        shape = RoundedCornerShape(24.dp), // 完全圆角
                        colors = TextFieldDefaults.colors(
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 头像按钮（40dp圆形）
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onAccountClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "菜单"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        ),
        scrollBehavior = scrollBehavior, // 支持滚动阴影效果
        modifier = modifier
    )
}

/**
 * 账户过滤器菜单
 */
@Composable
private fun AccountFilterMenu(
    selectedAccountId: String?,
    onAccountSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text("所有账户") },
            onClick = { onAccountSelected(null) }
        )
        // TODO: 添加实际的账户列表
    }
}


/**
 * DEBUG 模式的浮动按钮组
 * 包含撰写邮件和插入测试数据两个按钮
 */
@Composable
private fun DebugFloatingActionButtons(
    onComposeClick: () -> Unit,
    onInsertTestData: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        // 插入测试数据按钮
        FloatingActionButton(
            onClick = {
                scope.launch {
                    try {
                        // 使用 ViewModel 的方法插入测试数据
                        val accountId = "test_account_001"
                        onInsertTestData(accountId)
                        Log.d("InboxScreen", "✅ 成功触发测试数据插入")
                    } catch (e: Exception) {
                        Log.e("InboxScreen", "❌ 插入测试数据失败", e)
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = "插入测试数据",
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.size(16.dp))
        
        // 撰写邮件按钮
        FloatingActionButton(
            onClick = onComposeClick,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "撰写邮件"
            )
        }
    }
}
