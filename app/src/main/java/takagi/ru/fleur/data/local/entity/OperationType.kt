package takagi.ru.fleur.data.local.entity

/**
 * 同步操作类型枚举
 * 定义所有支持的 WebDAV 同步操作类型
 */
enum class OperationType {
    /**
     * 标记为已读
     */
    MARK_READ,
    
    /**
     * 标记为未读
     */
    MARK_UNREAD,
    
    /**
     * 归档邮件
     */
    ARCHIVE,
    
    /**
     * 删除邮件
     */
    DELETE,
    
    /**
     * 添加星标
     */
    STAR,
    
    /**
     * 移除星标
     */
    UNSTAR,
    
    /**
     * 移动到文件夹
     */
    MOVE_TO_FOLDER
}
