package takagi.ru.fleur.ui.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import takagi.ru.fleur.domain.model.Contact
import takagi.ru.fleur.domain.repository.ContactRepository
import java.util.UUID
import javax.inject.Inject

/**
 * 添加/编辑联系人 ViewModel
 */
@HiltViewModel
class AddContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddContactUiState())
    val uiState: StateFlow<AddContactUiState> = _uiState.asStateFlow()
    
    /**
     * 初始化(用于编辑现有联系人)
     */
    fun initContact(contact: Contact) {
        _uiState.update {
            it.copy(
                contactId = contact.id,
                name = contact.name,
                email = contact.email,
                phoneNumber = contact.phoneNumber ?: "",
                organization = contact.organization ?: "",
                jobTitle = contact.jobTitle ?: "",
                address = contact.address ?: "",
                notes = contact.notes ?: "",
                isEditing = true
            )
        }
    }
    
    /**
     * 预填充邮箱(从往来邮箱添加)
     */
    fun prefillEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }
    
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }
    
    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }
    
    fun updatePhoneNumber(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }
    
    fun updateOrganization(org: String) {
        _uiState.update { it.copy(organization = org) }
    }
    
    fun updateJobTitle(title: String) {
        _uiState.update { it.copy(jobTitle = title) }
    }
    
    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }
    
    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }
    
    /**
     * 验证输入
     */
    private fun validateInput(): Boolean {
        val state = _uiState.value
        var isValid = true
        
        // 验证姓名
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "请输入姓名") }
            isValid = false
        }
        
        // 验证邮箱
        if (state.email.isBlank()) {
            _uiState.update { it.copy(emailError = "请输入邮箱地址") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(emailError = "邮箱地址格式不正确") }
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * 保存联系人
     */
    fun saveContact(onSuccess: () -> Unit) {
        if (!validateInput()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            val state = _uiState.value
            val now = Clock.System.now()
            
            val contact = Contact(
                id = state.contactId ?: UUID.randomUUID().toString(),
                name = state.name.trim(),
                email = state.email.trim(),
                phoneNumber = state.phoneNumber.takeIf { it.isNotBlank() },
                organization = state.organization.takeIf { it.isNotBlank() },
                jobTitle = state.jobTitle.takeIf { it.isNotBlank() },
                address = state.address.takeIf { it.isNotBlank() },
                notes = state.notes.takeIf { it.isNotBlank() },
                avatarUrl = null,
                isFavorite = false,
                createdAt = now,
                updatedAt = now
            )
            
            val result = if (state.isEditing) {
                contactRepository.updateContact(contact)
            } else {
                // 检查邮箱是否已存在
                if (contactRepository.emailExists(state.email.trim())) {
                    _uiState.update { 
                        it.copy(
                            isSaving = false, 
                            emailError = "该邮箱已存在"
                        ) 
                    }
                    return@launch
                }
                contactRepository.addContact(contact)
            }
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isSaving = false, 
                            error = exception.message ?: "保存失败"
                        ) 
                    }
                }
            )
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 添加联系人 UI 状态
 */
data class AddContactUiState(
    val contactId: String? = null,
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val organization: String = "",
    val jobTitle: String = "",
    val address: String = "",
    val notes: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val error: String? = null
)
