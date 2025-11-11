package takagi.ru.fleur.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.ui.model.EmailUiModel
import takagi.ru.fleur.ui.components.EmailListItem

/**
 * 搜索界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmailDetail: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 显示错误消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error.getUserMessage())
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            SearchTopBar(
                query = uiState.query,
                onQueryChange = { viewModel.updateQuery(it) },
                onNavigateBack = onNavigateBack,
                onSubmit = { viewModel.submitSearch() },
                onToggleFilters = { viewModel.toggleFilters() },
                hasActiveFilters = uiState.hasActiveFilters(),
                activeFilterCount = uiState.activeFilterCount()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 过滤器面板
            AnimatedVisibility(visible = uiState.showFilters) {
                FilterPanel(
                    filters = uiState.filters,
                    onUpdateAttachmentFilter = { viewModel.updateAttachmentFilter(it) },
                    onUpdateUnreadFilter = { viewModel.updateUnreadFilter(it) },
                    onUpdateStarredFilter = { viewModel.updateStarredFilter(it) },
                    onClearFilters = { viewModel.clearFilters() }
                )
            }
            
            when {
                // 显示搜索历史
                uiState.query.isBlank() && uiState.searchHistory.isNotEmpty() -> {
                    SearchHistoryView(
                        history = uiState.searchHistory,
                        onSelectHistory = { viewModel.selectFromHistory(it) },
                        onRemoveHistory = { viewModel.removeFromHistory(it) },
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }
                
                // 正在搜索
                uiState.isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                // 显示搜索结果
                uiState.searchResults.isNotEmpty() -> {
                    SearchResultsList(
                        results = uiState.searchResults,
                        query = uiState.query,
                        onEmailClick = onNavigateToEmailDetail
                    )
                }
                
                // 空状态
                uiState.query.isNotBlank() && !uiState.isSearching -> {
                    EmptySearchResults()
                }
                
                // 默认状态
                else -> {
                    SearchPlaceholder()
                }
            }
        }
    }
}

/**
 * 搜索顶部栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onSubmit: () -> Unit,
    onToggleFilters: () -> Unit,
    hasActiveFilters: Boolean,
    activeFilterCount: Int
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { onSubmit() },
        active = true,
        onActiveChange = { },
        placeholder = { Text("搜索邮件...") },
        leadingIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, "返回")
            }
        },
        trailingIcon = {
            Row {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, "清除")
                    }
                }
                
                BadgedBox(
                    badge = {
                        if (hasActiveFilters) {
                            Badge { Text(activeFilterCount.toString()) }
                        }
                    }
                ) {
                    IconButton(onClick = onToggleFilters) {
                        Icon(
                            imageVector = if (hasActiveFilters) Icons.Default.FilterAlt else Icons.Default.FilterList,
                            contentDescription = "过滤器"
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        // SearchBar 内容为空，因为我们在外部处理
    }
}

/**
 * 过滤器面板
 */
@Composable
private fun FilterPanel(
    filters: takagi.ru.fleur.domain.model.SearchFilters,
    onUpdateAttachmentFilter: (Boolean?) -> Unit,
    onUpdateUnreadFilter: (Boolean?) -> Unit,
    onUpdateStarredFilter: (Boolean?) -> Unit,
    onClearFilters: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "过滤器",
                    style = MaterialTheme.typography.titleMedium
                )
                
                TextButton(onClick = onClearFilters) {
                    Text("清除全部")
                }
            }
            
            // 过滤器选项
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filters.hasAttachment == true,
                        onClick = {
                            onUpdateAttachmentFilter(
                                if (filters.hasAttachment == true) null else true
                            )
                        },
                        label = { Text("有附件") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Attachment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                
                item {
                    FilterChip(
                        selected = filters.isUnread == true,
                        onClick = {
                            onUpdateUnreadFilter(
                                if (filters.isUnread == true) null else true
                            )
                        },
                        label = { Text("未读") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.MarkEmailUnread,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                
                item {
                    FilterChip(
                        selected = filters.isStarred == true,
                        onClick = {
                            onUpdateStarredFilter(
                                if (filters.isStarred == true) null else true
                            )
                        },
                        label = { Text("星标") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * 搜索历史视图
 */
@Composable
private fun SearchHistoryView(
    history: List<String>,
    onSelectHistory: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "搜索历史",
                style = MaterialTheme.typography.titleMedium
            )
            
            if (history.isNotEmpty()) {
                TextButton(onClick = onClearHistory) {
                    Text("清除")
                }
            }
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(history) { query ->
                SearchHistoryItem(
                    query = query,
                    onSelect = { onSelectHistory(query) },
                    onRemove = { onRemoveHistory(query) }
                )
            }
        }
    }
}

/**
 * 搜索历史项
 */
@Composable
private fun SearchHistoryItem(
    query: String,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = query,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 搜索结果列表
 */
@Composable
private fun SearchResultsList(
    results: List<Email>,
    query: String,
    onEmailClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Text(
                text = "找到 ${results.size} 封邮件",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        items(results) { email ->
            EmailListItem(
                email = email,
                onClick = { onEmailClick(email.id) },
                onLongPress = { },
                isSelected = false,
                highlightQuery = query
            )
        }
    }
}

/**
 * 空搜索结果
 */
@Composable
private fun EmptySearchResults() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "未找到匹配的邮件",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 搜索占位符
 */
@Composable
private fun SearchPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "搜索邮件",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 为 EmailListItem 添加高亮支持的扩展
 */
@Composable
private fun EmailListItem(
    email: Email,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    isSelected: Boolean,
    highlightQuery: String
) {
    // 直接使用现有的 EmailListItem 组件
    takagi.ru.fleur.ui.components.EmailListItem(
        email = email,
        onItemClick = onClick,
        onItemLongClick = onLongPress,
        isSelected = isSelected
    )
}

