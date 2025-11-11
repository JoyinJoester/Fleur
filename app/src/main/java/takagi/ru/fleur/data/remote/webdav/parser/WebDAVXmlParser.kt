package takagi.ru.fleur.data.remote.webdav.parser

import takagi.ru.fleur.data.remote.webdav.dto.EmailDto
import takagi.ru.fleur.data.remote.webdav.dto.WebDAVMultiStatusResponse

/**
 * WebDAV XML 解析器接口
 * 用于解析 WebDAV 服务器返回的 XML 响应
 */
interface WebDAVXmlParser {
    /**
     * 解析多状态响应（PROPFIND 响应）
     * @param xml XML 字符串
     * @return 多状态响应对象
     */
    fun parseMultiStatusResponse(xml: String): Result<WebDAVMultiStatusResponse>
    
    /**
     * 解析邮件内容
     * @param xml 邮件 XML 字符串
     * @return 邮件 DTO
     */
    fun parseEmail(xml: String): Result<EmailDto>
    
    /**
     * 解析邮件列表
     * @param xml 邮件列表 XML 字符串
     * @return 邮件 DTO 列表
     */
    fun parseEmailList(xml: String): Result<List<EmailDto>>
}
