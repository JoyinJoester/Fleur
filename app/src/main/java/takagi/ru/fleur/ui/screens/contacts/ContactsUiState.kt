package takagi.ru.fleur.ui.screens.contacts

import androidx.compose.runtime.Immutable
import takagi.ru.fleur.ui.model.ContactUiModel

/**
 * 联系人页面 UI 状态
 * 
 * @property contacts 保存的联系人列表
 * @property frequentEmails 往来过的邮箱地址列表(未保存为联系人)
 * @property isLoading 是否正在加载
 * @property error 错误信息
 * @property searchQuery 搜索关键词
 * @property isSearchActive 搜索是否激活
 * @property selectedContact 选中的联系人
 * @property showDetailSheet 是否显示详情面板
 * @property showFrequentSection 是否展开往来邮箱区域
 */
@Immutable
data class ContactsUiState(
    val contacts: List<ContactUiModel> = emptyList(),
    val frequentEmails: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val selectedContact: ContactUiModel? = null,
    val showDetailSheet: Boolean = false,
    val showFrequentSection: Boolean = false
)
