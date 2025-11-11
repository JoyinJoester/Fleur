package takagi.ru.fleur.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import takagi.ru.fleur.domain.model.DateRange
import takagi.ru.fleur.domain.model.FleurError
import takagi.ru.fleur.domain.model.SearchFilters
import takagi.ru.fleur.domain.usecase.SearchEmailsUseCase
import javax.inject.Inject

/**
 * 搜索 ViewModel
 * 管理搜索界面的状态和业务逻辑
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchEmailsUseCase: SearchEmailsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    
    // 搜索历史存储（简单实现，实际应该持久化到 DataStore）
    private val searchHistoryList = mutableListOf<String>()
    
    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }
    
    init {
        loadSearchHistory()
    }
    
    /**
     * 加载搜索历史
     */
    private fun loadSearchHistory() {
        // TODO: 从 DataStore 加载搜索历史
        _uiState.update { it.copy(searchHistory = searchHistoryList) }
    }
    
    /**
     * 更新搜索关键词
     */
    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        
        if (query.isBlank()) {
            // 清空搜索结果
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            searchJob?.cancel()
        } else {
            // 执行搜索（带防抖）
            performSearch(query)
        }
    }
    
    /**
     * 执行搜索
     */
    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null) }
            
            val currentFilters = _uiState.value.filters
            
            searchEmailsUseCase(query, currentFilters)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            error = FleurError.UnknownError(e.message ?: "搜索失败", e)
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { emails ->
                            _uiState.update {
                                it.copy(
                                    searchResults = emails,
                                    isSearching = false
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(
                                    isSearching = false,
                                    error = error as? FleurError
                                        ?: FleurError.UnknownError(error.message ?: "搜索失败")
                                )
                            }
                        }
                    )
                }
        }
    }
    
    /**
     * 提交搜索（添加到历史）
     */
    fun submitSearch() {
        val query = _uiState.value.query.trim()
        if (query.isNotBlank()) {
            addToHistory(query)
        }
    }
    
    /**
     * 添加到搜索历史
     */
    private fun addToHistory(query: String) {
        // 移除重复项
        searchHistoryList.remove(query)
        
        // 添加到开头
        searchHistoryList.add(0, query)
        
        // 限制历史记录数量
        if (searchHistoryList.size > MAX_HISTORY_SIZE) {
            searchHistoryList.removeAt(searchHistoryList.size - 1)
        }
        
        // TODO: 持久化到 DataStore
        
        _uiState.update { it.copy(searchHistory = searchHistoryList.toList()) }
    }
    
    /**
     * 从历史中选择搜索词
     */
    fun selectFromHistory(query: String) {
        updateQuery(query)
    }
    
    /**
     * 删除历史记录项
     */
    fun removeFromHistory(query: String) {
        searchHistoryList.remove(query)
        
        // TODO: 持久化到 DataStore
        
        _uiState.update { it.copy(searchHistory = searchHistoryList.toList()) }
    }
    
    /**
     * 清空搜索历史
     */
    fun clearHistory() {
        searchHistoryList.clear()
        
        // TODO: 持久化到 DataStore
        
        _uiState.update { it.copy(searchHistory = emptyList()) }
    }
    
    /**
     * 显示/隐藏过滤器面板
     */
    fun toggleFilters() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }
    
    /**
     * 更新日期范围过滤器
     */
    fun updateDateRange(dateRange: DateRange?) {
        val newFilters = _uiState.value.filters.copy(dateRange = dateRange)
        _uiState.update { it.copy(filters = newFilters) }
        
        // 如果有搜索关键词，重新搜索
        if (_uiState.value.query.isNotBlank()) {
            performSearch(_uiState.value.query)
        }
    }
    
    /**
     * 更新发件人过滤器
     */
    fun updateSenderFilter(sender: String?) {
        val newFilters = _uiState.value.filters.copy(sender = sender)
        _uiState.update { it.copy(filters = newFilters) }
        
        if (_uiState.value.query.isNotBlank()) {
            performSearch(_uiState.value.query)
        }
    }
    
    /**
     * 更新账户过滤器
     */
    fun updateAccountFilter(accountId: String?) {
        val newFilters = _uiState.value.filters.copy(accountId = accountId)
        _uiState.update { it.copy(filters = newFilters) }
        
        if (_uiState.value.query.isNotBlank()) {
            performSearch(_uiState.value.query)
        }
    }
    
    /**
     * 更新附件过滤器
     */
    fun updateAttachmentFilter(hasAttachment: Boolean?) {
        val newFilters = _uiState.value.filters.copy(hasAttachment = hasAttachment)
        _uiState.update { it.copy(filters = newFilters) }
        
        if (_uiState.value.query.isNotBlank()) {
            performSearch(_uiState.value.query)
        }
    }
    
    /**
     * 更新未读过滤器
     */
    fun updateUnreadFilter(isUnread: Boolean?) {
        val newFilters = _uiState.value.filters.copy(isUnread = isUnread)
        _uiState.update { it.copy(filters = newFilters) }
        
        if (_uiState.value.query.isNotBlank()) {
            performSearch(_uiState.value.query)
        }
    }
    
    /**
     * 更新星标过滤器
     */
    fun updateStarredFilter(isStarred: Boolean?) {
        val newFilters = _uiState.value.filters.copy(isStarred = isStarred)
        _uiState.update { it.copy(filters = newFilters) }
        
        if (_uiState.value.query.isNotBlank()) {
            performSearch(_uiState.value.query)
        }
    }
    
    /**
     * 清除所有过滤器
     */
    fun clearFilters() {
        _uiState.update { it.copy(filters = SearchFilters()) }
        
        if (_uiState.value.query.isNotBlank()) {
            performSearch(_uiState.value.query)
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
