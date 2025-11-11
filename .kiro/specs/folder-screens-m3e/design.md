# Design Document - 文件夹页面 M3E 优化

## Overview

本设计文档描述了 Fleur 邮件应用中五个文件夹页面（已发送、草稿箱、星标邮件、归档、垃圾箱）的详细设计方案。设计遵循 Material 3 设计规范，重点关注流畅的动画、直观的交互和优秀的用户体验。

### 设计原则

1. **一致性优先**：所有文件夹页面共享统一的架构、组件和交互模式
2. **性能至上**：使用虚拟滚动、延迟加载和骨架屏确保流畅体验
3. **反馈及时**：每个操作都有明确的视觉和触觉反馈
4. **容错设计**：提供撤销操作和错误恢复机制
5. **自适应布局**：支持手机、平板和横屏模式

## Architecture

### 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ SentScreen   │  │ DraftsScreen │  │ StarredScreen│  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │ArchiveScreen │  │ TrashScreen  │                     │
│  └──────────────┘  └──────────────┘                     │
│                          ↓                               │
│              ┌──────────────────────┐                    │
│              │ FolderScreenTemplate │ (共享组件)        │
│              └──────────────────────┘                    │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │              FolderViewModel (共享)               │   │
│  └──────────────────────────────────────────────────┘   │
│                          ↓                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │GetEmailsUse  │  │ArchiveEmail  │  │ DeleteEmail  │  │
│  │   Case       │  │   UseCase    │  │   UseCase    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
│              ┌──────────────────────┐                    │
│              │  EmailRepository     │                    │
│              └──────────────────────┘                    │
│                          ↓                               │
│              ┌──────────────────────┐                    │
│              │     EmailDao         │                    │
│              └──────────────────────┘                    │
└─────────────────────────────────────────────────────────┘
```

### 设计模式

1. **模板模式**：`FolderScreenTemplate` 作为所有文件夹页面的基础模板
2. **策略模式**：不同文件夹使用不同的 `FolderConfig` 配置滑动操作和空状态
3. **MVVM 模式**：ViewModel 管理状态，Screen 负责 UI 渲染
4. **Repository 模式**：统一的数据访问接口

## Components and Interfaces

### 1. FolderScreenTemplate（核心模板组件）

所有文件夹页面的共享模板，提供统一的布局和交互逻辑。

```kotlin
@Composable
fun FolderScreenTemplate(
    config: FolderConfig,
    uiState: FolderUiState,
    onNavigateBack: () -> Unit,
    onNavigateToEmailDetail: (String) -> Unit,
    onNavigateToCompose: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onEmailAction: (String, EmailAction) -> Unit,
    onBatchAction: (List<String>, EmailAction) -> Unit,
    modifier: Modifier = Modifier
)
```

**职责**：
- 渲染顶部应用栏（标题、返回按钮、操作按钮）
- 显示邮件列表或空状态
- 处理下拉刷新和分页加载
- 管理多选模式
- 显示 FAB（如果配置需要）
- 显示 Snackbar 提示

### 2. FolderConfig（文件夹配置）

定义每个文件夹的特定行为和外观。

```kotlin
data class FolderConfig(
    val folderType: FolderType,
    val title: String,
    val emptyStateConfig: EmptyStateConfig,
    val swipeActions: SwipeActionsConfig,
    val showFab: Boolean = false,
    val fabIcon: ImageVector? = null,
    val fabAction: (() -> Unit)? = null,
    val topBarActions: List<TopBarAction> = emptyList()
)

enum class FolderType {
    SENT,       // 已发送
    DRAFTS,     // 草稿箱
    STARRED,    // 星标邮件
    ARCHIVE,    // 归档
    TRASH       // 垃圾箱
}

data class SwipeActionsConfig(
    val leftSwipe: SwipeAction?,
    val rightSwipe: SwipeAction?
)

data class SwipeAction(
    val icon: ImageVector,
    val backgroundColor: Color,
    val action: EmailAction
)

enum class EmailAction {
    DELETE,
    ARCHIVE,
    UNARCHIVE,
    STAR,
    UNSTAR,
    RESTORE,
    MARK_READ,
    MARK_UNREAD
}
```

### 3. FolderUiState（状态管理）

```kotlin
data class FolderUiState(
    val emails: List<Email> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: FleurError? = null,
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
    
    // 多选模式
    val isMultiSelectMode: Boolean = false,
    val selectedEmailIds: Set<String> = emptySet(),
    
    // 操作反馈
    val lastAction: ActionResult? = null,
    val showUndoSnackbar: Boolean = false
)

data class ActionResult(
    val action: EmailAction,
    val emailIds: List<String>,
    val timestamp: Long,
    val canUndo: Boolean = true
)
```

### 4. FolderViewModel（共享 ViewModel）

所有文件夹页面共享的 ViewModel，通过 `folderType` 参数区分不同文件夹。

```kotlin
@HiltViewModel
class FolderViewModel @Inject constructor(
    private val getEmailsUseCase: GetEmailsUseCase,
    private val archiveEmailUseCase: ArchiveEmailUseCase,
    private val deleteEmailUseCase: DeleteEmailUseCase,
    private val restoreEmailUseCase: RestoreEmailUseCase,
    private val toggleStarUseCase: ToggleStarUseCase,
    private val markAsReadUseCase: MarkAsReadUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val folderType: FolderType = 
        savedStateHandle.get<String>("folderType")?.let { 
            FolderType.valueOf(it) 
        } ?: FolderType.SENT
    
    private val _uiState = MutableStateFlow(FolderUiState())
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()
    
    init {
        loadEmails()
    }
    
    fun loadEmails(reset: Boolean = false)
    fun refreshEmails()
    fun loadNextPage()
    fun performAction(emailId: String, action: EmailAction)
    fun performBatchAction(emailIds: List<String>, action: EmailAction)
    fun undoLastAction()
    fun enterMultiSelectMode(emailId: String)
    fun exitMultiSelectMode()
    fun toggleEmailSelection(emailId: String)
}
```

### 5. 具体页面实现

每个文件夹页面只需要提供配置和导航逻辑：

```kotlin
@Composable
fun SentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmailDetail: (String) -> Unit,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val config = remember {
        FolderConfig(
            folderType = FolderType.SENT,
            title = "已发送",
            emptyStateConfig = EmptyStateConfig(
                icon = Icons.Default.Send,
                title = "暂无已发送邮件",
                description = "您发送的邮件将显示在这里"
            ),
            swipeActions = SwipeActionsConfig(
                leftSwipe = SwipeAction(
                    icon = Icons.Default.Delete,
                    backgroundColor = DeleteRed,
                    action = EmailAction.DELETE
                ),
                rightSwipe = null
            ),
            showFab = false
        )
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    FolderScreenTemplate(
        config = config,
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToEmailDetail = onNavigateToEmailDetail,
        onNavigateToCompose = {},
        onRefresh = { viewModel.refreshEmails() },
        onLoadMore = { viewModel.loadNextPage() },
        onEmailAction = { emailId, action -> 
            viewModel.performAction(emailId, action) 
        },
        onBatchAction = { emailIds, action -> 
            viewModel.performBatchAction(emailIds, action) 
        }
    )
}
```

### 6. 空状态组件

```kotlin
@Composable
fun FolderEmptyState(
    config: EmptyStateConfig,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = config.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = config.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        config.actionButton?.let { button ->
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = button.onClick) {
                Text(button.text)
            }
        }
    }
}

data class EmptyStateConfig(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val actionButton: ActionButton? = null
)

data class ActionButton(
    val text: String,
    val onClick: () -> Unit
)
```

## Data Models

### Email 领域模型（已存在）

```kotlin
data class Email(
    val id: String,
    val threadId: String,
    val accountId: String,
    val from: EmailAddress,
    val to: List<EmailAddress>,
    val cc: List<EmailAddress> = emptyList(),
    val bcc: List<EmailAddress> = emptyList(),
    val subject: String,
    val bodyPreview: String,
    val bodyPlain: String,
    val bodyHtml: String? = null,
    val bodyMarkdown: String? = null,
    val contentType: ContentType = ContentType.TEXT,
    val timestamp: Long,
    val isRead: Boolean,
    val isStarred: Boolean,
    val labels: List<String> = emptyList(),
    val attachments: List<Attachment> = emptyList()
)
```

### 数据库查询扩展

需要在 `EmailDao` 中添加按文件夹类型查询的方法：

```kotlin
@Dao
interface EmailDao {
    // 现有方法...
    
    @Query("""
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND labels LIKE '%sent%'
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getSentEmails(
        accountId: String,
        limit: Int,
        offset: Int
    ): Flow<List<EmailEntity>>
    
    @Query("""
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND labels LIKE '%drafts%'
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getDraftEmails(
        accountId: String,
        limit: Int,
        offset: Int
    ): Flow<List<EmailEntity>>
    
    @Query("""
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND is_starred = 1
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getStarredEmails(
        accountId: String,
        limit: Int,
        offset: Int
    ): Flow<List<EmailEntity>>
    
    @Query("""
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND labels LIKE '%archive%'
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getArchivedEmails(
        accountId: String,
        limit: Int,
        offset: Int
    ): Flow<List<EmailEntity>>
    
    @Query("""
        SELECT * FROM emails 
        WHERE account_id = :accountId 
        AND labels LIKE '%trash%'
        ORDER BY timestamp DESC 
        LIMIT :limit OFFSET :offset
    """)
    fun getTrashedEmails(
        accountId: String,
        limit: Int,
        offset: Int
    ): Flow<List<EmailEntity>>
}
```

## Error Handling

### 错误类型

使用现有的 `FleurError` 类型：

- `NetworkError`：网络请求失败
- `DatabaseError`：数据库操作失败
- `NotFoundError`：邮件不存在
- `UnknownError`：未知错误

### 错误显示策略

1. **加载错误**：显示错误状态页面，提供重试按钮
2. **操作错误**：显示 Snackbar 提示，自动消失
3. **网络错误**：显示离线指示器，允许离线操作

```kotlin
@Composable
fun ErrorState(
    error: FleurError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = error.getUserMessage(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}
```

## Testing Strategy

### 单元测试

1. **FolderViewModel 测试**
   - 测试邮件加载逻辑
   - 测试分页加载
   - 测试邮件操作（删除、归档等）
   - 测试多选模式
   - 测试撤销操作

2. **UseCase 测试**
   - 测试各种邮件操作的业务逻辑
   - 测试错误处理

### UI 测试

1. **FolderScreenTemplate 测试**
   - 测试空状态显示
   - 测试邮件列表渲染
   - 测试下拉刷新
   - 测试滑动操作
   - 测试多选模式

2. **集成测试**
   - 测试完整的用户流程（浏览、操作、撤销）
   - 测试导航流程

## Material 3 Design Details

### 动画规范

所有动画遵循 Material 3 Motion 系统：

```kotlin
object FleurAnimation {
    // 标准过渡动画
    const val STANDARD_DURATION = 300
    const val STANDARD_EASING = androidx.compose.animation.core.FastOutSlowInEasing
    
    // 滑动操作动画
    const val SWIPE_DURATION = 400
    val DecelerateEasing = androidx.compose.animation.core.CubicBezierEasing(0f, 0f, 0.2f, 1f)
    
    // 列表项动画
    const val LIST_ITEM_STAGGER_DELAY = 50L
    const val LIST_ITEM_FADE_DURATION = 200
    
    // 多选模式动画
    const val MULTI_SELECT_SCALE_DURATION = 200
    const val MULTI_SELECT_SCALE_FACTOR = 0.95f
}
```

### 颜色规范

```kotlin
// 操作颜色
val ArchiveGreen = Color(0xFF4CAF50)
val DeleteRed = Color(0xFFF44336)
val StarYellow = Color(0xFFFFC107)
val RestoreBlue = Color(0xFF2196F3)
```

### 间距规范

```kotlin
object FolderScreenDimens {
    val ContentPadding = 16.dp
    val ItemSpacing = 8.dp
    val EmptyStateIconSize = 120.dp
    val EmptyStateSpacing = 24.dp
    val SwipeThreshold = 80.dp
    val MultiSelectCheckboxSize = 24.dp
}
```

### 触觉反馈

- **长按进入多选**：`HapticFeedbackType.LongPress`
- **滑动操作触发**：`HapticFeedbackType.LongPress`
- **批量操作完成**：`HapticFeedbackType.TextHandleMove`

## Performance Optimization

### 1. 虚拟滚动

使用 `LazyColumn` 实现虚拟滚动，只渲染可见项：

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
) {
    items(
        items = emails,
        key = { it.id }
    ) { email ->
        SwipeableEmailItem(
            onArchive = { /* ... */ },
            onDelete = { /* ... */ }
        ) {
            EmailListItem(
                email = email,
                isSelected = email.id in selectedEmailIds,
                onClick = { /* ... */ },
                onLongClick = { /* ... */ }
            )
        }
    }
}
```

### 2. 延迟加载图片

```kotlin
@Composable
fun EmailListItem(
    email: Email,
    modifier: Modifier = Modifier
) {
    val isScrolling = remember { mutableStateOf(false) }
    
    Row(modifier = modifier) {
        if (!isScrolling.value) {
            AsyncImage(
                model = email.from.avatarUrl,
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
        } else {
            // 滚动时显示占位符
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        // ...
    }
}
```

### 3. 骨架屏

```kotlin
@Composable
fun EmailListItemSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .shimmer()
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .shimmer()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .shimmer()
            )
        }
    }
}
```

### 4. 分页加载

```kotlin
LazyColumn {
    items(emails) { email ->
        EmailListItem(email = email)
    }
    
    // 加载更多触发器
    item {
        if (hasMorePages && !isLoading) {
            LaunchedEffect(Unit) {
                onLoadMore()
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
```

## Adaptive Layout

### 响应式断点

```kotlin
enum class WindowSizeClass {
    COMPACT,  // < 600dp (手机竖屏)
    MEDIUM,   // 600dp - 840dp (手机横屏、小平板)
    EXPANDED  // > 840dp (大平板)
}

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp < 600 -> WindowSizeClass.COMPACT
        configuration.screenWidthDp < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}
```

### 自适应布局实现

```kotlin
@Composable
fun FolderScreenTemplate(
    config: FolderConfig,
    uiState: FolderUiState,
    // ...
) {
    val windowSizeClass = rememberWindowSizeClass()
    
    when (windowSizeClass) {
        WindowSizeClass.COMPACT -> {
            // 单列布局
            CompactLayout(config, uiState, /* ... */)
        }
        WindowSizeClass.MEDIUM -> {
            // 双列布局
            MediumLayout(config, uiState, /* ... */)
        }
        WindowSizeClass.EXPANDED -> {
            // 双窗格布局（列表 + 详情）
            ExpandedLayout(config, uiState, /* ... */)
        }
    }
}
```

## Navigation Integration

### 路由定义

```kotlin
sealed class Screen(val route: String) {
    object Inbox : Screen("inbox")
    object Sent : Screen("sent")
    object Drafts : Screen("drafts")
    object Starred : Screen("starred")
    object Archive : Screen("archive")
    object Trash : Screen("trash")
    // ...
}
```

### NavGraph 更新

```kotlin
@Composable
fun NavGraph(
    navController: NavHostController,
    // ...
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Inbox.route
    ) {
        // 已发送
        composable(Screen.Sent.route) {
            SentScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailDetail = { emailId ->
                    navController.navigate(Screen.EmailDetail.createRoute(emailId))
                }
            )
        }
        
        // 草稿箱
        composable(Screen.Drafts.route) {
            DraftsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCompose = { draftId ->
                    navController.navigate(Screen.Compose.createRouteWithDraft(draftId))
                }
            )
        }
        
        // 星标邮件
        composable(Screen.Starred.route) {
            StarredScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailDetail = { emailId ->
                    navController.navigate(Screen.EmailDetail.createRoute(emailId))
                }
            )
        }
        
        // 归档
        composable(Screen.Archive.route) {
            ArchiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailDetail = { emailId ->
                    navController.navigate(Screen.EmailDetail.createRoute(emailId))
                }
            )
        }
        
        // 垃圾箱
        composable(Screen.Trash.route) {
            TrashScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailDetail = { emailId ->
                    navController.navigate(Screen.EmailDetail.createRoute(emailId))
                }
            )
        }
    }
}
```

## Accessibility

### 语义化标签

```kotlin
@Composable
fun EmailListItem(
    email: Email,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .semantics {
                contentDescription = "邮件来自 ${email.from.name}，" +
                    "主题：${email.subject}，" +
                    "时间：${formatTimestamp(email.timestamp)}"
                if (!email.isRead) {
                    stateDescription = "未读"
                }
            }
    ) {
        // ...
    }
}
```

### 触摸目标大小

确保所有可交互元素至少 48dp × 48dp：

```kotlin
IconButton(
    onClick = { /* ... */ },
    modifier = Modifier.size(48.dp)
) {
    Icon(/* ... */)
}
```

## Summary

本设计采用模板模式和配置驱动的方式，实现了五个文件夹页面的统一架构。通过共享 `FolderScreenTemplate` 和 `FolderViewModel`，大幅减少了代码重复，同时保持了各页面的灵活性。设计严格遵循 Material 3 规范，提供流畅的动画、直观的交互和优秀的性能表现。

