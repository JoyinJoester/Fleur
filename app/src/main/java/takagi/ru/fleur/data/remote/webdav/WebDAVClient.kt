package takagi.ru.fleur.data.remote.webdav

import kotlinx.datetime.Instant
import takagi.ru.fleur.data.remote.webdav.dto.EmailDto
import takagi.ru.fleur.data.remote.webdav.dto.EmailFlags
import takagi.ru.fleur.domain.model.WebDAVConfig

/**
 * WebDAV 客户端接口
 * 用于与 WebDAV 服务器进行邮件同步
 */
interface WebDAVClient {
    /**
     * 连接到 WebDAV 服务器并验证凭证
     * @param config WebDAV 配置
     * @param password 密码
     * @return 连接结果
     */
    suspend fun connect(config: WebDAVConfig, password: String): Result<Unit>
    
    /**
     * 获取邮件列表
     * @param since 起始时间，用于增量同步（可选）
     * @return 邮件 DTO 列表
     */
    suspend fun fetchEmails(since: Instant? = null): Result<List<EmailDto>>
    
    /**
     * 发送邮件
     * @param email 邮件 DTO
     * @return 发送结果
     */
    suspend fun sendEmail(email: EmailDto): Result<Unit>
    
    /**
     * 删除邮件
     * @param emailId 邮件 ID
     * @return 删除结果
     */
    suspend fun deleteEmail(emailId: String): Result<Unit>
    
    /**
     * 更新邮件标记
     * @param emailId 邮件 ID
     * @param flags 邮件标记
     * @return 更新结果
     */
    suspend fun updateEmailFlags(emailId: String, flags: EmailFlags): Result<Unit>
}
