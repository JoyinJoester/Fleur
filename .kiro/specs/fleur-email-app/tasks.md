# Fleur 邮箱应用实现任务列表

## 任务概述

本任务列表将 Fleur 邮箱应用的设计转化为可执行的代码实现步骤。每个任务都是增量式的，确保代码逐步集成，没有孤立或未连接的代码。

---

## 任务列表

- [x] 1. 项目初始化和依赖配置





  - 配置 Gradle 文件，添加 Jetpack Compose、Material 3、Room、Hilt、Coroutines、OkHttp、Coil、WorkManager 依赖
  - 创建模块化包结构：`ui`、`domain`、`data` 三层架构
  - 配置 Hilt 依赖注入，创建 Application 类
  - 配置 Compose 编译器和 Kotlin 版本
  - _需求: 2.1, 5.1, 12.1_




- [x] 2. 实现 Domain 层核心模型


  - 创建 `Email` 数据类，包含 id、threadId、accountId、from、to、subject、body、attachments、timestamp、flags
  - 创建 `EmailThread` 数据类，包含线程信息和邮件列表
  - 创建 `Account` 数据类，包含账户信息和 WebDAV 配置
  - 创建 `WebDAVConfig` 数据类
  - 创建 `Attachment` 数据类
  - 创建 `EmailAddress` 数据类
  - 创建 `FleurError` sealed class 及其子类
  - _需求: 5.1, 9.1_

- [x] 3. 定义 Repository 接口



  - 创建 `EmailRepository` 接口，定义邮件 CRUD 和同步方法
  - 创建 `AccountRepository` 接口，定义账户管理方法
  - 创建 `PreferencesRepository` 接口，定义设置存储方法
  - 所有方法返回 `Flow<Result<T>>` 或 `Result<T>`
  - _需求: 5.1, 9.1, 11.1_

- [x] 4. 实现 Material 3 主题系统




- [x] 4.1 创建配色方案

  - 定义浅色模式 ColorScheme：Primary #1976D2, Background #F5F5F0, Surface #FFFFFF
  - 定义深色模式 ColorScheme：Primary #2196F3, Background Gradient(#0A0E1A → #0D1B2A), Surface #1B2838
  - 创建 `FleurColorScheme` 对象
  - _需求: 2.2_



- [x] 4.2 实现玻璃拟态效果

  - 创建 `Modifier.glassmorphism()` 扩展函数，实现 20dp blur + 80% opacity
  - 创建 `Modifier.blurEffect()` 扩展函数，用于覆盖层组件（8-12dp blur）
  - 实现背景遮罩层组件 `BlurredScrim`
  - _需求: 2.3, 3.4_


- [x] 4.3 配置 Typography 和阴影

  - 创建 `FleurTypography`，定义文本样式
  - 定义柔和阴影系统：2dp/8px blur, 4dp/12px blur, 6dp/16px blur
  - 创建 `FleurTheme` Composable，整合所有主题元素
  - 支持动态配色（Android 12+）
  - _需求: 2.1, 2.4, 2.5_

- [x] 5. 实现核心 UI 组件




- [x] 5.1 创建 FleurCard 组件

  - 实现 `FleurCard` Composable，支持 hover 和 selected 状态
  - Hover 时 elevation 提升至 6dp，scale 1.02，动画 150ms
  - Selected 时左侧 4dp 蓝色竖条
  - 浅色模式应用玻璃拟态，深色模式使用 surface 颜色
  - _需求: 2.3, 2.4, 4.4_


- [x] 5.2 创建 EmailListItem 组件

  - 实现 `EmailListItem` Composable，显示发件人、主题、预览、时间戳
  - 使用 `FleurCard` 作为容器
  - 未读邮件：主题加粗，左侧蓝色圆点
  - 支持点击、长按事件
  - _需求: 1.1, 4.1_


- [x] 5.3 创建 MessageBubble 组件


  - 实现 `MessageBubble` Composable，用于聊天视图
  - 发送消息：右对齐，surfaceVariant 背景，外侧圆角 16dp
  - 接收消息：左对齐，surface 背景，外侧圆角 16dp
  - 显示头像、时间戳
  - _需求: 1.2, 1.5_

- [x] 6. 实现动效系统



  - 创建动画常量：时长、缓动曲线
  - 实现列表项 stagger 动画：200ms fade + slide，50ms 延迟
  - 实现视图切换动画：300ms fade + slide
  - 实现卡片悬停动画：150ms elevation + scale
  - 创建 `AnimatedListItem` Composable 封装动画逻辑
  - _需求: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 7. 实现 Room 数据库层




- [x] 7.1 创建数据库实体

  - 创建 `EmailEntity`，添加索引：account_id, thread_id, timestamp, is_read
  - 创建 `AccountEntity`
  - 创建 `AttachmentEntity`
  - 创建 `PendingOperationEntity`（离线操作队列）
  - _需求: 11.1, 12.2_



- [x] 7.2 创建 DAO 接口

  - 创建 `EmailDao`，实现分页查询、线程查询、搜索、CRUD 操作
  - 创建 `AccountDao`
  - 创建 `AttachmentDao`
  - 创建 `PendingOperationDao`
  - 所有查询返回 `Flow<T>`
  - _需求: 7.1, 7.2, 11.1, 12.2_


- [x] 7.3 配置数据库和 Mapper

  - 创建 `FleurDatabase` 抽象类，配置版本和实体
  - 实现 Entity ↔ Domain Model 的 Mapper 类
  - 在 Hilt Module 中提供数据库和 DAO 实例
  - _需求: 11.1_

- [x] 8. 实现安全凭证存储



  - 创建 `SecureCredentialStorage` 类
  - 使用 `EncryptedSharedPreferences` 和 Android Keystore
  - 实现 `savePassword()`, `getPassword()`, `deletePassword()` 方法
  - _需求: 5.5_

- [x] 9. 实现 WebDAV 客户端




- [x] 9.1 创建 WebDAV 接口和 DTO


  - 定义 `WebDAVClient` 接口
  - 创建 `EmailDto`, `WebDAVResponse` 数据类
  - 定义 XML 解析器接口
  - _需求: 5.1, 5.2_

- [x] 9.2 实现 WebDAV 客户端


  - 使用 OkHttp 实现 `WebDAVClientImpl`
  - 配置 SSL/TLS、连接池（最大 5 个）、超时（连接 10s，读写 30s）
  - 实现 `connect()`, `fetchEmails()`, `sendEmail()`, `deleteEmail()` 方法
  - 实现 XML 解析器处理 WebDAV 响应
  - 实现指数退避重试策略（最多 3 次）
  - _需求: 5.1, 5.2, 5.3, 5.4_

- [x] 10. 实现 Repository 层


- [x] 10.1 实现 EmailRepository


  - 创建 `EmailRepositoryImpl`，协调本地和远程数据源
  - 实现离线优先策略：立即返回本地数据，后台同步远程
  - 实现分页加载（每页 50 封）
  - 实现增量同步（根据最后同步时间）
  - 实现搜索功能（全文搜索 + 过滤器）
  - _需求: 7.1, 7.2, 11.1, 11.2, 12.2_

- [x] 10.2 实现 AccountRepository


  - 创建 `AccountRepositoryImpl`
  - 集成 `SecureCredentialStorage` 存储密码
  - 实现账户 CRUD 操作
  - 实现账户验证（调用 WebDAV connect）
  - _需求: 9.1, 9.2, 9.4_



- [x] 10.3 实现 PreferencesRepository

  - 使用 DataStore 实现 `PreferencesRepositoryImpl`
  - 存储视图模式、主题偏好、同步间隔、通知设置
  - 提供 Flow 接口监听设置变化
  - _需求: 1.4, 10.3_

- [x] 11. 实现 Domain 层 Use Cases



  - 创建 `GetEmailsUseCase`：获取邮件列表
  - 创建 `GetEmailThreadUseCase`：获取邮件线程
  - 创建 `SendEmailUseCase`：发送邮件
  - 创建 `SyncEmailsUseCase`：同步所有账户邮件
  - 创建 `SearchEmailsUseCase`：搜索邮件（300ms 防抖）
  - 创建 `ManageAccountUseCase`：管理账户
  - 创建 `DeleteEmailUseCase`, `ArchiveEmailUseCase`, `MarkAsReadUseCase`
  - _需求: 1.1, 5.1, 5.3, 7.1, 7.2, 8.1, 9.1_

- [x] 12. 实现导航架构



  - 定义 `Screen` sealed class，包含所有路由
  - 创建 `NavGraph` Composable，配置 Navigation Compose
  - 实现参数传递（emailId, threadId）
  - 创建 `NavigationActions` 类封装导航操作
  - 实现页面过渡动画（300ms）
  - _需求: 1.1, 4.1_

- [x] 13. 实现收件箱界面

- [x] 13.1 创建 InboxViewModel


  - 定义 `InboxUiState` 数据类（emails, isLoading, error, viewMode, selectedAccount）
  - 实现 `loadEmails()`, `refreshEmails()`, `loadNextPage()` 方法
  - 实现 `switchViewMode()`, `filterByAccount()` 方法
  - 使用 `StateFlow` 管理状态
  - 集成 `GetEmailsUseCase` 和 `SyncEmailsUseCase`
  - _需求: 1.1, 1.3, 9.2, 12.1_

- [x] 13.2 实现 EmailListView（传统视图）


  - 创建 `EmailListView` Composable
  - 使用 `LazyColumn` 显示邮件列表，应用 stagger 动画
  - 使用 `EmailListItem` 组件
  - 实现下拉刷新（PullRefresh）
  - 实现分页加载（监听滚动位置）
  - 实现点击跳转详情页
  - _需求: 1.1, 4.2, 12.2_

- [x] 13.3 实现 EmailChatView（聊天视图）



  - 创建 `EmailChatView` Composable
  - 使用 `LazyColumn(reverseLayout = true)` 显示对话
  - 使用 `MessageBubble` 组件
  - 按线程分组显示邮件
  - 实现点击展开完整邮件头
  - _需求: 1.2, 1.5_

- [x] 13.4 实现视图切换功能


  - 在 TopAppBar 添加视图切换按钮
  - 使用 `AnimatedContent` 实现切换动画（300ms）
  - 持久化视图偏好到 DataStore
  - _需求: 1.3, 1.4, 4.3_

- [x] 13.5 实现 InboxScreen 主界面


  - 创建 `InboxScreen` Composable，整合 ViewModel 和视图
  - 实现 TopAppBar（Chrome 风格）：菜单按钮 + 圆角搜索框（占据大部分宽度）+ 头像
  - 搜索框点击展开为全屏搜索界面
  - 头像点击跳转到账户管理页面，显示当前默认账户头像
  - 实现账户过滤器（Dropdown Menu）
  - 实现 FAB（撰写邮件）
  - 处理加载、错误状态
  - _需求: 1.1, 7.1, 9.2_

- [x] 14. 实现滑动手势操作



  - 使用 `SwipeToDismiss` 实现左右滑动
  - 右滑归档：绿色背景 + 归档图标
  - 左滑删除：红色背景 + 删除图标
  - 滑动超过 50% 触发操作
  - 实现滑动动画（400ms）
  - 集成 `ArchiveEmailUseCase` 和 `DeleteEmailUseCase`
  - _需求: 8.1, 8.2, 8.3_

- [x] 15. 实现多选模式



  - 长按邮件项进入多选模式
  - 显示 Checkbox 和选中数量
  - 实现批量操作工具栏：删除、归档、标记已读
  - 使用 `AnimatedVisibility` 显示/隐藏工具栏
  - _需求: 8.5_

- [x] 16. 实现邮件详情页


- [x] 16.1 创建 EmailDetailViewModel


  - 定义 `EmailDetailUiState` 数据类
  - 实现 `loadEmail()` 方法
  - 实现操作方法：`reply()`, `forward()`, `delete()`, `archive()`, `toggleStar()`
  - _需求: 1.1_

- [x] 16.2 实现 EmailDetailScreen UI


  - 创建 `EmailDetailScreen` Composable
  - 显示发件人信息卡片（玻璃拟态）
  - 显示邮件正文（HTML 渲染使用 WebView）
  - 显示附件列表（横向 LazyRow）
  - 实现操作按钮：回复、全部回复、转发
  - 实现 Shared Element Transition（头像、主题）
  - _需求: 1.1, 4.1_

- [x] 16.3 实现附件预览和下载


  - 点击附件显示 Modal Bottom Sheet
  - 选项：预览、下载、分享
  - 实现文件下载（使用 DownloadManager）
  - 显示下载进度
  - _需求: 6.3_

- [x] 17. 实现邮件撰写功能




- [x] 17.1 创建 ComposeViewModel


  - 定义 `ComposeUiState` 数据类
  - 实现草稿自动保存（30 秒或停止输入 3 秒）
  - 实现 `sendEmail()` 方法，验证收件人格式
  - 实现附件管理：添加、删除、大小限制（25MB/个，50MB 总计）
  - 集成 `SendEmailUseCase`
  - _需求: 6.1, 6.3, 6.4, 6.5_

- [x] 17.2 实现 ComposeScreen UI


  - 创建 `ComposeScreen` Composable
  - 实现输入字段：收件人、抄送、密送、主题、正文（OutlinedTextField）
  - 实现富文本编辑工具栏：粗体、斜体、下划线、列表
  - 使用 `AnnotatedString` 实现富文本
  - 实现发件账户选择（Modal Bottom Sheet）
  - 实现附件列表（横向 LazyRow，可滑动删除）
  - 实现发送按钮（验证后启用）
  - _需求: 6.1, 6.2, 6.3, 9.5_

- [x] 18. 实现搜索功能

- [x] 18.1 创建 SearchViewModel


  - 定义 `SearchUiState` 数据类
  - 实现搜索逻辑，300ms 防抖
  - 实现搜索历史管理（最近 10 条）
  - 实现过滤器：日期范围、发件人、账户、附件
  - 集成 `SearchEmailsUseCase`
  - _需求: 7.1, 7.2, 7.4, 7.5_

- [x] 18.2 实现 SearchScreen UI


  - 创建 `SearchScreen` Composable
  - 使用 Material 3 `SearchBar` 组件
  - 显示搜索历史（Chip 组件，可删除）
  - 实现过滤器 UI（FilterChip）
  - 显示搜索结果，高亮关键词（AnnotatedString + SpanStyle）
  - 实现空状态和加载状态
  - _需求: 7.1, 7.3, 7.4_

- [x] 19. 实现账户管理功能


- [x] 19.1 创建 AccountViewModel


  - 定义 `AccountUiState` 数据类
  - 实现账户列表加载
  - 实现添加、编辑、删除账户
  - 实现账户验证（显示进度）
  - 集成 `ManageAccountUseCase`
  - _需求: 9.1, 9.5_

- [x] 19.2 实现 AccountManagementScreen UI


  - 创建 `AccountManagementScreen` Composable
  - 使用 `ListItem` 显示账户列表（邮箱、名称、颜色指示器）
  - 显示默认账户标记（星标）
  - 实现 FAB 添加新账户
  - 实现删除确认 AlertDialog
  - _需求: 9.1, 9.4_

- [x] 19.3 实现添加账户流程


  - 创建 `AddAccountScreen` Composable
  - 输入字段：邮箱、密码、显示名称、WebDAV 服务器、端口
  - 实现账户验证（调用 WebDAV connect）
  - 显示验证进度和结果
  - 实现颜色选择器
  - 保存账户到数据库和加密存储
  - _需求: 5.1, 5.2, 9.1_

- [x] 20. 实现 Navigation Drawer








  - 创建 `FleurNavigationDrawer` Composable
  - 实现自适应布局：宽度 >= 600dp 显示，< 600dp 隐藏
  - 显示用户信息、文件夹列表、账户列表、设置入口
  - 实现毛玻璃效果：10dp blur + 85% opacity
  - 实现滑入动画（250ms）和背景遮罩（40% black）
  - 显示未读数 Badge
  - _需求: 3.1, 3.2, 3.3, 3.4_

- [x] 21. 实现 Modal Bottom Sheet



  - 创建 `EmailActionsBottomSheet` Composable（邮件操作菜单）
  - 创建 `AccountSelectorBottomSheet` Composable（选择发件账户）
  - 创建 `AttachmentOptionsBottomSheet` Composable（附件操作）
  - 实现毛玻璃效果：12dp blur + 90% opacity
  - 实现滑入动画（300ms）和手势拖拽关闭
  - _需求: 3.5_

- [x] 22. 实现后台同步服务



- [x] 22.1 配置 WorkManager


  - 创建 `EmailSyncWorker` 类，继承 `CoroutineWorker`
  - 实现 `doWork()` 方法，调用 `SyncEmailsUseCase`
  - 配置周期性任务（每 15 分钟）
  - 添加网络连接约束
  - _需求: 5.3_

- [x] 22.2 实现同步状态管理


  - 在 ViewModel 中监听同步状态
  - 显示同步进度指示器
  - 处理同步错误（指数退避重试）
  - 实现手动同步（下拉刷新）
  - _需求: 5.3, 5.4_

- [x] 23. 实现通知系统

- [x] 23.1 创建通知服务


  - 创建 `NotificationService` 类
  - 配置通知渠道（Android 8.0+）
  - 实现新邮件通知：显示发件人、主题、预览
  - 实现通知分组（按账户）
  - _需求: 10.1, 10.2_

- [x] 23.2 实现通知交互


  - 添加通知操作按钮：归档、删除、回复
  - 实现点击通知跳转到邮件详情页
  - 在设置中配置通知偏好（声音、震动、优先级）
  - _需求: 10.3, 10.4, 10.5_

- [x] 24. 实现离线模式



  - 在 Repository 中检测网络状态
  - 离线时仅返回本地缓存数据（最近 30 天）
  - 在 UI 显示离线指示器
  - 实现离线操作队列（发送、删除、归档）
  - 网络恢复时自动执行队列操作（10 秒内）
  - _需求: 11.1, 11.2, 11.3, 11.4, 11.5_

- [x] 25. 实现错误处理



  - 创建 `ErrorDisplay` Composable
  - 网络错误：Snackbar + 重试按钮
  - 认证错误：AlertDialog + 重新登录
  - 同步错误：后台日志 + 指数退避重试
  - 存储错误：AlertDialog + 清理缓存选项
  - 验证错误：TextField 错误提示
  - _需求: 14.1, 14.2, 14.3, 14.4_

- [x] 26. 实现设置页面



  - 创建 `SettingsScreen` Composable
  - 实现主题切换：浅色、深色、跟随系统
  - 实现动态配色开关（Android 12+）
  - 配置同步间隔（Dropdown Menu）
  - 配置通知偏好（Switch）
  - 配置滑动手势自定义
  - 使用 DataStore 持久化设置
  - _需求: 2.2, 2.5, 5.3, 8.4, 10.5_

- [x] 27. 性能优化

- [x] 27.1 优化 Compose 性能

  - 为 UI Model 添加 `@Stable` 和 `@Immutable` 注解
  - 使用 `remember` 和 `derivedStateOf` 优化计算
  - 为 LazyColumn items 提供稳定的 key
  - 使用 `animateItemPlacement` 实现平滑动画
  - _需求: 12.1, 12.5_

- [x] 27.2 优化图片加载

  - 集成 Coil 库
  - 配置内存缓存（25% 可用内存）和磁盘缓存（100MB）
  - 实现图片懒加载（仅加载可见区域）
  - 使用占位符和渐进式加载
  - _需求: 12.3_

- [x] 27.3 优化数据库查询

  - 验证索引是否生效
  - 实现邮件正文懒加载（列表只加载前 200 字符预览）
  - 优化分页查询性能
  - 定期清理 30 天前的缓存
  - _需求: 11.1, 12.2, 12.4_

- [x] 28. 实现可访问性支持

  - 为所有交互元素添加 `contentDescription`
  - 确保触摸目标最小 48dp × 48dp
  - 验证颜色对比度符合 WCAG 2.1 AA 标准（4.5:1）
  - 测试 TalkBack 屏幕阅读器兼容性
  - 支持系统字体缩放（200%）
  - _需求: 13.1, 13.2, 13.3, 13.4, 13.5_

- [x] 29. 集成和端到端测试


  - 创建 MainActivity，设置 Compose 内容和主题
  - 集成所有功能模块：导航、收件箱、详情、撰写、搜索、账户、设置
  - 测试完整用户流程：添加账户 → 同步邮件 → 查看邮件 → 撰写回复 → 发送
  - 测试视图切换、手势操作、离线模式
  - 测试毛玻璃效果和动画性能
  - 修复集成过程中发现的问题
  - _需求: 所有需求_

---

## 任务说明

- 每个任务都是可独立执行的代码实现步骤
- 任务按照依赖关系排序，后续任务依赖前面任务的输出
- 所有代码都会逐步集成到应用中，没有孤立的代码
- 专注于核心功能实现，确保应用优雅、流畅、好用
