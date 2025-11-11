package takagi.ru.fleur.domain.model

/**
 * 同步结果
 * @property accountId 账户ID
 * @property newEmailsCount 新邮件数量
 * @property updatedEmailsCount 更新的邮件数量
 * @property deletedEmailsCount 删除的邮件数量
 * @property success 是否成功
 * @property error 错误信息（如果失败）
 */
data class SyncResult(
    val accountId: String,
    val newEmailsCount: Int = 0,
    val updatedEmailsCount: Int = 0,
    val deletedEmailsCount: Int = 0,
    val success: Boolean = true,
    val error: FleurError? = null
) {
    /**
     * 总变更数量
     */
    fun totalChanges(): Int = newEmailsCount + updatedEmailsCount + deletedEmailsCount
}
