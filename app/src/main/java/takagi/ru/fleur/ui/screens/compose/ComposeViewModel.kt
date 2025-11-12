package takagi.ru.fleur.ui.screens.compose

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.Attachment
import takagi.ru.fleur.domain.model.ComposeMode
import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.EmailAddress
import takagi.ru.fleur.domain.model.FleurError
import takagi.ru.fleur.domain.repository.AccountRepository
import takagi.ru.fleur.domain.repository.EmailRepository
import takagi.ru.fleur.domain.usecase.SendEmailUseCase
import takagi.ru.fleur.util.EmailContentFormatter
import java.util.UUID
import javax.inject.Inject

/**
 * 邮件撰写 ViewModel
 * 管理邮件撰写界面的状态和业务逻辑
 */
@HiltViewModel
class ComposeViewModel @Inject constructor(
    private val sendEmailUseCase: SendEmailUseCase,
    private val accountRepository: AccountRepository,
    private val emailRepository: EmailRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ComposeUiState())
    val uiState: StateFlow<ComposeUiState> = _uiState.asStateFlow()
    
    private var autoSaveJob: Job? = null
    private var lastInputTime = 0L
    
    companion object {
        private const val TAG = "ComposeViewModel"
        private const val AUTO_SAVE_INTERVAL = 30_000L // 30秒
        private const val INPUT_DEBOUNCE_DELAY = 3_000L // 3秒
    }
    
    init {
        loadDefaultAccount()
        startAutoSave()
        
        // 从路由参数获取撰写模式和引用邮件ID
        val mode = savedStateHandle.get<String>("mode")
        val referenceId = savedStateHandle.get<String>("referenceId")
        
        if (mode != null && referenceId != null) {
            val composeMode = try {
                ComposeMode.valueOf(mode)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "无效的撰写模式: $mode", e)
                ComposeMode.NEW
            }
            
            _uiState.update {
                it.copy(
                    composeMode = composeMode,
                    referenceEmailId = referenceId
                )
            }
            
            // 加载引用邮件并预填充内容
            loadReferenceEmail(referenceId, composeMode)
        }
    }
    
    /**
     * 加载默认账户
     */
    private fun loadDefaultAccount() {
        viewModelScope.launch {
            accountRepository.getAccounts().first().let { accounts ->
                val defaultAccount = accounts.firstOrNull { it.isDefault } ?: accounts.firstOrNull()
                _uiState.update { it.copy(selectedAccount = defaultAccount) }
            }
        }
    }
    
    /**
     * 启动自动保存
     */
    private fun startAutoSave() {
        autoSaveJob = viewModelScope.launch {
            while (true) {
                delay(AUTO_SAVE_INTERVAL)
                
                val currentState = _uiState.value
                if (currentState.isDirty && !currentState.isSending) {
                    saveDraft()
                }
            }
        }
    }
    
    /**
     * 检查输入防抖并保存草稿
     */
    private fun checkInputDebounce() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            lastInputTime = currentTime
            
            delay(INPUT_DEBOUNCE_DELAY)
            
            // 如果3秒内没有新的输入，保存草稿
            if (lastInputTime == currentTime && _uiState.value.isDirty) {
                saveDraft()
            }
        }
    }
    
    /**
     * 保存草稿
     */
    private fun saveDraft() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingDraft = true) }
            
            // TODO: 实现草稿保存到本地数据库
            // 这里暂时只更新保存时间
            delay(500) // 模拟保存操作
            
            _uiState.update {
                it.copy(
                    isSavingDraft = false,
                    lastDraftSaveTime = System.currentTimeMillis(),
                    isDirty = false
                )
            }
        }
    }
    
    /**
     * 更新收件人
     */
    fun updateToAddresses(addresses: String) {
        _uiState.update { it.copy(toAddresses = addresses, isDirty = true) }
        checkInputDebounce()
    }
    
    /**
     * 更新抄送
     */
    fun updateCcAddresses(addresses: String) {
        _uiState.update { it.copy(ccAddresses = addresses, isDirty = true) }
        checkInputDebounce()
    }
    
    /**
     * 更新密送
     */
    fun updateBccAddresses(addresses: String) {
        _uiState.update { it.copy(bccAddresses = addresses, isDirty = true) }
        checkInputDebounce()
    }
    
    /**
     * 更新主题
     */
    fun updateSubject(subject: String) {
        _uiState.update { it.copy(subject = subject, isDirty = true) }
        checkInputDebounce()
    }
    
    /**
     * 更新正文
     */
    fun updateBody(body: String) {
        _uiState.update { it.copy(body = body, isDirty = true) }
        checkInputDebounce()
    }
    
    /**
     * 选择发件账户
     */
    fun selectAccount(account: Account) {
        _uiState.update { it.copy(selectedAccount = account, showAccountSelector = false) }
    }
    
    /**
     * 显示/隐藏账户选择器
     */
    fun toggleAccountSelector() {
        _uiState.update { it.copy(showAccountSelector = !it.showAccountSelector) }
    }
    
    /**
     * 显示/隐藏抄送和密送字段
     */
    fun toggleCcBcc() {
        _uiState.update { it.copy(showCcBcc = !it.showCcBcc) }
    }
    
    /**
     * 添加附件
     */
    fun addAttachment(uri: Uri, fileName: String, mimeType: String, size: Long) {
        val currentState = _uiState.value
        
        // 检查单个附件大小限制
        if (size > ComposeUiState.MAX_ATTACHMENT_SIZE) {
            _uiState.update {
                it.copy(
                    error = FleurError.ValidationError(
                        field = "附件",
                        errorMessage = "附件大小不能超过 ${ComposeUiState.MAX_ATTACHMENT_SIZE / (1024 * 1024)} MB"
                    )
                )
            }
            return
        }
        
        // 检查总附件大小限制
        val totalSize = currentState.totalAttachmentSize() + size
        if (totalSize > ComposeUiState.MAX_TOTAL_ATTACHMENT_SIZE) {
            _uiState.update {
                it.copy(
                    error = FleurError.ValidationError(
                        field = "附件",
                        errorMessage = "附件总大小不能超过 ${ComposeUiState.MAX_TOTAL_ATTACHMENT_SIZE / (1024 * 1024)} MB"
                    )
                )
            }
            return
        }
        
        val attachment = Attachment(
            id = UUID.randomUUID().toString(),
            emailId = "", // 发送时会设置
            fileName = fileName,
            mimeType = mimeType,
            size = size,
            localPath = uri.toString()
        )
        
        _uiState.update {
            it.copy(
                attachments = it.attachments + attachment,
                isDirty = true
            )
        }
    }
    
    /**
     * 删除附件
     */
    fun removeAttachment(attachmentId: String) {
        _uiState.update {
            it.copy(
                attachments = it.attachments.filter { attachment -> attachment.id != attachmentId },
                isDirty = true
            )
        }
    }
    
    /**
     * 发送邮件
     */
    fun sendEmail(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        
        // 验证
        if (!currentState.canSend()) {
            _uiState.update {
                it.copy(error = FleurError.ValidationError(
                    field = "邮件",
                    errorMessage = "请检查邮件内容"
                ))
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            
            try {
                val email = buildEmail(currentState)
                val result = sendEmailUseCase(email)
                
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                isDirty = false
                            )
                        }
                        onSuccess()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                error = error as? FleurError
                                    ?: FleurError.UnknownError(error.message ?: "发送失败")
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        error = FleurError.UnknownError(e.message ?: "发送失败", e)
                    )
                }
            }
        }
    }
    
    /**
     * 构建邮件对象
     */
    private fun buildEmail(state: ComposeUiState): Email {
        val account = state.selectedAccount
            ?: throw IllegalStateException("未选择发件账户")

        val mergedBody = when (state.composeMode) {
            ComposeMode.REPLY, ComposeMode.REPLY_ALL -> {
                val reference = state.referenceEmail
                if (reference != null) {
                    EmailContentFormatter.mergeReplyContent(state.body, reference)
                } else {
                    state.body
                }
            }
            ComposeMode.FORWARD -> {
                val reference = state.referenceEmail
                if (reference != null) {
                    EmailContentFormatter.mergeForwardContent(state.body, reference)
                } else {
                    state.body
                }
            }
            else -> state.body
        }

        return Email(
            id = UUID.randomUUID().toString(),
            threadId = UUID.randomUUID().toString(),
            accountId = account.id,
            from = EmailAddress(
                address = account.email,
                name = account.displayName
            ),
            to = parseEmailAddresses(state.toAddresses),
            cc = parseEmailAddresses(state.ccAddresses),
            bcc = parseEmailAddresses(state.bccAddresses),
            subject = state.subject,
            bodyPreview = mergedBody.take(200),
            bodyPlain = mergedBody,
            bodyHtml = null,
            attachments = state.attachments,
            timestamp = Clock.System.now(),
            isRead = true,
            isStarred = false
        )
    }
    
    /**
     * 解析邮件地址字符串
     */
    private fun parseEmailAddresses(addresses: String): List<EmailAddress> {
        if (addresses.isBlank()) return emptyList()
        
        return addresses.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { EmailAddress(address = it) }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 加载引用邮件
     * 
     * 根据撰写模式加载原邮件并预填充内容
     * 
     * @param emailId 引用的邮件ID
     * @param mode 撰写模式
     */
    private fun loadReferenceEmail(emailId: String, mode: ComposeMode) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                emailRepository.getEmailById(emailId).first().fold(
                    onSuccess = { email ->
                        prefillContent(email, mode)
                    },
                    onFailure = { error ->
                        Log.w(TAG, "未找到引用邮件: $emailId", error)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = FleurError.NotFoundError("未找到原邮件"),
                                referenceEmail = null,
                                quotedOriginalContent = ""
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "加载引用邮件失败", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = FleurError.UnknownError("加载原邮件失败: ${e.message}", e),
                        referenceEmail = null,
                        quotedOriginalContent = ""
                    )
                }
            }
        }
    }
    
    /**
     * 根据撰写模式预填充内容
     * 
     * @param originalEmail 原邮件
     * @param mode 撰写模式
     */
    private fun prefillContent(originalEmail: Email, mode: ComposeMode) {
        when (mode) {
            ComposeMode.REPLY -> prefillReply(originalEmail)
            ComposeMode.REPLY_ALL -> prefillReplyAll(originalEmail)
            ComposeMode.FORWARD -> prefillForward(originalEmail)
            ComposeMode.DRAFT -> prefillDraft(originalEmail)
            ComposeMode.NEW -> {
                // 新邮件模式不需要预填充
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        referenceEmail = null,
                        quotedOriginalContent = ""
                    )
                }
            }
        }
    }
    
    /**
     * 预填充回复内容
     * 
     * @param originalEmail 原邮件
     */
    private fun prefillReply(originalEmail: Email) {
        _uiState.update {
            it.copy(
                referenceEmail = originalEmail,
                quotedOriginalContent = EmailContentFormatter.buildReplyQuote(originalEmail),
                toAddresses = originalEmail.from.formatted(),
                subject = EmailContentFormatter.addReplyPrefix(originalEmail.subject),
                body = "",
                isLoading = false,
                isDirty = false // 预填充的内容不算修改
            )
        }
    }
    
    /**
     * 预填充全部回复内容
     * 
     * @param originalEmail 原邮件
     */
    private fun prefillReplyAll(originalEmail: Email) {
        viewModelScope.launch {
            // 获取当前用户的邮箱地址
            val currentAccount = _uiState.value.selectedAccount
            val currentUserEmail = currentAccount?.email
            
            // 构建收件人列表：原发件人 + 原收件人（排除当前用户）
            val toList = mutableListOf<EmailAddress>()
            toList.add(originalEmail.from)
            toList.addAll(originalEmail.to.filter { it.address != currentUserEmail })
            
            // 构建抄送列表：原抄送人（排除当前用户）
            val ccList = originalEmail.cc.filter { it.address != currentUserEmail }
            
            _uiState.update {
                it.copy(
                    referenceEmail = originalEmail,
                    quotedOriginalContent = EmailContentFormatter.buildReplyQuote(originalEmail),
                    toAddresses = toList.joinToString(", ") { addr -> addr.formatted() },
                    ccAddresses = ccList.joinToString(", ") { addr -> addr.formatted() },
                    subject = EmailContentFormatter.addReplyPrefix(originalEmail.subject),
                    body = "",
                    showCcBcc = ccList.isNotEmpty(), // 如果有抄送人，自动显示抄送字段
                    isLoading = false,
                    isDirty = false
                )
            }
        }
    }
    
    /**
     * 预填充转发内容
     * 
     * @param originalEmail 原邮件
     */
    private fun prefillForward(originalEmail: Email) {
        _uiState.update {
            it.copy(
                referenceEmail = originalEmail,
                quotedOriginalContent = EmailContentFormatter.buildForwardQuote(originalEmail),
                toAddresses = "", // 转发时收件人为空
                subject = EmailContentFormatter.addForwardPrefix(originalEmail.subject),
                body = "",
                attachments = originalEmail.attachments, // 保留原邮件附件
                isLoading = false,
                isDirty = false
            )
        }
    }
    
    /**
     * 预填充草稿内容
     * 
     * @param draftEmail 草稿邮件
     */
    private fun prefillDraft(draftEmail: Email) {
        _uiState.update {
            it.copy(
                referenceEmail = null,
                quotedOriginalContent = "",
                toAddresses = draftEmail.to.joinToString(", ") { addr -> addr.formatted() },
                ccAddresses = draftEmail.cc.joinToString(", ") { addr -> addr.formatted() },
                bccAddresses = draftEmail.bcc.joinToString(", ") { addr -> addr.formatted() },
                subject = draftEmail.subject,
                body = draftEmail.bodyPlain,
                attachments = draftEmail.attachments,
                showCcBcc = draftEmail.cc.isNotEmpty() || draftEmail.bcc.isNotEmpty(),
                isLoading = false,
                isDirty = false
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}
