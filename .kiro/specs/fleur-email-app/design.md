# Fleur 邮箱应用设计文档

## 概述

Fleur 是一款基于 Kotlin 和 Jetpack Compose 构建的现代化邮箱客户端，采用 Material 3 Extended 设计语言。本设计文档详细描述了应用的架构、组件、数据模型、UI/UX 设计和技术实现方案，旨在打造一款比 Gmail 更优雅、更好用的邮箱应用。

### 设计原则

1. **优雅至上**: 每个界面元素都经过精心设计，使用玻璃拟态、柔和阴影和流畅动效
2. **性能优先**: 采用离线优先架构，确保快速响应和流畅体验
3. **模块化**: 清晰的分层架构，便于维护和扩展
4. **可访问性**: 遵循 WCAG 标准，支持所有用户群体

## 架构设计

### 整体架构

Fleur 采用 Clean Architecture 分层架构，结合 MVVM 模式和单向数据流（UDF）：

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Compose    │  │  ViewModels  │  │  Navigation  │  │
│  │     UI       │◄─┤   (State)    │  │    Graph     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Use Cases  │  │    Models    │  │ Repositories │  │
│  │              │  │              │  │  Interfaces  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │     Room     │  │    WebDAV    │  │  DataStore   │  │
│  │   Database   │  │    Client    │  │  Preferences │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 技术栈

- **UI**: Jetpack Compose + Material 3
- **依赖注入**: Hilt
- **数据库**: Room
- **网络**: OkHttp + Retrofit (WebDAV)
- **异步**: Kotlin Coroutines + Flow
- **图片加载**: Coil
- **后台任务**: WorkManager

## 组件和接口设计

### 1. Presentation Layer

#### 1.1 主题系统 (Theme System)

**FleurTheme Composable**
```kotlin
@Composable
fun FleurTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)
```

**配色方案**

浅色模式 (Light Mode):
- Primary: #1976D2 (深蓝)
- Secondary: #9C27B0 (紫色)
- Background: #F5F5F0 (米白)
- Surface: #FFFFFF (纯白，带玻璃拟态)
- OnSurface: #212121 (深灰)

深色模式 (Dark Mode):
- Primary: #2196F3 (科技蓝)
- Secondary: #00BCD4 (青蓝)
- Background: Gradient(#0A0E1A → #0D1B2A)
- Surface: #1B2838 (深蓝灰)
- OnSurface: #FFFFFF (白色)

**玻璃拟态效果实现**

对于卡片和普通组件:
```kotlin
Modifier.glassmorphism(
    blurRadius = 20.dp,
    backgroundColor = Color.White.copy(alpha = 0.8f),
    borderColor = Color.White.copy(alpha = 0.3f),
    borderWidth = 1.dp
)
```

对于覆盖层组件 (Navigation Drawer, Bottom Sheet, Modal):
```kotlin
// 1. 使用 Modifier.blur() 实现高斯模糊
Modifier
    .blur(radius = 10.dp) // 模糊半径 8-12dp
    .background(
        color = if (darkTheme) {
            Color(0xFF1B2838).copy(alpha = 0.85f)
        } else {
            Color.White.copy(alpha = 0.85f)
        }
    )
    .border(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    )

// 2. 背景遮罩层实现
Box(modifier = Modifier.fillMaxSize()) {
    // 下层内容
    MainContent()
    
    // 毛玻璃遮罩层
    if (showOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .blur(radius = 10.dp)
                .clickable { /* 关闭覆盖层 */ }
        )
        
        // 覆盖层内容 (Drawer/Sheet)
        OverlayContent(
            modifier = Modifier
                .blur(radius = 10.dp)
                .background(surfaceColor.copy(alpha = 0.9f))
        )
    }
}

// 3. 性能优化版本 (使用 RenderEffect)
@RequiresApi(Build.VERSION_CODES.S)
fun Modifier.blurEffect(radius: Float): Modifier = this.then(
    graphicsLayer {
        renderEffect = BlurEffect(radius, radius, Shader.TileMode.CLAMP)
    }
)
```

**毛玻璃效果最佳实践**:
- Navigation Drawer: 10dp blur + 85% opacity
- Bottom Sheet: 12dp blur + 90% opacity  
- Modal Dialog: 8dp blur + 80% opacity
- 背景遮罩: 无 blur + 40% black opacity
- 添加 1dp 半透明边框增强层次感
- Android 12+ 使用 RenderEffect 获得更好性能

**阴影系统**
- Elevation 2dp: blur 8px, opacity 0.08
- Elevation 4dp: blur 12px, opacity 0.12
- Elevation 6dp: blur 16px, opacity 0.16

#### 1.2 导航架构

**Navigation Graph**
```kotlin
sealed class Screen(val route: String) {
    object Inbox : Screen("inbox")
    object EmailDetail : Screen("email/{emailId}")
    object Compose : Screen("compose")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object AccountManagement : Screen("accounts")
}
```

**Adaptive Navigation**
- 宽度 >= 600dp: NavigationDrawer + NavigationRail
- 宽度 < 600dp: BottomNavigationBar

#### 1.3 核心 UI 组件

**EmailListItem**
```kotlin
@Composable
fun EmailListItem(
    email: EmailUiModel,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onSwipeToArchive: () -> Unit,
    onSwipeToDelete: () -> Unit
)
```

特性:
- 玻璃拟态卡片背景
- 悬停时 elevation 提升至 6dp，scale 1.02
- 选中时左侧 4dp 蓝色竖条
- 支持左右滑动手势
- 淡入 + 向上滑动动画 (stagger 50ms)

**MessageBubble (聊天视图)**
```kotlin
@Composable
fun MessageBubble(
    email: EmailUiModel,
    isSent: Boolean,
    showAvatar: Boolean
)
```

特性:
- 发送消息: 右对齐，surfaceVariant 背景
- 接收消息: 左对齐，surface 背景
- 圆角: 16dp (外侧), 4dp (内侧)
- 柔和阴影: elevation 2dp

**FleurCard**
```kotlin
@Composable
fun FleurCard(
    modifier: Modifier = Modifier,
    isHovered: Boolean = false,
    isSelected: Boolean = false,
    content: @Composable () -> Unit
)
```

#### 1.4 动效系统

**过渡动画规范**
- 页面切换: 300ms, FastOutSlowIn
- 视图切换: 300ms, 淡入淡出 + 滑动
- 列表项出现: 200ms fade + 50ms stagger
- 滑动操作: 400ms, DecelerateEasing
- 涟漪效果: 200ms

**Shared Element Transitions**
- 邮件列表 → 详情页: 头像、主题平滑过渡
- 使用 `sharedBounds` 和 `animateContentSize`

### 2. Domain Layer

#### 2.1 核心模型

**Email**
```kotlin
data class Email(
    val id: String,
    val threadId: String,
    val accountId: String,
    val from: EmailAddress,
    val to: List<EmailAddress>,
    val cc: List<EmailAddress>,
    val bcc: List<EmailAddress>,
    val subject: String,
    val bodyPreview: String,
    val bodyHtml: String?,
    val bodyPlain: String,
    val attachments: List<Attachment>,
    val timestamp: Instant,
    val isRead: Boolean,
    val isStarred: Boolean,
    val labels: List<String>
)
```

**EmailThread**
```kotlin
data class EmailThread(
    val id: String,
    val subject: String,
    val participants: List<EmailAddress>,
    val emails: List<Email>,
    val lastMessageTime: Instant,
    val unreadCount: Int
)
```

**Account**
```kotlin
data class Account(
    val id: String,
    val email: String,
    val displayName: String,
    val provider: EmailProvider,
    val color: Color,
    val isDefault: Boolean,
    val webdavConfig: WebDAVConfig
)
```

**WebDAVConfig**
```kotlin
data class WebDAVConfig(
    val serverUrl: String,
    val port: Int,
    val username: String,
    val useSsl: Boolean,
    val calendarPath: String?,
    val contactsPath: String?
)
```

#### 2.2 Repository 接口

**EmailRepository**
```kotlin
interface EmailRepository {
    fun getEmails(accountId: String?, page: Int): Flow<Result<List<Email>>>
    fun getEmailThread(threadId: String): Flow<Result<EmailThread>>
    fun searchEmails(query: String, filters: SearchFilters): Flow<Result<List<Email>>>
    suspend fun sendEmail(email: Email): Result<Unit>
    suspend fun deleteEmail(emailId: String): Result<Unit>
    suspend fun archiveEmail(emailId: String): Result<Unit>
    suspend fun markAsRead(emailId: String, isRead: Boolean): Result<Unit>
    suspend fun syncEmails(accountId: String): Result<SyncResult>
}
```

**AccountRepository**
```kotlin
interface AccountRepository {
    fun getAccounts(): Flow<List<Account>>
    suspend fun addAccount(account: Account, password: String): Result<Unit>
    suspend fun updateAccount(account: Account): Result<Unit>
    suspend fun deleteAccount(accountId: String): Result<Unit>
    suspend fun verifyAccount(config: WebDAVConfig, password: String): Result<Boolean>
}
```

#### 2.3 Use Cases

**GetEmailsUseCase**
```kotlin
class GetEmailsUseCase(
    private val emailRepository: EmailRepository
) {
    operator fun invoke(
        accountId: String? = null,
        page: Int = 0
    ): Flow<Result<List<Email>>> = emailRepository.getEmails(accountId, page)
}
```

**SyncEmailsUseCase**
```kotlin
class SyncEmailsUseCase(
    private val emailRepository: EmailRepository,
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(): Result<SyncResult> {
        // 获取所有账户并逐个同步
        // 使用增量同步策略
        // 处理冲突（服务器优先）
    }
}
```

### 3. Data Layer

#### 3.1 Room 数据库

**EmailEntity**
```kotlin
@Entity(
    tableName = "emails",
    indices = [
        Index("account_id"),
        Index("thread_id"),
        Index("timestamp"),
        Index("is_read")
    ]
)
data class EmailEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val accountId: String,
    val fromAddress: String,
    val fromName: String,
    val toAddresses: String, // JSON
    val subject: String,
    val bodyPreview: String,
    val bodyHtml: String?,
    val bodyPlain: String,
    val timestamp: Long,
    val isRead: Boolean,
    val isStarred: Boolean,
    val labels: String // JSON
)
```

**EmailDao**
```kotlin
@Dao
interface EmailDao {
    @Query("SELECT * FROM emails WHERE account_id = :accountId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getEmailsPaged(accountId: String, limit: Int, offset: Int): Flow<List<EmailEntity>>
    
    @Query("SELECT * FROM emails WHERE thread_id = :threadId ORDER BY timestamp ASC")
    fun getEmailThread(threadId: String): Flow<List<EmailEntity>>
    
    @Query("SELECT * FROM emails WHERE subject LIKE :query OR body_plain LIKE :query")
    fun searchEmails(query: String): Flow<List<EmailEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailEntity>)
    
    @Query("DELETE FROM emails WHERE id = :emailId")
    suspend fun deleteEmail(emailId: String)
}
```

#### 3.2 WebDAV 客户端

**WebDAVClient**
```kotlin
interface WebDAVClient {
    suspend fun connect(config: WebDAVConfig, password: String): Result<Unit>
    suspend fun fetchEmails(since: Instant?): Result<List<EmailDto>>
    suspend fun sendEmail(email: EmailDto): Result<Unit>
    suspend fun deleteEmail(emailId: String): Result<Unit>
    suspend fun updateEmailFlags(emailId: String, flags: EmailFlags): Result<Unit>
}
```

**实现细节**
- 使用 OkHttp 配置 SSL/TLS
- 实现 XML 解析器处理 WebDAV 响应
- 连接池: 最大 5 个连接
- 超时: 连接 10s, 读取 30s, 写入 30s
- 重试策略: 指数退避，最多 3 次

#### 3.3 安全存储

**SecureCredentialStorage**
```kotlin
class SecureCredentialStorage(context: Context) {
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "fleur_credentials",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun savePassword(accountId: String, password: String)
    fun getPassword(accountId: String): String?
    fun deletePassword(accountId: String)
}
```

### 4. 状态管理

#### 4.1 UI State 模式

**InboxUiState**
```kotlin
data class InboxUiState(
    val emails: List<EmailUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: FleurError? = null,
    val viewMode: ViewMode = ViewMode.LIST,
    val selectedAccount: String? = null,
    val isRefreshing: Boolean = false,
    val hasMorePages: Boolean = true
)

enum class ViewMode {
    LIST,    // 传统列表视图
    CHAT     // 聊天气泡视图
}
```

**InboxViewModel**
```kotlin
@HiltViewModel
class InboxViewModel @Inject constructor(
    private val getEmailsUseCase: GetEmailsUseCase,
    private val syncEmailsUseCase: SyncEmailsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()
    
    fun loadEmails(page: Int = 0)
    fun refreshEmails()
    fun switchViewMode(mode: ViewMode)
    fun filterByAccount(accountId: String?)
}
```

### 5. 离线优先策略

**数据同步流程**
```
1. UI 请求数据
   ↓
2. Repository 立即返回本地缓存 (Room)
   ↓
3. 后台触发远程同步 (WebDAV)
   ↓
4. 同步完成后更新本地数据库
   ↓
5. Flow 自动通知 UI 更新
```

**离线操作队列**
```kotlin
data class PendingOperation(
    val id: String,
    val type: OperationType,
    val emailId: String,
    val timestamp: Instant,
    val retryCount: Int = 0
)

enum class OperationType {
    SEND_EMAIL,
    DELETE_EMAIL,
    ARCHIVE_EMAIL,
    MARK_READ
}
```

网络恢复时，WorkManager 执行队列中的操作。

## UI/UX 详细设计

### 1. 收件箱界面 (Inbox Screen)

**布局结构**
```
┌─────────────────────────────────────────┐
│  [☰]  [搜索邮件_______________] [👤]   │ TopAppBar (Chrome 风格)
├─────────────────────────────────────────┤
│  [所有账户 ▼]  [列表/聊天切换]          │ Filter Bar
├─────────────────────────────────────────┤
│  ┌───────────────────────────────────┐  │
│  │ 📧 Alice Chen                     │  │
│  │ 项目进度更新                      │  │
│  │ 这是邮件预览文本...        2小时前 │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │ 📧 Bob Wang                       │  │
│  │ Re: 会议安排                      │  │
│  │ 好的，我会准时参加...      昨天   │  │
│  └───────────────────────────────────┘  │
│                 ...                      │
└─────────────────────────────────────────┘
│  [✏️]                                    │ FAB
└─────────────────────────────────────────┘
```

**TopAppBar 交互细节**
- 搜索框: 类似 Chrome 的圆角搜索框，占据大部分宽度
- 点击搜索框: 展开为全屏搜索界面，显示搜索历史和过滤器
- 头像: 显示当前默认账户头像，点击跳转到账户管理页面
- 菜单按钮: 打开 Navigation Drawer

**邮件列表交互细节**
- 下拉刷新: 显示 Material 3 CircularProgressIndicator
- 滚动到底部: 自动加载下一页 (50 封/页)
- 点击邮件: 300ms 过渡动画进入详情页
- 长按邮件: 进入多选模式，显示 Checkbox
- 右滑: 绿色背景 + 归档图标
- 左滑: 红色背景 + 删除图标

**视觉效果**
- 浅色模式: 玻璃拟态卡片，20px blur，白色 80% opacity
- 深色模式: 深蓝灰卡片 (#1B2838)，悬停时蓝色高光边框
- 未读邮件: 主题文字加粗，左侧蓝色圆点
- 选中邮件: 左侧 4dp 蓝色竖条

### 2. 聊天视图 (Chat View)

**布局结构**
```
┌─────────────────────────────────────────┐
│  [←]  与 Alice Chen 的对话              │ TopAppBar
├─────────────────────────────────────────┤
│                                          │
│  ┌──────────────────────┐               │
│  │ 你好，项目进展如何？  │ 10:30 AM     │ (接收)
│  └──────────────────────┘               │
│                                          │
│               ┌──────────────────────┐  │
│     9:45 AM   │ 进展顺利，预计本周完成 │  │ (发送)
│               └──────────────────────┘  │
│                                          │
│  ┌──────────────────────┐               │
│  │ 太好了！             │ 昨天          │
│  └──────────────────────┘               │
│                                          │
└─────────────────────────────────────────┘
```

**交互细节**
- 使用 LazyColumn(reverseLayout = true) 实现
- 发送消息: 右对齐，主色调背景
- 接收消息: 左对齐，surface 背景
- 点击消息: 展开显示完整邮件头信息
- 长按消息: 显示操作菜单 (回复、转发、复制)

**视觉效果**
- 气泡圆角: 外侧 16dp，内侧 4dp
- 阴影: elevation 2dp，柔和模糊
- 时间戳: 12sp，次要文本颜色
- 头像: 40dp 圆形，Material You 动态配色

### 3. 邮件详情页 (Email Detail Screen)

**布局结构**
```
┌─────────────────────────────────────────┐
│  [←]  [⭐] [📁] [🗑️]                    │ TopAppBar
├─────────────────────────────────────────┤
│  👤 Alice Chen <alice@example.com>      │
│  收件人: me@example.com                  │
│  2024-01-15 14:30                        │
│                                          │
│  项目进度更新                            │
│  ─────────────────────────────────────  │
│                                          │
│  大家好，                                │
│                                          │
│  本周项目进展顺利，主要完成了...         │
│                                          │
│  📎 report.pdf (2.3 MB)                 │
│  📎 screenshot.png (450 KB)             │
│                                          │
├─────────────────────────────────────────┤
│  [回复]  [全部回复]  [转发]             │ Action Bar
└─────────────────────────────────────────┘
```

**交互细节**
- Shared Element Transition: 头像和主题从列表平滑过渡
- HTML 渲染: 使用 AndroidView + WebView，注入自定义 CSS
- 附件点击: 显示 Bottom Sheet 选择预览或下载
- 回复按钮: 滑入撰写界面，自动填充收件人和主题

**视觉效果**
- 发件人信息: 卡片样式，玻璃拟态背景
- 邮件正文: 16sp，行高 1.5，舒适阅读
- 附件卡片: 圆角 12dp，图标 + 文件名 + 大小
- 操作按钮: Filled Tonal Button，间距 8dp

### 4. 撰写邮件界面 (Compose Screen)

**布局结构**
```
┌─────────────────────────────────────────┐
│  [×]  撰写邮件                [发送]    │ TopAppBar
├─────────────────────────────────────────┤
│  发件人: me@example.com [▼]             │
│  ─────────────────────────────────────  │
│  收件人: _____________________________  │
│  抄送: _______________________________  │
│  主题: _______________________________  │
│  ─────────────────────────────────────  │
│  [B] [I] [U] [•] [1.]                   │ Format Bar
│  ─────────────────────────────────────  │
│                                          │
│  邮件正文...                             │
│                                          │
│                                          │
│  ─────────────────────────────────────  │
│  📎 attachment.pdf (1.2 MB)  [×]        │
└─────────────────────────────────────────┘
│  [📎]                                    │ Bottom Bar
└─────────────────────────────────────────┘
```

**交互细节**
- 发件人选择: 点击显示 Modal Bottom Sheet，列出所有账户
- 富文本编辑: 工具栏按钮切换格式，使用 AnnotatedString
- 附件添加: 使用 ActivityResultContract 选择文件
- 自动保存: 每 30 秒或停止输入 3 秒后保存草稿
- 发送验证: 检查收件人格式，附件大小限制

**视觉效果**
- 输入框: OutlinedTextField，Material 3 样式
- 格式工具栏: IconButton，选中时主色调背景
- 附件卡片: 横向 LazyRow，可滑动删除
- 发送按钮: 填充时启用，空时禁用

### 5. 搜索界面 (Search Screen)

**布局结构**
```
┌─────────────────────────────────────────┐
│  [←] [搜索邮件________________] [×]     │ SearchBar
├─────────────────────────────────────────┤
│  最近搜索:                               │
│  [项目报告] [会议纪要] [发票]           │ Chips
│                                          │
│  过滤器:                                 │
│  [日期 ▼] [发件人 ▼] [账户 ▼] [附件]   │ FilterChips
├─────────────────────────────────────────┤
│  搜索结果 (23)                           │
│  ┌───────────────────────────────────┐  │
│  │ 📧 Alice Chen                     │  │
│  │ 项目进度更新                      │  │
│  │ ...项目进展顺利...         2小时前 │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

**交互细节**
- 搜索防抖: 300ms 延迟后触发搜索
- 关键词高亮: 使用 AnnotatedString + SpanStyle
- 过滤器: 点击显示 Dropdown Menu 或 Date Picker
- 搜索历史: 点击 Chip 快速搜索，长按删除

**视觉效果**
- SearchBar: Material 3 组件，自动展开/收起
- 高亮文本: 主色调背景，白色文字
- FilterChip: 选中时填充主色调
- 空状态: 插图 + 提示文字

### 6. 账户管理界面 (Account Management)

**布局结构**
```
┌─────────────────────────────────────────┐
│  [←]  账户管理                          │ TopAppBar
├─────────────────────────────────────────┤
│  ┌───────────────────────────────────┐  │
│  │ 🔵 me@example.com                 │  │
│  │    个人邮箱                  [⚙️] │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │ 🟢 work@company.com               │  │
│  │    工作邮箱 (默认)          [⚙️] │  │
│  └───────────────────────────────────┘  │
│                                          │
└─────────────────────────────────────────┘
│  [+]                                     │ FAB
└─────────────────────────────────────────┘
```

**添加账户流程**
```
1. 输入邮箱地址
   ↓
2. 自动检测服务器配置 (WebDAV)
   ↓
3. 输入密码
   ↓
4. 验证连接 (显示进度)
   ↓
5. 设置显示名称和颜色
   ↓
6. 完成添加，开始同步
```

**交互细节**
- 账户卡片: 显示邮箱、名称、颜色指示器
- 点击设置: 进入账户详情页，可编辑配置
- 删除账户: 显示 AlertDialog 确认
- 默认账户: 显示星标图标

### 7. Navigation Drawer

**布局结构**
```
┌─────────────────────────┐
│  👤 User Name            │ Header
│  me@example.com          │
├─────────────────────────┤
│  📥 收件箱         (23) │
│  📤 已发送              │
│  📝 草稿箱         (2)  │
│  ⭐ 星标邮件            │
│  📁 归档                │
│  🗑️ 垃圾箱              │
├─────────────────────────┤
│  账户                   │
│  🔵 个人邮箱      (15)  │
│  🟢 工作邮箱      (8)   │
├─────────────────────────┤
│  ⚙️ 设置                │
│  ℹ️ 关于                │
└─────────────────────────┘
```

**交互细节**
- 滑入动画: 250ms，DecelerateEasing
- 遮罩层: 黑色 40% opacity
- 选中项: 主色调背景，圆角 12dp
- 未读数: 主色调 Badge

**视觉效果**
- 宽度: 280dp (手机), 360dp (平板)
- 背景: 浅色模式玻璃拟态，深色模式 surface 颜色
- 分隔线: onSurface 12% opacity
- 图标: 24dp，onSurface 颜色

### 8. Modal Bottom Sheet

**使用场景**
1. 邮件操作菜单 (回复、转发、归档、删除、标记)
2. 选择发件账户
3. 附件预览选项 (打开、下载、分享)
4. 搜索过滤器详细设置

**布局示例 (邮件操作)**
```
┌─────────────────────────────────────────┐
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  │ Handle
│                                          │
│  📧 邮件操作                             │
│                                          │
│  [↩️] 回复                               │
│  [↪️] 转发                               │
│  [📁] 归档                               │
│  [⭐] 标记星标                           │
│  [🗑️] 删除                               │
│                                          │
└─────────────────────────────────────────┘
```

**交互细节**
- 滑入动画: 300ms，DecelerateEasing
- 拖拽关闭: 支持手势下滑
- 背景遮罩: 黑色 50% opacity
- 项目点击: 涟漪效果 + 200ms 延迟关闭

## 动效设计规范

### 1. 动画时长

| 动画类型 | 时长 | 缓动曲线 |
|---------|------|---------|
| 微交互 (涟漪、按钮) | 150-200ms | FastOutSlowIn |
| 页面过渡 | 300ms | FastOutSlowIn |
| 列表项出现 | 200ms + 50ms stagger | DecelerateEasing |
| 滑动操作 | 400ms | DecelerateEasing |
| Bottom Sheet | 300ms | DecelerateEasing |
| Navigation Drawer | 250ms | DecelerateEasing |
| 主题切换 | 600ms | FastOutSlowIn |

### 2. 关键动画实现

**列表项 Stagger 动画**
```kotlin
LazyColumn {
    itemsIndexed(emails) { index, email ->
        EmailListItem(
            email = email,
            modifier = Modifier.animateEnterExit(
                enter = fadeIn(animationSpec = tween(200, delayMillis = index * 50)) +
                        slideInVertically(
                            animationSpec = tween(200, delayMillis = index * 50),
                            initialOffsetY = { it / 4 }
                        )
            )
        )
    }
}
```

**视图切换动画**
```kotlin
AnimatedContent(
    targetState = viewMode,
    transitionSpec = {
        fadeIn(tween(300)) + slideInHorizontally() with
        fadeOut(tween(300)) + slideOutHorizontally()
    }
) { mode ->
    when (mode) {
        ViewMode.LIST -> EmailListView()
        ViewMode.CHAT -> EmailChatView()
    }
}
```

**卡片悬停动画**
```kotlin
val elevation by animateDpAsState(
    targetValue = if (isHovered) 6.dp else 2.dp,
    animationSpec = tween(150)
)
val scale by animateFloatAsState(
    targetValue = if (isHovered) 1.02f else 1f,
    animationSpec = tween(150)
)
```

## 性能优化策略

### 1. Compose 优化

- 使用 `@Stable` 和 `@Immutable` 注解标记数据类
- 为 LazyColumn items 提供稳定的 key
- 使用 `derivedStateOf` 避免不必要的重组
- 使用 `remember` 缓存计算结果
- 避免在 Composable 中创建新对象

### 2. 数据库优化

- 为常用查询字段添加索引
- 使用分页查询 (Paging 3)
- 邮件正文懒加载 (列表只加载预览)
- 定期清理 30 天前的缓存邮件

### 3. 图片加载优化

- 使用 Coil 配置内存缓存 (25% 可用内存)
- 磁盘缓存 (100MB)
- 图片懒加载 (仅加载可见区域)
- 头像使用占位符和渐进式加载

### 4. 网络优化

- 使用 OkHttp 连接池
- 实现请求去重
- 增量同步 (仅获取新邮件)
- 压缩传输数据

## 错误处理策略

### 1. 错误类型

```kotlin
sealed class FleurError {
    data class NetworkError(val message: String) : FleurError()
    data class AuthError(val accountId: String) : FleurError()
    data class SyncError(val reason: String) : FleurError()
    data class StorageError(val availableSpace: Long) : FleurError()
    data class ValidationError(val field: String) : FleurError()
}
```

### 2. 错误展示

- 网络错误: Snackbar + 重试按钮
- 认证错误: AlertDialog + 重新登录
- 同步错误: 后台记录日志 + 指数退避重试
- 存储错误: AlertDialog + 清理缓存选项
- 验证错误: TextField 下方错误提示

### 3. 重试策略

```kotlin
suspend fun <T> retryWithExponentialBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    block: suspend () -> T
): Result<T>
```

## 测试策略

### 1. 单元测试

- Domain Layer: Use Cases 业务逻辑测试
- Data Layer: Repository 实现测试
- ViewModel: 状态管理测试

### 2. UI 测试

- Compose UI 测试: 使用 ComposeTestRule
- 导航测试: 验证页面跳转
- 交互测试: 点击、滑动、长按

### 3. 集成测试

- 端到端流程测试
- 离线模式测试
- 多账户切换测试

## 可访问性设计

### 1. 内容描述

所有交互元素添加 `contentDescription`:
```kotlin
IconButton(
    onClick = { /* ... */ },
    modifier = Modifier.semantics {
        contentDescription = "发送邮件"
    }
)
```

### 2. 触摸目标

最小尺寸 48dp × 48dp:
```kotlin
Modifier.size(48.dp)
```

### 3. 颜色对比度

- 正文文本: 至少 4.5:1
- 大文本 (18sp+): 至少 3:1
- 使用工具验证: WebAIM Contrast Checker

### 4. 屏幕阅读器

- 使用语义化组件
- 合理的阅读顺序
- 测试 TalkBack 兼容性

## 安全设计

### 1. 凭证存储

- 使用 EncryptedSharedPreferences
- Android Keystore 管理密钥
- 不在日志中输出敏感信息

### 2. 网络安全

- 强制 HTTPS
- 证书固定 (Certificate Pinning)
- 禁用不安全的 SSL/TLS 版本

### 3. 数据保护

- 邮件正文加密存储 (可选)
- 应用锁 (生物识别或 PIN)
- 自动锁定超时

## 总结

Fleur 邮箱应用的设计遵循 Material 3 Extended 设计规范，采用 Clean Architecture 和 MVVM 模式，实现了优雅的 UI/UX、流畅的动效和可靠的性能。通过玻璃拟态设计、柔和阴影和精心设计的动画，Fleur 将提供比 Gmail 更清爽、更优雅的用户体验。
