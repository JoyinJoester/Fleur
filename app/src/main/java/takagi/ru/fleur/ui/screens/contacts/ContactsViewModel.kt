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
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.ui.model.ContactUiModel
import javax.inject.Inject

/**
 * 联系人页面 ViewModel
 * 管理联系人列表的状态和业务逻辑
 */
@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val searchContactsUseCase: SearchContactsUseCase,
    private val emailRepository: EmailRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()
    
    init {
        loadContacts()
        loadFrequentEmails()
    }
    
    /**
     * 加载联系人列表
     */
    fun loadContacts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            getContactsUseCase()
                .catch { exception ->
                    _uiState.update { it.copy(isLoading = false, error = exception.message ?: "加载联系人失败") }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { contacts ->
                            _uiState.update { it.copy(contacts = contacts, isLoading = false, error = null) }
                        },
                        onFailure = { exception ->
                            _uiState.update { it.copy(isLoading = false, error = exception.message ?: "加载联系人失败") }
                        }
                    )
                }
        }
    }
    
    /**
     * 加载往来过的邮箱地址
     * 从所有邮件的发件人/收件人中提取,排除已保存的联系人
     */
    private fun loadFrequentEmails() {
        viewModelScope.launch {
            emailRepository.getEmails(
                accountId = null,
                page = 0,
                pageSize = 100
            )
                .catch { /* 忽略错误 */ }
                .collect { result ->
                    result.fold(
                        onSuccess = { emails ->
                            val savedEmails = _uiState.value.contacts.map { it.email }.toSet()
                            val frequentEmails = emails
                                .flatMap { msg ->
                                    val fromEmail = msg.from.address
                                    val toEmails = msg.to.map { addr -> addr.address }
                                    val ccEmails = msg.cc.map { addr -> addr.address }
                                    listOf(fromEmail) + toEmails + ccEmails
                                }
                                .distinct()
                                .filterNot { it in savedEmails }
                                .take(20)
                            
                            _uiState.update { state -> state.copy(frequentEmails = frequentEmails) }
                        },
                        onFailure = { /* 忽略错误 */ }
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
    }
    
    /**
     * 设置搜索激活状态
     * @param active 是否激活搜索
     */
    fun setSearchActive(active: Boolean) {
        _uiState.update { 
            it.copy(
                isSearchActive = active,
                searchQuery = if (!active) "" else it.searchQuery
            )
        }
    }
    
    /**
     * 切换往来邮箱区域展开状态
     */
    fun toggleFrequentSection() {
        _uiState.update { it.copy(showFrequentSection = !it.showFrequentSection) }
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
