package takagi.ru.fleur.domain.model

/**
 * 邮件撰写模式
 * 
 * 定义用户撰写邮件时的不同模式，用于确定如何预填充邮件内容
 */
enum class ComposeMode {
    /**
     * 新邮件 - 从头开始撰写新邮件
     */
    NEW,
    
    /**
     * 回复 - 回复单个发件人
     */
    REPLY,
    
    /**
     * 全部回复 - 回复所有收件人和发件人
     */
    REPLY_ALL,
    
    /**
     * 转发 - 将邮件转发给其他人
     */
    FORWARD,
    
    /**
     * 草稿 - 继续编辑已保存的草稿
     */
    DRAFT
}
