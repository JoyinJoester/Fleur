package takagi.ru.fleur.data.remote.webdav.parser

import kotlinx.datetime.Instant
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import takagi.ru.fleur.data.remote.webdav.dto.AttachmentDto
import takagi.ru.fleur.data.remote.webdav.dto.EmailAddressDto
import takagi.ru.fleur.data.remote.webdav.dto.EmailDto
import takagi.ru.fleur.data.remote.webdav.dto.EmailFlags
import takagi.ru.fleur.data.remote.webdav.dto.WebDAVMultiStatusResponse
import takagi.ru.fleur.data.remote.webdav.dto.WebDAVResourceResponse
import java.io.StringReader
import javax.inject.Inject

/**
 * WebDAV XML 解析器实现
 * 使用 Android 的 XmlPullParser 解析 XML
 */
class WebDAVXmlParserImpl @Inject constructor() : WebDAVXmlParser {
    
    private val parserFactory = XmlPullParserFactory.newInstance().apply {
        isNamespaceAware = true
    }
    
    /**
     * 解析多状态响应
     */
    override fun parseMultiStatusResponse(xml: String): Result<WebDAVMultiStatusResponse> {
        return try {
            val parser = parserFactory.newPullParser()
            parser.setInput(StringReader(xml))
            
            val responses = mutableListOf<WebDAVResourceResponse>()
            var eventType = parser.eventType
            
            var currentHref: String? = null
            var currentStatus: Int? = null
            val currentProperties = mutableMapOf<String, String>()
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "href" -> {
                                currentHref = parser.nextText()
                            }
                            "status" -> {
                                val statusText = parser.nextText()
                                currentStatus = parseStatusCode(statusText)
                            }
                            "prop" -> {
                                parseProperties(parser, currentProperties)
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "response" && currentHref != null && currentStatus != null) {
                            responses.add(
                                WebDAVResourceResponse(
                                    href = currentHref,
                                    statusCode = currentStatus,
                                    properties = currentProperties.toMap()
                                )
                            )
                            currentHref = null
                            currentStatus = null
                            currentProperties.clear()
                        }
                    }
                }
                eventType = parser.next()
            }
            
            Result.success(WebDAVMultiStatusResponse(responses))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 解析邮件内容
     */
    override fun parseEmail(xml: String): Result<EmailDto> {
        return try {
            // 简化的邮件解析实现
            // 实际应用中需要完整的 MIME 解析器
            val email = parseSimpleEmail(xml)
            Result.success(email)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 解析邮件列表
     */
    override fun parseEmailList(xml: String): Result<List<EmailDto>> {
        return try {
            val parser = parserFactory.newPullParser()
            parser.setInput(StringReader(xml))
            
            val emails = mutableListOf<EmailDto>()
            var eventType = parser.eventType
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "email" || parser.name == "message") {
                            // 解析单个邮件
                            val emailXml = readElementText(parser)
                            parseEmail(emailXml).getOrNull()?.let { emails.add(it) }
                        }
                    }
                }
                eventType = parser.next()
            }
            
            Result.success(emails)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 解析属性
     */
    private fun parseProperties(parser: XmlPullParser, properties: MutableMap<String, String>) {
        var eventType = parser.eventType
        var depth = 1
        
        while (depth > 0) {
            eventType = parser.next()
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    depth++
                    val name = parser.name
                    val value = parser.nextText()
                    if (value.isNotEmpty()) {
                        properties[name] = value
                    }
                }
                XmlPullParser.END_TAG -> {
                    depth--
                }
            }
        }
    }
    
    /**
     * 解析状态码
     */
    private fun parseStatusCode(statusText: String): Int {
        // 格式: "HTTP/1.1 200 OK"
        val parts = statusText.split(" ")
        return if (parts.size >= 2) {
            parts[1].toIntOrNull() ?: 500
        } else {
            500
        }
    }
    
    /**
     * 读取元素文本内容
     */
    private fun readElementText(parser: XmlPullParser): String {
        val builder = StringBuilder()
        var depth = 1
        var eventType = parser.next()
        
        while (depth > 0) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    depth++
                    builder.append("<${parser.name}>")
                }
                XmlPullParser.END_TAG -> {
                    depth--
                    if (depth > 0) {
                        builder.append("</${parser.name}>")
                    }
                }
                XmlPullParser.TEXT -> {
                    builder.append(parser.text)
                }
            }
            if (depth > 0) {
                eventType = parser.next()
            }
        }
        
        return builder.toString()
    }
    
    /**
     * 简化的邮件解析
     * 实际应用中应使用完整的 MIME 解析器
     */
    private fun parseSimpleEmail(content: String): EmailDto {
        val lines = content.lines()
        var from: EmailAddressDto? = null
        var to = mutableListOf<EmailAddressDto>()
        var cc = mutableListOf<EmailAddressDto>()
        var subject = ""
        var bodyPlain = ""
        var bodyHtml: String? = null
        var timestamp = Instant.DISTANT_PAST
        
        var inBody = false
        val bodyBuilder = StringBuilder()
        
        for (line in lines) {
            when {
                line.startsWith("From:") -> {
                    from = parseEmailAddress(line.substringAfter("From:").trim())
                }
                line.startsWith("To:") -> {
                    to = parseEmailAddressList(line.substringAfter("To:").trim())
                }
                line.startsWith("Cc:") -> {
                    cc = parseEmailAddressList(line.substringAfter("Cc:").trim())
                }
                line.startsWith("Subject:") -> {
                    subject = line.substringAfter("Subject:").trim()
                }
                line.startsWith("Date:") -> {
                    // 简化的日期解析
                    timestamp = try {
                        Instant.parse(line.substringAfter("Date:").trim())
                    } catch (e: Exception) {
                        Instant.DISTANT_PAST
                    }
                }
                line.isEmpty() && !inBody -> {
                    inBody = true
                }
                inBody -> {
                    bodyBuilder.appendLine(line)
                }
            }
        }
        
        bodyPlain = bodyBuilder.toString().trim()
        
        return EmailDto(
            id = generateEmailId(),
            threadId = generateThreadId(subject),
            from = from ?: EmailAddressDto("unknown@example.com"),
            to = to,
            cc = cc,
            subject = subject,
            bodyPlain = bodyPlain,
            bodyHtml = bodyHtml,
            timestamp = timestamp,
            flags = EmailFlags()
        )
    }
    
    /**
     * 解析邮件地址
     */
    private fun parseEmailAddress(text: String): EmailAddressDto {
        // 格式: "Name <email@example.com>" 或 "email@example.com"
        val regex = """(.+?)\s*<(.+?)>""".toRegex()
        val match = regex.find(text)
        
        return if (match != null) {
            val (name, address) = match.destructured
            EmailAddressDto(address.trim(), name.trim())
        } else {
            EmailAddressDto(text.trim())
        }
    }
    
    /**
     * 解析邮件地址列表
     */
    private fun parseEmailAddressList(text: String): MutableList<EmailAddressDto> {
        return text.split(",")
            .map { parseEmailAddress(it.trim()) }
            .toMutableList()
    }
    
    /**
     * 生成邮件 ID
     */
    private fun generateEmailId(): String {
        return "email_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
    
    /**
     * 生成线程 ID
     */
    private fun generateThreadId(subject: String): String {
        // 移除 Re:, Fwd: 等前缀
        val cleanSubject = subject
            .replace(Regex("^(Re:|Fwd:|RE:|FW:)\\s*", RegexOption.IGNORE_CASE), "")
            .trim()
        return "thread_${cleanSubject.hashCode()}"
    }
}
