package takagi.ru.fleur.ui.screens.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.ui.components.MessageBubble
import takagi.ru.fleur.ui.model.toUiModel

/**
 * 邮件聊天视图
 * 按线程分组显示邮件，类似聊天界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailChatView(
    emails: List<Email>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onEmailClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    
    // 按线程分组邮件
    val emailsByThread = remember(emails) {
        emails.groupBy { it.threadId }
            .map { (threadId, threadEmails) ->
                threadId to threadEmails.sortedBy { it.timestamp }
            }
            .sortedByDescending { (_, threadEmails) ->
                threadEmails.lastOrNull()?.timestamp
            }
    }
    
    // 监听滚动位置，实现分页加载
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= emailsByThread.size - 3
        }
    }
    
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }
    
    // 处理下拉刷新
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }
    
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            onRefresh()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            reverseLayout = false // 最新的在底部
        ) {
            emailsByThread.forEach { (threadId, threadEmails) ->
                item(key = "thread_$threadId") {
                    EmailThreadItem(
                        emails = threadEmails,
                        onEmailClick = onEmailClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
            
            // 加载更多指示器
            if (emailsByThread.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/**
 * 邮件线程项
 * 显示一个线程中的所有邮件
 */
@Composable
private fun EmailThreadItem(
    emails: List<Email>,
    onEmailClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 线程主题
        if (emails.isNotEmpty()) {
            Text(
                text = emails.first().subject,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }
        
        // 显示邮件
        val displayEmails = if (expanded) emails else emails.takeLast(2)
        
        displayEmails.forEach { email ->
            MessageBubble(
                email = email.toUiModel(),
                isSent = false, // 简化处理，实际应根据发件人判断
                showAvatar = true,
                onClick = { onEmailClick(email.id) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 展开/收起按钮
        if (emails.size > 2 && !expanded) {
            Text(
                text = "显示更多 (${emails.size - 2} 封)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(8.dp)
            )
        }
    }
}
