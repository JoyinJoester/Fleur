package takagi.ru.fleur.ui.screens.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 搜索栏组件
 * 
 * 用于在对话中搜索消息
 * 支持：
 * - 搜索输入
 * - 搜索结果计数
 * - 上一个/下一个导航
 * - 展开/收起动画
 * 
 * @param visible 是否可见
 * @param searchQuery 搜索查询
 * @param onSearchQueryChange 搜索查询变化回调
 * @param currentResultIndex 当前结果索引（从 0 开始）
 * @param totalResults 总结果数
 * @param onPreviousResult 上一个结果回调
 * @param onNextResult 下一个结果回调
 * @param onClose 关闭回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    visible: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    currentResultIndex: Int,
    totalResults: Int,
    onPreviousResult: () -> Unit,
    onNextResult: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    
    // 当搜索栏可见时，自动聚焦输入框
    LaunchedEffect(visible) {
        if (visible) {
            focusRequester.requestFocus()
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 返回按钮
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(48.dp) // 符合最小触摸目标 48dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "关闭搜索"
                        )
                    }
                    
                    // 搜索输入框
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text("搜索消息...")
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "清除"
                                    )
                                }
                            }
                        }
                    )
                    
                    // 搜索结果计数和导航
                    if (totalResults > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 结果计数
                            Text(
                                text = "${currentResultIndex + 1}/$totalResults",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // 上一个按钮
                            IconButton(
                                onClick = onPreviousResult,
                                enabled = totalResults > 1,
                                modifier = Modifier.size(48.dp) // 符合最小触摸目标 48dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "上一个",
                                    tint = if (totalResults > 1) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    }
                                )
                            }
                            
                            // 下一个按钮
                            IconButton(
                                onClick = onNextResult,
                                enabled = totalResults > 1,
                                modifier = Modifier.size(48.dp) // 符合最小触摸目标 48dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "下一个",
                                    tint = if (totalResults > 1) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    }
                                )
                            }
                        }
                    }
                }
                
                HorizontalDivider()
            }
        }
    }
}
