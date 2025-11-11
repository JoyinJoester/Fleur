package takagi.ru.fleur.ui.screens.detail

import takagi.ru.fleur.domain.model.Email
import takagi.ru.fleur.domain.model.FleurError

/**
 * 邮件详情 UI 状态
 */
data class EmailDetailUiState(
    val email: Email? = null,
    val isLoading: Boolean = false,
    val error: FleurError? = null,
    val isStarred: Boolean = false
)
