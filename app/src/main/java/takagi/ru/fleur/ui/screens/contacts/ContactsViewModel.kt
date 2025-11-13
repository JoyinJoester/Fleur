package takagi.ru.fleur.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import takagi.ru.fleur.domain.usecase.GetContactsUseCase
import takagi.ru.fleur.domain.usecase.SearchContactsUseCase
import takagi.ru.fleur.ui.model.ContactUiModel
import javax.inject.Inject

/**
 * 联系人页面 ViewModel
 * 管理联系人列表的状态和业务逻辑
 */
@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val searchContactsUseCase: SearchContactsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()
    
    init {
        loadContacts()
    }
    
    /**
     * 加载联系人列表
     * @param refresh 是否强制刷新
     */
    fun loadContacts(refresh: Boolean = false) {
        viewModelScope.launch {
            // 设置加载状态
            _uiState.update { 
                it.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    error = null
                )
            }
            
            getContactsUseCase()
                .catch { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = exception.message ?: "加载联系人失败"
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { contacts ->
                            _uiState.update { 
                                it.copy(
                                    contacts = contacts,
                                    isLoading = false,
                                    isRefreshing = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { exception ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    error = exception.message ?: "加载联系人失败"
                                )
                            }
                        }
                    )
                }
        }
    }
    
    /**
     * 搜索联系人
     * @param query 搜索关键词
     */
    fun searchContacts(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            _uiState.update { 
                it.copy(
                    searchResults = emptyList(),
                    error = null
                )
            }
            return
        }
        
        viewModelScope.launch {
            searchContactsUseCase(query)
                .catch { exception ->
                    _uiState.update { 
                        it.copy(
                            error = exception.message ?: "搜索失败"
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { results ->
                            _uiState.update { 
                                it.copy(
                                    searchResults = results,
                                    error = null
                                )
                            }
                        },
                        onFailure = { exception ->
                            _uiState.update { 
                                it.copy(
                                    error = exception.message ?: "搜索失败"
                                )
                            }
                        }
                    )
                }
        }
    }
    
    /**
     * 设置搜索激活状态
     * @param active 是否激活搜索
     */
    fun setSearchActive(active: Boolean) {
        _uiState.update { 
            it.copy(
                isSearchActive = active,
                searchQuery = if (!active) "" else it.searchQuery,
                searchResults = if (!active) emptyList() else it.searchResults
            )
        }
    }
    
    /**
     * 显示联系人详情
     * @param contact 要显示的联系人
     */
    fun showContactDetail(contact: ContactUiModel) {
        _uiState.update { 
            it.copy(
                selectedContact = contact,
                showDetailSheet = true
            )
        }
    }
    
    /**
     * 隐藏联系人详情
     */
    fun hideContactDetail() {
        _uiState.update { 
            it.copy(
                selectedContact = null,
                showDetailSheet = false
            )
        }
    }
    
    /**
     * 导航到聊天页面
     * 此方法由 UI 层调用，实际导航由 NavController 处理
     * @param contact 联系人
     * @return 对话ID，如果没有则返回 null
     */
    fun navigateToChat(contact: ContactUiModel): String? {
        return contact.conversationId
    }
    
    /**
     * 导航到撰写邮件页面
     * 此方法由 UI 层调用，实际导航由 NavController 处理
     * @param contact 联系人
     * @return 收件人邮箱地址
     */
    fun navigateToCompose(contact: ContactUiModel): String {
        return contact.email
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
