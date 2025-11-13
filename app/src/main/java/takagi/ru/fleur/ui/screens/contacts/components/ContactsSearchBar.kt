package takagi.ru.fleur.ui.screens.contacts.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import takagi.ru.fleur.ui.model.ContactUiModel

/**
 * 联系人搜索栏组件
 * 使用 Material 3 SearchBar，支持展开/收起和搜索结果显示
 * 
 * @param query 搜索关键词
 * @param onQueryChange 搜索关键词变化回调
 * @param active 是否激活（展开）
 * @param onActiveChange 激活状态变化回调
 * @param searchResults 搜索结果列表
 * @param onContactClick 联系人点击回调
 * @param onChatClick 聊天按钮点击回调
 * @param onEmailClick 邮件按钮点击回调
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    searchResults: List<ContactUiModel>,
    onContactClick: (ContactUiModel) -> Unit,
    onChatClick: (ContactUiModel) -> Unit,
    onEmailClick: (ContactUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { /* 不需要额外处理 */ },
        active = active,
        onActiveChange = onActiveChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text("搜索联系人")
        },
        leadingIcon = {
            if (active) {
                IconButton(onClick = { onActiveChange(false) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索"
                )
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清除"
                    )
                }
            }
        }
    ) {
        // 搜索结果列表
        AnimatedVisibility(
            visible = searchResults.isNotEmpty(),
            enter = fadeIn(
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ) + expandVertically(
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ) + shrinkVertically(
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            )
        ) {
            LazyColumn {
                items(
                    items = searchResults,
                    key = { contact -> contact.id }
                ) { contact ->
                    ContactItem(
                        contact = contact,
                        onClick = {
                            onContactClick(contact)
                            onActiveChange(false)
                        },
                        onChatClick = {
                            onChatClick(contact)
                            onActiveChange(false)
                        },
                        onEmailClick = {
                            onEmailClick(contact)
                            onActiveChange(false)
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 88.dp)
                    )
                }
            }
        }
        
        // 空状态提示
        if (query.isNotEmpty() && searchResults.isEmpty()) {
            Text(
                text = "未找到匹配的联系人",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
