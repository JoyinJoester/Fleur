package takagi.ru.fleur.ui.screens.search

import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.FleurError
import takagi.ru.fleur.domain.model.SearchFilters

/**
 * 搜索界面状态
 * @property query 搜索关键词
 * @property searchResults 搜索结果列表
 * @property searchHistory 搜索历史（最近10条）
 * @property filters 当前应用的过滤器
 * @property isSearching 是否正在搜索
 * @property error 错误信息
 * @property showFilters 是否显示过滤器面板
 */
data class SearchUiState(
    val query: String = "",
    val searchResults: List<Email> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val filters: SearchFilters = SearchFilters(),
    val isSearching: Boolean = false,
    val error: FleurError? = null,
    val showFilters: Boolean = false
) {
    /**
     * 是否有活动的过滤器
     */
    fun hasActiveFilters(): Boolean {
        return filters.dateRange != null ||
                filters.sender != null ||
                filters.accountId != null ||
                filters.hasAttachment != null ||
                filters.isUnread != null ||
                filters.isStarred != null
    }
    
    /**
     * 获取活动过滤器数量
     */
    fun activeFilterCount(): Int {
        var count = 0
        if (filters.dateRange != null) count++
        if (filters.sender != null) count++
        if (filters.accountId != null) count++
        if (filters.hasAttachment != null) count++
        if (filters.isUnread != null) count++
        if (filters.isStarred != null) count++
        return count
    }
}
