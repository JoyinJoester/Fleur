package takagi.ru.fleur.ui.screens.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import takagi.ru.fleur.domain.repository.AccountRepository
import takagi.ru.fleur.domain.usecase.GetConversationsUseCase
import javax.inject.Inject

/**
 * Chat 页面 ViewModel
 * 
 * 管理对话列表的状态和业务逻辑
 * 支持分页加载和下拉刷新
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val accountRepository: AccountRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val TAG = "ChatViewModel"
        private const val PAGE_SIZE = 20 // 每页加载 20 个对话
    }
    
    init {
        // 初始化时加载对话列表
        loadConversations()
    }
    
    /**
     * 加载对话列表
     * 
     * 首次加载或刷新时调用
     * 会重置分页状态并从第一页开始加载
     */
    fun loadConversations() {
        viewModelScope.launch {
            try {
                // 设置加载状态
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // 获取当前账户ID
                val accountId = getCurrentAccountId()
                
                // 加载第一页数据
                getConversationsUseCase(
                    accountId = accountId,
                    page = 0,
                    pageSize = PAGE_SIZE
                )
                .catch { exception ->
                    Log.e(TAG, "加载对话列表失败", exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "加载失败"
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { conversations ->
                            Log.d(TAG, "加载对话列表成功: ${conversations.size} 个对话")
                            _uiState.update {
                                it.copy(
                                    conversations = conversations,
                                    isLoading = false,
                                    error = null,
                                    currentPage = 0,
                                    hasMore = conversations.size >= PAGE_SIZE
                                )
                            }
                        },
                        onFailure = { exception ->
                            Log.e(TAG, "加载对话列表失败", exception)
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = exception.message ?: "加载失败"
                                )
                            }
                        }
                    )
                }
            } catch (e: CancellationException) {
                // 协程被取消是正常行为,不需要记录错误
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "加载对话列表异常", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }
    
    /**
     * 刷新对话列表
     * 
     * 下拉刷新时调用
     * 会重置分页状态并从第一页开始加载
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                // 设置刷新状态
                _uiState.update { it.copy(isRefreshing = true, error = null) }
                
                // 获取当前账户ID
                val accountId = getCurrentAccountId()
                
                // 加载第一页数据
                getConversationsUseCase(
                    accountId = accountId,
                    page = 0,
                    pageSize = PAGE_SIZE
                )
                .catch { exception ->
                    Log.e(TAG, "刷新对话列表失败", exception)
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = exception.message ?: "刷新失败"
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { conversations ->
                            Log.d(TAG, "刷新对话列表成功: ${conversations.size} 个对话")
                            _uiState.update {
                                it.copy(
                                    conversations = conversations,
                                    isRefreshing = false,
                                    error = null,
                                    currentPage = 0,
                                    hasMore = conversations.size >= PAGE_SIZE
                                )
                            }
                        },
                        onFailure = { exception ->
                            Log.e(TAG, "刷新对话列表失败", exception)
                            _uiState.update {
                                it.copy(
                                    isRefreshing = false,
                                    error = exception.message ?: "刷新失败"
                                )
                            }
                        }
                    )
                }
            } catch (e: CancellationException) {
                // 协程被取消是正常行为,不需要记录错误
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "刷新对话列表异常", e)
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = e.message ?: "刷新失败"
                    )
                }
            }
        }
    }
    
    /**
     * 加载更多对话
     * 
     * 滚动到底部时调用
     * 加载下一页数据并追加到现有列表
     */
    fun loadMore() {
        // 如果正在加载或没有更多数据，则不执行
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) {
            return
        }
        
        viewModelScope.launch {
            try {
                // 设置加载更多状态
                _uiState.update { it.copy(isLoadingMore = true) }
                
                // 获取当前账户ID
                val accountId = getCurrentAccountId()
                
                // 计算下一页页码
                val nextPage = _uiState.value.currentPage + 1
                
                // 加载下一页数据
                getConversationsUseCase(
                    accountId = accountId,
                    page = nextPage,
                    pageSize = PAGE_SIZE
                )
                .catch { exception ->
                    Log.e(TAG, "加载更多对话失败", exception)
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            error = exception.message ?: "加载更多失败"
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { newConversations ->
                            Log.d(TAG, "加载更多对话成功: ${newConversations.size} 个对话")
                            _uiState.update {
                                it.copy(
                                    conversations = it.conversations + newConversations,
                                    isLoadingMore = false,
                                    currentPage = nextPage,
                                    hasMore = newConversations.size >= PAGE_SIZE
                                )
                            }
                        },
                        onFailure = { exception ->
                            Log.e(TAG, "加载更多对话失败", exception)
                            _uiState.update {
                                it.copy(
                                    isLoadingMore = false,
                                    error = exception.message ?: "加载更多失败"
                                )
                            }
                        }
                    )
                }
            } catch (e: CancellationException) {
                // 协程被取消是正常行为,不需要记录错误
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "加载更多对话异常", e)
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = e.message ?: "加载更多失败"
                    )
                }
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 获取当前账户ID
     * 
     * @return 账户ID，如果没有账户则返回 null
     */
    private suspend fun getCurrentAccountId(): String? {
        // TODO: 从 AccountRepository 获取当前账户
        // 这里暂时返回 null，表示获取所有账户的对话
        return null
    }
}
