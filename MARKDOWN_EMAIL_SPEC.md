# Fleur 邮件内容系统技术规范

## 📋 概述

Fleur 邮件客户端支持多种邮件内容格式，提供统一的编辑、预览和显示体验。

## 🎯 核心目标

1. **编辑体验** - Markdown 富文本编辑器
2. **预览效果** - 撰写页面实时预览
3. **详情显示** - 完整支持所有邮件格式
4. **向后兼容** - 支持纯文本、HTML、Markdown

## 📐 邮件内容格式规范

### 1. 支持的格式类型

```kotlin
enum class EmailContentType {
    PLAIN_TEXT,      // 纯文本
    MARKDOWN,        // Markdown 格式
    HTML,            // HTML 邮件
    RICH_TEXT        // 富文本（内部格式）
}
```

### 2. 数据库存储结构

```kotlin
@Entity(tableName = "emails")
data class Email(
    @PrimaryKey val id: String,
    val subject: String,
    val from: String,
    val to: String,
    
    // 内容字段
    val bodyText: String,              // 纯文本内容（必需）
    val bodyMarkdown: String? = null,  // Markdown 源码（可选）
    val bodyHtml: String? = null,      // HTML 内容（可选）
    val contentType: String = "text",  // 内容类型标识
    
    val timestamp: Long,
    val isRead: Boolean = false
)
```

### 3. 内容优先级规则

显示邮件时按以下优先级选择内容：

```
1. bodyHtml (如果存在且非空) → HTML 渲染
2. bodyMarkdown (如果存在且非空) → Markdown 渲染
3. bodyText (兜底) → 纯文本显示
```

## 🛠️ 技术实现方案

### 1. Markdown 编辑器

#### 依赖库选择

```gradle
// build.gradle.kts (app)
dependencies {
    // Markdown 渲染 - 使用 Markwon
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:image:4.6.2")
    implementation("io.noties.markwon:linkify:4.6.2")
    implementation("io.noties.markwon:syntax-highlight:4.6.2")
    implementation("io.noties.markwon:html:4.6.2")
    
    // HTML 解析
    implementation("org.jsoup:jsoup:1.16.1")
    
    // Compose 集成
    implementation("androidx.compose.ui:ui-text:1.5.4")
}
```

#### 编辑器组件

```kotlin
@Composable
fun MarkdownEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    // 支持 Markdown 语法的文本编辑器
    // 实时语法高亮
    // 工具栏快捷操作
}
```

### 2. Markdown 预览组件

```kotlin
@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier
) {
    // 使用 AndroidView 嵌入 Markwon 渲染
    AndroidView(
        factory = { context ->
            TextView(context).apply {
                // Markwon 配置
            }
        },
        update = { textView ->
            // 更新 Markdown 内容
        }
    )
}
```

### 3. HTML 邮件渲染

```kotlin
@Composable
fun HtmlEmailViewer(
    html: String,
    modifier: Modifier = Modifier
) {
    // 使用 WebView 或 Jsoup + AnnotatedString
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false // 安全考虑
                settings.setSupportZoom(true)
            }
        }
    )
}
```

### 4. 统一内容渲染器

```kotlin
@Composable
fun EmailContentRenderer(
    email: Email,
    modifier: Modifier = Modifier
) {
    when {
        !email.bodyHtml.isNullOrBlank() -> {
            HtmlEmailViewer(html = email.bodyHtml)
        }
        !email.bodyMarkdown.isNullOrBlank() -> {
            MarkdownPreview(markdown = email.bodyMarkdown)
        }
        else -> {
            Text(text = email.bodyText)
        }
    }
}
```

## 📝 编辑流程

### 撰写邮件流程

```
1. 用户在编辑器输入 Markdown
   ↓
2. 工具栏按钮插入格式标记
   ↓
3. 实时预览渲染效果（可选）
   ↓
4. 保存时同时生成：
   - bodyMarkdown: 原始 Markdown
   - bodyText: 纯文本（去除格式）
   - bodyHtml: 转换后的 HTML（可选）
```

### 发送邮件处理

```kotlin
fun prepareSendEmail(
    markdown: String
): EmailData {
    return EmailData(
        bodyMarkdown = markdown,
        bodyText = markdown.stripMarkdown(),  // 移除格式标记
        bodyHtml = markdown.toHtml(),         // 转换为 HTML
        contentType = "markdown"
    )
}
```

## 🎨 UI 组件规范

### 1. 编辑器工具栏增强

```kotlin
// 新增 Markdown 专用按钮
- 标题 (H1-H6)
- 粗体 **text**
- 斜体 *text*
- 删除线 ~~text~~
- 引用 > text
- 代码 `code`
- 代码块 ```language
- 链接 [text](url)
- 图片 ![alt](url)
- 列表 - item
- 有序列表 1. item
- 分割线 ---
- 表格
```

### 2. 预览模式切换

```kotlin
enum class EditorMode {
    EDIT,       // 纯编辑
    SPLIT,      // 分屏（编辑 + 预览）
    PREVIEW     // 纯预览
}
```

### 3. 撰写页面预览区域

```kotlin
@Composable
fun ComposeBottomSheet() {
    // ...现有代码
    
    // 正文区域改为：
    if (showFullscreenEditor) {
        FullscreenMarkdownEditor(...)
    } else {
        // 预览卡片
        MarkdownPreviewCard(
            markdown = bodyMarkdown,
            onClick = { showFullscreenEditor = true }
        )
    }
}
```

### 4. 邮件详情页适配

```kotlin
@Composable
fun EmailDetailScreen(email: Email) {
    Scaffold { padding ->
        Column {
            // 头部信息
            EmailHeader(email)
            
            Divider()
            
            // 自适应内容渲染
            EmailContentRenderer(email)
            
            // 操作按钮
            EmailActions()
        }
    }
}
```

## 🔄 内容转换工具

### Markdown 工具类

```kotlin
object MarkdownUtils {
    /**
     * Markdown 转纯文本
     */
    fun String.stripMarkdown(): String {
        return this
            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")  // 粗体
            .replace(Regex("\\*(.+?)\\*"), "$1")         // 斜体
            .replace(Regex("~~(.+?)~~"), "$1")           // 删除线
            .replace(Regex("\\[(.+?)\\]\\(.+?\\)"), "$1") // 链接
            .replace(Regex("^#+\\s+"), "")               // 标题
            .replace(Regex("^>\\s+"), "")                // 引用
            .replace(Regex("`(.+?)`"), "$1")             // 代码
    }
    
    /**
     * Markdown 转 HTML
     */
    fun String.toHtml(): String {
        return Markwon.create(context)
            .toMarkdown(this)
            .toString()
    }
    
    /**
     * HTML 转纯文本
     */
    fun String.htmlToText(): String {
        return Jsoup.parse(this).text()
    }
}
```

## 📊 数据迁移策略

### 现有数据升级

```kotlin
// Migration 策略
class Migration2To3 : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 添加新字段
        database.execSQL(
            "ALTER TABLE emails ADD COLUMN bodyMarkdown TEXT"
        )
        database.execSQL(
            "ALTER TABLE emails ADD COLUMN bodyHtml TEXT"
        )
        database.execSQL(
            "ALTER TABLE emails ADD COLUMN contentType TEXT DEFAULT 'text'"
        )
    }
}
```

## 🎯 实现优先级

### Phase 1: 基础 Markdown 支持
- [ ] 集成 Markwon 依赖
- [ ] 创建 MarkdownPreview 组件
- [ ] 更新数据库 schema
- [ ] 编辑器支持 Markdown 输入

### Phase 2: 编辑器增强
- [ ] 完整的工具栏
- [ ] 实时预览
- [ ] 语法高亮
- [ ] 快捷键支持

### Phase 3: HTML 邮件支持
- [ ] HTML 渲染组件
- [ ] 安全过滤
- [ ] 图片加载
- [ ] 样式适配

### Phase 4: 高级功能
- [ ] 模板系统
- [ ] 草稿自动保存
- [ ] 历史版本
- [ ] 导入导出

## 🔒 安全考虑

1. **HTML 注入防护**
   - 使用 Jsoup 清理 HTML
   - 禁用 JavaScript
   - 过滤危险标签

2. **图片加载**
   - 询问用户是否加载外部图片
   - HTTPS 优先
   - 缓存机制

3. **链接处理**
   - 显示真实 URL
   - 钓鱼警告
   - 外部浏览器打开

## 📱 性能优化

1. **渲染优化**
   - LazyColumn 虚拟滚动
   - 内容缓存
   - 异步渲染

2. **内存管理**
   - 图片压缩
   - WebView 回收
   - 分页加载

## 🧪 测试计划

```kotlin
// 单元测试
class MarkdownUtilsTest {
    @Test
    fun testStripMarkdown() { }
    
    @Test
    fun testToHtml() { }
}

// UI 测试
class EmailDetailScreenTest {
    @Test
    fun testMarkdownRendering() { }
    
    @Test
    fun testHtmlRendering() { }
}
```

## 📚 参考资源

- [Markwon 文档](https://noties.io/Markwon/)
- [Jsoup 文档](https://jsoup.org/)
- [CommonMark 规范](https://commonmark.org/)
- [Material Design 文本编辑](https://m3.material.io/components/text-fields)

---

**文档版本**: 1.0  
**创建日期**: 2025-11-10  
**负责人**: Fleur 开发团队
