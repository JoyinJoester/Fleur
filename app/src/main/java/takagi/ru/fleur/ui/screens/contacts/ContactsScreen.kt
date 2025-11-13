package takagi.ru.fleur.ui.screens.contacts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import takagi.ru.fleur.ui.screens.contacts.components.AddContactFAB
import takagi.ru.fleur.ui.screens.contacts.components.ContactDetailBottomSheet
import takagi.ru.fleur.ui.screens.contacts.components.ContactsEmptyState
import takagi.ru.fleur.ui.screens.contacts.components.ContactsList
import takagi.ru.fleur.ui.screens.contacts.components.ContactsLoadingState
import takagi.ru.fleur.ui.screens.contacts.components.ContactsSearchBar

/**
 * 联系人页面
 * 显示联系人列表，支持搜索、查看详情和快速操作
 * 
 * @param navController 导航控制器
 * @param onMenuClick 菜单按钮点击回调
 * @param viewModel ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun ContactsScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // 搜索防抖（300ms）
    LaunchedEffect(Unit) {
        snapshotFlow { uiState.searchQuery }
            .debounce(300)
            .collectLatest { query ->
                if (query.isNotBlank()) {
                    viewModel.searchContacts(query)
                }
            }
    }
    
    // 显示错误消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            val result = snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "重试",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.loadContacts(refresh = true)
            }
            viewModel.clearError()
        }
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("联系人") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "菜单"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (!uiState.isSearchActive && uiState.contacts.isNotEmpty()) {
                AddContactFAB(
                    onClick = {
                        // TODO: 导航到添加联系人页面
                    },
                    listState = listState
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
                // 加载状态
                uiState.isLoading -> {
                    ContactsLoadingState()
                }
                
                // 空状态
                uiState.contacts.isEmpty() && !uiState.isLoading -> {
                    ContactsEmptyState(
                        onAddContactClick = {
                            // TODO: 导航到添加联系人页面
                        }
                    )
                }
                
                // 正常状态
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ContactsList(
                            contacts = uiState.contacts,
                            onContactClick = { contact ->
                                viewModel.showContactDetail(contact)
                            },
                            onChatClick = { contact ->
                                viewModel.navigateToChat(contact)?.let { conversationId ->
                                    navController.navigate("chat_detail/$conversationId")
                                }
                            },
                            onEmailClick = { contact ->
                                val email = viewModel.navigateToCompose(contact)
                                navController.navigate("compose?to=$email")
                            }
                        )
                        
                        // 搜索栏（覆盖在列表上方）
                        ContactsSearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = { query ->
                                viewModel.searchContacts(query)
                            },
                            active = uiState.isSearchActive,
                            onActiveChange = { active ->
                                viewModel.setSearchActive(active)
                            },
                            searchResults = uiState.searchResults,
                            onContactClick = { contact ->
                                viewModel.showContactDetail(contact)
                            },
                            onChatClick = { contact ->
                                viewModel.navigateToChat(contact)?.let { conversationId ->
                                    navController.navigate("chat_detail/$conversationId")
                                }
                            },
                            onEmailClick = { contact ->
                                val email = viewModel.navigateToCompose(contact)
                                navController.navigate("compose?to=$email")
                            },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            
            // 联系人详情底部面板
            if (uiState.showDetailSheet && uiState.selectedContact != null) {
                ContactDetailBottomSheet(
                    contact = uiState.selectedContact!!,
                    onDismiss = { viewModel.hideContactDetail() },
                    onChatClick = {
                        viewModel.navigateToChat(uiState.selectedContact!!)?.let { conversationId ->
                            navController.navigate("chat_detail/$conversationId")
                        }
                        viewModel.hideContactDetail()
                    },
                    onEmailClick = {
                        val email = viewModel.navigateToCompose(uiState.selectedContact!!)
                        navController.navigate("compose?to=$email")
                        viewModel.hideContactDetail()
                    },
                    onEditClick = {
                        // TODO: 导航到编辑联系人页面
                        viewModel.hideContactDetail()
                    },
                    onDeleteClick = {
                        // TODO: 实现删除联系人功能
                        viewModel.hideContactDetail()
                    }
                )
            }
        }
    }
}
