package takagi.ru.fleur.domain.model

/**
 * 邮件地址模型
 * @property address 邮箱地址
 * @property name 显示名称（可选）
 */
data class EmailAddress(
    val address: String,
    val name: String? = null
) {
    /**
     * 格式化显示
     * 例如: "张三 <zhangsan@example.com>" 或 "zhangsan@example.com"
     */
    fun formatted(): String = if (name != null) {
        "$name <$address>"
    } else {
        address
    }
}
