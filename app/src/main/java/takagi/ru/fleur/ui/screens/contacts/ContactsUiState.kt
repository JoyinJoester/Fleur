package takagi.ru.fleur.ui.screens.contacts

import androidx.compose.runtime.Immutable
import takagi.ru.fleur.ui.model.ContactUiModel

/**
 * 联系人页面 UI 状态
 * 
 * @property contacts 联系人列表
 * @property isLoading 是否正在加载
 * @property isRefreshing 是否正在刷新
 * @property error 错误信息
 * @property searchQuery 搜索关键词
 * @property searchResults 搜索结果列表
 * @property isSearchActive 搜索是否激活
 * @property selectedContact 选中的联系人
 * @property showDetailSheet 是否显示详情面板
 */
@Immutable
data class ContactsUiState(
    val contacts: List<ContactUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<ContactUiModel> = emptyList(),
    val isSearchActive: Boolean = false,
    val selectedContact: ContactUiModel? = null,
    val showDetailSheet: Boolean = false
)
