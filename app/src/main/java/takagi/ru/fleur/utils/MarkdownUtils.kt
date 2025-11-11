package takagi.ru.fleur.utils

import android.content.Context
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

/**
 * Markdown 处理工具类
 */
object MarkdownUtils {
    
    /**
     * 创建 Markwon 实例
     */
    fun createMarkwon(context: Context): Markwon {
        return Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .build()
    }
    
    /**
     * Markdown 转纯文本
     * 移除所有格式标记
     */
    fun String.stripMarkdown(): String {
        return this
            // 移除粗体 **text** 或 __text__
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
            .replace(Regex("__(.+?)__"), "$1")
            // 移除斜体 *text* 或 _text_
            .replace(Regex("\\*(.+?)\\*"), "$1")
            .replace(Regex("_(.+?)_"), "$1")
            // 移除删除线 ~~text~~
            .replace(Regex("~~(.+?)~~"), "$1")
            // 移除链接 [text](url)
            .replace(Regex("\\[(.+?)\\]\\(.+?\\)"), "$1")
            // 移除图片 ![alt](url)
            .replace(Regex("!\\[.*?\\]\\(.+?\\)"), "")
            // 移除标题 # text
            .replace(Regex("^#{1,6}\\s+(.*)$", RegexOption.MULTILINE), "$1")
            // 移除引用 > text
            .replace(Regex("^>\\s+(.*)$", RegexOption.MULTILINE), "$1")
            // 移除行内代码 `code`
            .replace(Regex("`(.+?)`"), "$1")
            // 移除代码块 ```code```
            .replace(Regex("```[\\s\\S]*?```"), "")
            // 移除列表标记
            .replace(Regex("^[*\\-+]\\s+", RegexOption.MULTILINE), "")
            .replace(Regex("^\\d+\\.\\s+", RegexOption.MULTILINE), "")
            // 移除分割线
            .replace(Regex("^[-*_]{3,}$", RegexOption.MULTILINE), "")
            // 清理多余空行
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
    
    /**
     * Markdown 转 HTML
     * 使用基本的正则表达式进行转换
     */
    fun String.markdownToHtml(): String {
        // 简单的 Markdown 到 HTML 转换
        // 由于 Markwon 主要用于 Android 显示，这里使用基本转换规则
        var html = this
        
        // 标题
        html = html.replace(Regex("^######\\s+(.*)$", RegexOption.MULTILINE), "<h6>$1</h6>")
        html = html.replace(Regex("^#####\\s+(.*)$", RegexOption.MULTILINE), "<h5>$1</h5>")
        html = html.replace(Regex("^####\\s+(.*)$", RegexOption.MULTILINE), "<h4>$1</h4>")
        html = html.replace(Regex("^###\\s+(.*)$", RegexOption.MULTILINE), "<h3>$1</h3>")
        html = html.replace(Regex("^##\\s+(.*)$", RegexOption.MULTILINE), "<h2>$1</h2>")
        html = html.replace(Regex("^#\\s+(.*)$", RegexOption.MULTILINE), "<h1>$1</h1>")
        
        // 粗体
        html = html.replace(Regex("\\*\\*(.+?)\\*\\*"), "<strong>$1</strong>")
        html = html.replace(Regex("__(.+?)__"), "<strong>$1</strong>")
        
        // 斜体
        html = html.replace(Regex("\\*(.+?)\\*"), "<em>$1</em>")
        html = html.replace(Regex("_(.+?)_"), "<em>$1</em>")
        
        // 删除线
        html = html.replace(Regex("~~(.+?)~~"), "<del>$1</del>")
        
        // 链接
        html = html.replace(Regex("\\[(.+?)\\]\\((.+?)\\)"), "<a href=\"$2\">$1</a>")
        
        // 图片
        html = html.replace(Regex("!\\[(.+?)\\]\\((.+?)\\)"), "<img src=\"$2\" alt=\"$1\" />")
        
        // 行内代码
        html = html.replace(Regex("`(.+?)`"), "<code>$1</code>")
        
        // 代码块
        html = html.replace(Regex("```([\\s\\S]*?)```"), "<pre><code>$1</code></pre>")
        
        // 引用
        html = html.replace(Regex("^>\\s+(.*)$", RegexOption.MULTILINE), "<blockquote>$1</blockquote>")
        
        // 无序列表
        html = html.replace(Regex("^[*\\-+]\\s+(.*)$", RegexOption.MULTILINE), "<li>$1</li>")
        
        // 有序列表
        html = html.replace(Regex("^\\d+\\.\\s+(.*)$", RegexOption.MULTILINE), "<li>$1</li>")
        
        // 分割线
        html = html.replace(Regex("^[-*_]{3,}$", RegexOption.MULTILINE), "<hr />")
        
        // 换行
        html = html.replace("\n", "<br />\n")
        
        return "<html><body>$html</body></html>"
    }
    
    /**
     * HTML 转纯文本
     */
    fun String.htmlToText(): String {
        return Jsoup.parse(this).text()
    }
    
    /**
     * 清理不安全的 HTML
     */
    fun String.sanitizeHtml(): String {
        return Jsoup.clean(
            this,
            Safelist.relaxed()
                .addTags("img", "video", "audio")
                .addAttributes("img", "src", "alt", "width", "height")
                .addAttributes("video", "src", "controls", "width", "height")
                .addAttributes("audio", "src", "controls")
                .addProtocols("img", "src", "http", "https", "data")
                .addProtocols("video", "src", "http", "https")
                .addProtocols("audio", "src", "http", "https")
        )
    }
    
    /**
     * 检测内容类型
     */
    fun detectContentType(content: String): ContentType {
        return when {
            content.trim().startsWith("<") && content.trim().endsWith(">") -> ContentType.HTML
            content.contains(Regex("[*_#`\\[\\]>]")) -> ContentType.MARKDOWN
            else -> ContentType.PLAIN_TEXT
        }
    }
    
    /**
     * 获取纯文本预览（限制长度）
     */
    fun String.getPreview(maxLength: Int = 100): String {
        val text = when (detectContentType(this)) {
            ContentType.HTML -> htmlToText()
            ContentType.MARKDOWN -> stripMarkdown()
            ContentType.PLAIN_TEXT -> this
        }
        return if (text.length > maxLength) {
            text.take(maxLength) + "..."
        } else {
            text
        }
    }
}

/**
 * 内容类型枚举
 */
enum class ContentType {
    PLAIN_TEXT,
    MARKDOWN,
    HTML
}
