package takagi.ru.fleur.domain.model

import kotlinx.datetime.Instant

/**
 * 搜索过滤器
 * @property dateRange 日期范围（可选）
 * @property sender 发件人过滤（可选）
 * @property accountId 账户ID过滤（可选）
 * @property threadId 线程ID过滤（可选）
 * @property hasAttachment 是否有附件（可选）
 * @property isUnread 是否未读（可选）
 * @property isStarred 是否星标（可选）
 */
data class SearchFilters(
    val dateRange: DateRange? = null,
    val sender: String? = null,
    val accountId: String? = null,
    val threadId: String? = null,
    val hasAttachment: Boolean? = null,
    val isUnread: Boolean? = null,
    val isStarred: Boolean? = null
)

/**
 * 日期范围
 * @property start 开始时间
 * @property end 结束时间
 */
data class DateRange(
    val start: Instant,
    val end: Instant
)
