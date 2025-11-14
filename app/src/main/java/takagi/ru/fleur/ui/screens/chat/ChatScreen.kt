package takagi.ru.fleur.ui.screens.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.fleur.ui.screens.chat.components.SwipeableConversationItem
import takagi.ru.fleur.ui.screens.chat.model.ConversationAction
import takagi.ru.fleur.ui.screens.chat.model.ConversationSwipeAction

/**
 * Chat 页面
 * 
 * 显示对话列表，支持：
 * - 下拉刷新
 * - 滚动加载更多
 * - 搜索
 * - 点击进入对话详情
 * 
 * @param onNavigateToDetail 导航到对话详情的回调
 * @param onNavigateToSearch 导航到搜索页面的回调
 * @param viewModel ViewModel
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ChatScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onMenuClick: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }

    BackHandler(enabled = uiState.isMultiSelectMode) {
        viewModel.clearSelection()
    }
    
    // 监听滚动位置，自动加载更多
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= uiState.conversations.size - 3 &&
                    !uiState.isLoadingMore &&
                    uiState.hasMore) {
                    viewModel.loadMore()
                }
            }
    }
    
    // 显示错误信息
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "重试",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (uiState.isMultiSelectMode) {
                ChatSelectionTopBar(
                    selectedCount = uiState.selectedConversationIds.size,
                    onClose = { viewModel.clearSelection() },
                    onDelete = { viewModel.deleteSelectedConversations() },
                    onMarkRead = { viewModel.markSelectedConversationsAsRead() },
                    onMarkUnread = { viewModel.markSelectedConversationsAsUnread() }
                )
            } else {
                ChatTopAppBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onMenuClick = onMenuClick,
                    onAvatarClick = { /* TODO: handle avatar click */ },
                    scrollBehavior = scrollBehavior
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // 初始加载状态
                uiState.isLoading && uiState.conversations.isEmpty() -> {
                    LoadingState()
                }
                
                // 空状态
                uiState.conversations.isEmpty() && !uiState.isLoading -> {
                    EmptyState()
                }
                
                // 对话列表
                else -> {
                    ConversationList(
                        conversations = uiState.conversations,
                        isRefreshing = uiState.isRefreshing,
                        isLoadingMore = uiState.isLoadingMore,
                        onRefresh = { viewModel.refresh() },
                        onConversationClick = { conversation ->
                            if (uiState.isMultiSelectMode) {
                                viewModel.toggleConversationSelection(conversation.id)
                            } else {
                                if (conversation.unreadCount > 0) {
                                    viewModel.markConversationAsRead(conversation.id)
                                }
                                onNavigateToDetail(conversation.id)
                            }
                        },
                        onConversationLongPress = { conversationId ->
                            viewModel.enterSelectionMode(conversationId)
                        },
                        onConversationSwipe = { conversationId, action ->
                            viewModel.handleSwipeAction(action, conversationId)
                        },
                        listState = listState,
                        selectedConversationIds = uiState.selectedConversationIds,
                        isMultiSelectMode = uiState.isMultiSelectMode
                    )
                }
            }
        }
    }
}

/**
 * 对话列表组件
 * 
 * @param conversations 对话列表
 * @param isRefreshing 是否正在刷新
 * @param isLoadingMore 是否正在加载更多
 * @param onRefresh 刷新回调
 * @param onConversationClick 对话点击回调
 * @param listState 列表状态
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ConversationList(
    conversations: List<takagi.ru.fleur.ui.model.ConversationUiModel>,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    onRefresh: () -> Unit,
    onConversationClick: (takagi.ru.fleur.ui.model.ConversationUiModel) -> Unit,
    onConversationLongPress: (String) -> Unit,
    onConversationSwipe: (String, ConversationAction) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    selectedConversationIds: Set<String>,
    isMultiSelectMode: Boolean
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        // 对话列表项
        items(
            items = conversations,
            key = { it.id }
        ) { conversation ->
            val isSelected = selectedConversationIds.contains(conversation.id)
            val leftSwipeAction = if (conversation.unreadCount > 0) {
                ConversationSwipeAction(
                    icon = Icons.Default.MarkEmailRead,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    action = ConversationAction.MarkRead
                )
            } else {
                ConversationSwipeAction(
                    icon = Icons.Default.MarkEmailUnread,
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    action = ConversationAction.MarkUnread
                )
            }

            val rightSwipeAction = ConversationSwipeAction(
                icon = Icons.Default.Delete,
                backgroundColor = MaterialTheme.colorScheme.error,
                action = ConversationAction.Delete
            )

            SwipeableConversationItem(
                conversation = conversation,
                isSelected = isSelected,
                isMultiSelectMode = isMultiSelectMode,
                leftSwipeAction = leftSwipeAction,
                rightSwipeAction = rightSwipeAction,
                onClick = { onConversationClick(conversation) },
                onLongPress = { onConversationLongPress(conversation.id) },
                onSwipeAction = { action -> onConversationSwipe(conversation.id, action) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 加载更多指示器
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )
    }
}

/**
 * 加载状态组件
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 空状态组件
 */
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Chat,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "暂无对话",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "开始发送邮件来创建对话",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onAvatarClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("搜索对话") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "对",
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
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatSelectionTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onMarkRead: () -> Unit,
    onMarkUnread: () -> Unit
) {
    TopAppBar(
        title = { Text("已选择 $selectedCount 项") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "退出多选"
                )
            }
        },
        actions = {
            IconButton(onClick = onMarkRead, enabled = selectedCount > 0) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = "标记已读"
                )
            }
            IconButton(onClick = onMarkUnread, enabled = selectedCount > 0) {
                Icon(
                    imageVector = Icons.Default.MarkEmailUnread,
                    contentDescription = "标记未读"
                )
            }
            IconButton(onClick = onDelete, enabled = selectedCount > 0) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除对话"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}
