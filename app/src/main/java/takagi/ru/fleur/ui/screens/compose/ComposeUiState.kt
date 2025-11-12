package takagi.ru.fleur.ui.screens.compose

import takagi.ru.fleur.domain.model.Account
import takagi.ru.fleur.domain.model.Attachment
import takagi.ru.fleur.domain.model.ComposeMode
import takagi.ru.fleur.domain.model.FleurError

/**
 * 邮件撰写界面状态
 * @property selectedAccount 选中的发件账户
 * @property toAddresses 收件人地址列表（逗号分隔的字符串）
 * @property ccAddresses 抄送地址列表（逗号分隔的字符串）
 * @property bccAddresses 密送地址列表（逗号分隔的字符串）
 * @property subject 主题
 * @property body 正文内容
 * @property attachments 附件列表
 * @property isSending 是否正在发送
 * @property isSavingDraft 是否正在保存草稿
 * @property isLoading 是否正在加载引用邮件
 * @property error 错误信息
 * @property showAccountSelector 是否显示账户选择器
 * @property showCcBcc 是否显示抄送和密送字段
 * @property lastDraftSaveTime 最后一次保存草稿的时间戳
 * @property isDirty 内容是否已修改
 * @property composeMode 撰写模式（NEW, REPLY, REPLY_ALL, FORWARD, DRAFT）
 * @property referenceEmailId 引用的邮件ID（回复、转发或草稿时使用）
 */
data class ComposeUiState(
    val selectedAccount: Account? = null,
    val toAddresses: String = "",
    val ccAddresses: String = "",
    val bccAddresses: String = "",
    val subject: String = "",
    val body: String = "",
    val attachments: List<Attachment> = emptyList(),
    val isSending: Boolean = false,
    val isSavingDraft: Boolean = false,
    val isLoading: Boolean = false,
    val error: FleurError? = null,
    val showAccountSelector: Boolean = false,
    val showCcBcc: Boolean = false,
    val lastDraftSaveTime: Long? = null,
    val isDirty: Boolean = false,
    val composeMode: ComposeMode = ComposeMode.NEW,
    val referenceEmailId: String? = null
) {
    /**
     * 验证是否可以发送
     */
    fun canSend(): Boolean {
        return selectedAccount != null &&
                toAddresses.isNotBlank() &&
                subject.isNotBlank() &&
                !isSending &&
                isValidEmailAddresses(toAddresses) &&
                (ccAddresses.isBlank() || isValidEmailAddresses(ccAddresses)) &&
                (bccAddresses.isBlank() || isValidEmailAddresses(bccAddresses)) &&
                totalAttachmentSize() <= MAX_TOTAL_ATTACHMENT_SIZE
    }
    
    /**
     * 计算附件总大小
     */
    fun totalAttachmentSize(): Long {
        return attachments.sumOf { it.size }
    }
    
    /**
     * 验证邮件地址格式
     */
    private fun isValidEmailAddresses(addresses: String): Boolean {
        if (addresses.isBlank()) return true
        
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return addresses.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .all { emailRegex.matches(it) }
    }
    
    companion object {
        const val MAX_ATTACHMENT_SIZE = 25L * 1024 * 1024 // 25MB
        const val MAX_TOTAL_ATTACHMENT_SIZE = 50L * 1024 * 1024 // 50MB
    }
}
