package takagi.ru.fleur.ui.screens.folder

/**
 * 邮件操作类型枚举
 * 定义所有可能的邮件操作
 */
enum class EmailAction {
    /**
     * 删除邮件（移至垃圾箱）
     */
    DELETE,
    
    /**
     * 归档邮件
     */
    ARCHIVE,
    
    /**
     * 取消归档（移至收件箱）
     */
    UNARCHIVE,
    
    /**
     * 添加星标
     */
    STAR,
    
    /**
     * 取消星标
     */
    UNSTAR,
    
    /**
     * 恢复邮件（从垃圾箱恢复到收件箱）
     */
    RESTORE,
    
    /**
     * 标记为已读
     */
    MARK_READ,
    
    /**
     * 标记为未读
     */
    MARK_UNREAD
}
