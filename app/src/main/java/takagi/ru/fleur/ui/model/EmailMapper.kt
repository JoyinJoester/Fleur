package takagi.ru.fleur.ui.model

import androidx.compose.ui.graphics.Color
import takagi.ru.fleur.domain.model.Email

/**
 * Email 到 EmailUiModel 的映射扩展函数
 */
fun Email.toUiModel(accountColor: Color = Color.Blue): EmailUiModel {
    return EmailUiModel(
        id = id,
        threadId = threadId,
        accountColor = accountColor,
        fromName = from.name ?: from.address,
        fromAddress = from.address,
        subject = subject,
        preview = bodyPreview,
        timestamp = timestamp,
        isRead = isRead,
        isStarred = isStarred,
        hasAttachments = attachments.isNotEmpty(),
        attachmentCount = attachments.size
    )
}
