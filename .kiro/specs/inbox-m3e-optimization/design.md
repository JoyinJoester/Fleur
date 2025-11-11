# Design Document

## Overview

本设计文档详细描述了Fleur邮件应用收件箱页面的Material Design 3 Enhanced (M3E)优化方案。设计目标是通过精致的卡片设计、流畅的动画效果和优雅的交互反馈，打造一个现代化、美观且实用的收件箱体验。

设计遵循以下核心原则：
- **视觉层次清晰**：通过颜色、字重、间距建立清晰的信息层级
- **动效自然流畅**：使用符合物理规律的缓动曲线和合理的动画时长
- **交互反馈及时**：提供视觉、触觉等多维度的操作反馈
- **性能优先**：确保在大量数据下仍然保持流畅的60fps体验

## Architecture

### 组件层级结构

```
InboxScreen (主容器)
├── InboxTopAppBar (顶部栏)
│   ├── MenuButton (菜单按钮)
│   ├── SearchBar (搜索框)
│   └── AvatarButton (头像按钮)
├── OfflineIndicator (离线指示器)
├── SyncStatusIndicator (同步状态)
├── EmailList (邮件列表)
│   └── EnhancedEmailListItem (增强邮件卡片) ×N
│       ├── AccountIndicator (账户指示器)
│       ├── UnreadDot (未读标记)
│       ├── EmailContent (邮件内容)
│       │   ├── SenderInfo (发件人)
│       │   ├── Subject (主题)
│       │   └── Preview (预览)
│       ├── Timestamp (时间戳)
│       ├── ActionButtons (操作按钮)
│       └── Metadata (元数据：星标、附件)
└── FloatingActionButton (撰写按钮)
```

### 设计模式

1. **组件化设计**：将邮件卡片拆分为独立的可复用组件
2. **状态驱动UI**：通过UiState统一管理界面状态
3. **响应式布局**：支持不同屏幕尺寸和方向
4. **主题适配**：完整支持浅色/深色模式

## Components and Interfaces

### 1. EnhancedEmailListItem (增强邮件列表项)

#### 设计规格

**尺寸与间距**
- 卡片圆角：16dp（比原来的12dp更圆润）
- 卡片内边距：16dp
- 卡片外边距：水平16dp，垂直6dp（增加垂直间距）
- 最小高度：96dp
- 触摸目标：至少48dp×48dp

**视觉效果**
- 默认elevation：4dp（比原来的2dp更明显）
- 悬停elevation：8dp（比原来的6dp更突出）
- 选中边框：2dp，primary色
- 悬停缩放：1.02倍
- 玻璃拟态：浅色模式下启用（20dp blur，90%透明度）

**颜色方案**
```kotlin
// 浅色模式
containerColor = Color.Transparent (使用玻璃拟态)
contentColor = MaterialTheme.colorScheme.onSurface
borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)

// 深色模式
containerColor = MaterialTheme.colorScheme.surface
contentColor = MaterialTheme.colorScheme.onSurface
borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
```

#### 布局结构

```
Row (主容器)
├── Column (左侧指示器) [width: 8dp]
│   ├── AccountColorDot (账户颜色) [8dp圆形]
│   └── UnreadDot (未读标记) [6dp圆形，仅未读时显示]
├── Spacer [12dp]
├── Column (邮件内容) [weight: 1f]
│   ├── Text (发件人) [titleMedium, 粗体(未读)]
│   ├── Spacer [4dp]
│   ├── Text (主题) [bodyMedium, 半粗(未读)]
│   ├── Spacer [2dp]
│   └── Text (预览) [bodySmall, 2行]
├── Spacer [8dp]
└── Column (右侧元数据) [align: End]
    ├── Text (时间戳) [labelSmall]
    ├── Spacer [4dp]
    └── Row (图标行)
        ├── StarIcon (星标) [16dp，仅星标时显示]
        └── AttachmentCount (附件数) [labelSmall]
```

#### 交互状态

**1. 默认状态**
- elevation: 4dp
- scale: 1.0
- opacity: 1.0

**2. 悬停状态（Hover）**
- elevation: 8dp → 动画150ms
- scale: 1.02 → 动画150ms
- 显示操作按钮（星标、归档、删除）→ 淡入200ms

**3. 按压状态（Pressed）**
- 涟漪效果：从触摸点扩散
- duration: 150ms
- color: primary.copy(alpha = 0.12f)

**4. 选中状态（Selected）**
- 左侧蓝色竖条：4dp宽
- 边框：2dp，primary色
- 背景色：primaryContainer.copy(alpha = 0.08f)

**5. 滑动状态（Swipe）**
- 左滑（归档）：绿色背景 + 归档图标
- 右滑（删除）：红色背景 + 删除图标
- threshold: 0.3（30%宽度触发）
- animation: 400ms，DecelerateEasing

#### 动画规格

**进入动画（Stagger）**
```kotlin
fadeIn(
    animationSpec = tween(
        durationMillis = 200,
        delayMillis = index * 50,
        easing = FastOutSlowIn
    )
) + slideInVertically(
    animationSpec = tween(
        durationMillis = 200,
        delayMillis = index * 50,
        easing = FastOutSlowIn
    ),
    initialOffsetY = { it / 4 }
)
```

**退出动画**
```kotlin
fadeOut(
    animationSpec = tween(
        durationMillis = 200,
        easing = AccelerateEasing
    )
) + slideOutVertically(
    animationSpec = tween(
        durationMillis = 200,
        easing = AccelerateEasing
    ),
    targetOffsetY = { it / 4 }
)
```

**悬停动画**
```kotlin
// Elevation
animateDpAsState(
    targetValue = if (isHovered) 8.dp else 4.dp,
    animationSpec = tween(150, easing = FastOutSlowIn)
)

// Scale
animateFloatAsState(
    targetValue = if (isHovered) 1.02f else 1f,
    animationSpec = tween(150, easing = FastOutSlowIn)
)
```

### 2. InboxTopAppBar (顶部应用栏)

#### 设计规格

**尺寸**
- 高度：64dp（标准TopAppBar高度）
- 搜索框圆角：24dp（完全圆角）
- 头像直径：40dp

**布局**
```
TopAppBar
├── NavigationIcon (菜单按钮) [48dp×48dp]
├── Title (搜索框 + 头像)
│   ├── TextField (搜索框) [weight: 1f, height: 48dp]
│   │   ├── LeadingIcon (搜索图标)
│   │   └── Placeholder ("搜索邮件")
│   ├── Spacer [8dp]
│   └── AvatarButton (头像) [40dp圆形]
```

**视觉效果**
- 搜索框背景：surfaceVariant
- 搜索框elevation：0dp（平面设计）
- 滚动时elevation：4dp（显示阴影）
- 头像背景：primary色
- 头像文字：onPrimary色

#### 滚动行为

```kotlin
val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

// 根据滚动偏移量调整elevation
val elevation by animateDpAsState(
    targetValue = if (scrollOffset > 0) 4.dp else 0.dp,
    animationSpec = tween(200)
)
```

### 3. EmailList (邮件列表)

#### LazyColumn配置

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(0.dp) // 卡片自带间距
) {
    itemsIndexed(
        items = emails,
        key = { _, email -> email.id }
    ) { index, email ->
        EnhancedEmailListItem(
            email = email,
            index = index, // 用于stagger动画
            modifier = Modifier.animateItemPlacement()
        )
    }
}
```

#### 下拉刷新

```kotlin
PullRefreshIndicator(
    refreshing = isRefreshing,
    state = pullRefreshState,
    modifier = Modifier.align(Alignment.TopCenter),
    backgroundColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.primary
)
```

### 4. 操作按钮（悬停时显示）

#### 设计规格

**位置**：卡片右侧，垂直居中
**尺寸**：每个按钮40dp×40dp
**间距**：按钮之间4dp

**按钮列表**
1. 星标按钮（Star）
   - 图标：Icons.Default.Star / StarBorder
   - 颜色：StarYellow / onSurfaceVariant
   
2. 归档按钮（Archive）
   - 图标：Icons.Default.Archive
   - 颜色：onSurfaceVariant
   
3. 删除按钮（Delete）
   - 图标：Icons.Default.Delete
   - 颜色：error

**动画**
```kotlin
AnimatedVisibility(
    visible = isHovered,
    enter = fadeIn(tween(200)) + expandHorizontally(tween(200)),
    exit = fadeOut(tween(150)) + shrinkHorizontally(tween(150))
) {
    Row(spacing = 4.dp) {
        IconButton(onClick = onStar) { ... }
        IconButton(onClick = onArchive) { ... }
        IconButton(onClick = onDelete) { ... }
    }
}
```

### 5. 空状态与加载状态

#### 加载状态（骨架屏）

```kotlin
@Composable
fun EmailListItemSkeleton() {
    FleurCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // 账户指示器骨架
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .shimmer()
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // 发件人骨架
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 主题骨架
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // 预览骨架
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }
    }
}

// Shimmer效果
@Composable
fun Modifier.shimmer(): Modifier {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    return background(
        MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
    )
}
```

#### 空状态

```kotlin
@Composable
fun EmptyInboxState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 插图
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 标题
        Text(
            text = "收件箱为空",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 描述
        Text(
            text = "您的所有邮件都已处理完毕",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
```

## Data Models

### EmailUiModel (扩展)

```kotlin
data class EmailUiModel(
    val id: String,
    val accountId: String,
    val accountColor: Color,
    val fromName: String,
    val fromAddress: String,
    val subject: String,
    val preview: String,
    val timestamp: Instant,
    val isRead: Boolean,
    val isStarred: Boolean,
    val hasAttachments: Boolean,
    val attachmentCount: Int,
    
    // 新增字段
    val isHovered: Boolean = false,  // 悬停状态
    val isSelected: Boolean = false, // 选中状态
    val swipeProgress: Float = 0f,   // 滑动进度
    val animationIndex: Int = 0      // 动画索引（用于stagger）
)
```

### InboxUiState (扩展)

```kotlin
data class InboxUiState(
    val emails: List<EmailUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val isOffline: Boolean = false,
    val error: FleurError? = null,
    val syncError: FleurError? = null,
    
    // 多选模式
    val isMultiSelectMode: Boolean = false,
    val selectedEmailIds: Set<String> = emptySet(),
    
    // 视图模式
    val viewMode: ViewMode = ViewMode.LIST,
    
    // 过滤
    val selectedAccountId: String? = null,
    
    // 分页
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
    
    // 新增：性能优化
    val visibleRange: IntRange = 0..20, // 可见范围
    val scrollOffset: Int = 0            // 滚动偏移
)
```

## Error Handling

### 错误类型

1. **网络错误**：显示离线指示器，允许离线操作
2. **同步错误**：显示Snackbar提示，提供重试按钮
3. **加载错误**：显示错误状态页面，提供刷新按钮
4. **操作错误**：显示Snackbar提示，自动撤销操作

### 错误显示策略

```kotlin
// 非阻塞错误：使用Snackbar
LaunchedEffect(uiState.error) {
    uiState.error?.let { error ->
        val result = snackbarHostState.showSnackbar(
            message = error.getUserMessage(),
            actionLabel = "重试",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.retryLastOperation()
        }
        viewModel.clearError()
    }
}

// 阻塞错误：使用错误页面
if (uiState.error != null && uiState.emails.isEmpty()) {
    ErrorState(
        error = uiState.error,
        onRetry = { viewModel.refreshEmails() }
    )
}
```

## Testing Strategy

### 单元测试

1. **EmailListItem组件测试**
   - 测试不同状态的渲染（默认、悬停、选中）
   - 测试动画触发条件
   - 测试点击和长按事件

2. **动画测试**
   - 测试stagger动画的延迟计算
   - 测试elevation和scale动画的目标值
   - 测试滑动动画的threshold

3. **性能测试**
   - 测试大列表（1000+项）的渲染性能
   - 测试滚动帧率
   - 测试内存占用

### UI测试

1. **交互测试**
   - 测试邮件卡片的点击和长按
   - 测试滑动操作（归档、删除）
   - 测试多选模式的进入和退出

2. **视觉回归测试**
   - 截图对比测试（浅色/深色模式）
   - 测试不同屏幕尺寸的布局
   - 测试动画的关键帧

3. **无障碍测试**
   - 测试TalkBack支持
   - 测试触摸目标大小
   - 测试颜色对比度

### 性能基准

- **初始加载时间**：< 500ms（20封邮件）
- **滚动帧率**：60fps（稳定）
- **动画流畅度**：60fps（无掉帧）
- **内存占用**：< 100MB（1000封邮件）
- **交互响应时间**：< 100ms（点击到反馈）

## Implementation Notes

### 性能优化策略

1. **虚拟滚动**：使用LazyColumn，只渲染可见项
2. **状态缓存**：使用remember缓存不变的计算结果
3. **动画优化**：使用animateItemPlacement减少重组
4. **图片懒加载**：头像和附件缩略图按需加载
5. **分页加载**：每次加载20封邮件，滚动到底部时加载更多

### 无障碍支持

1. **语义标签**：为所有图标提供contentDescription
2. **触摸目标**：确保所有可点击元素至少48dp×48dp
3. **颜色对比度**：确保文本与背景对比度≥4.5:1
4. **字体缩放**：支持系统字体大小设置
5. **屏幕阅读器**：提供清晰的语义结构

### 主题适配

1. **浅色模式**：使用玻璃拟态效果，半透明背景
2. **深色模式**：使用纯色背景，避免过度模糊
3. **动态颜色**：支持Android 12+的Material You动态颜色
4. **对比度模式**：支持高对比度模式

### 动画性能

1. **硬件加速**：确保所有动画使用GPU加速
2. **避免过度动画**：限制同时播放的动画数量
3. **降级策略**：低端设备禁用复杂动画
4. **帧率监控**：使用Choreographer监控帧率

## Design Decisions

### 为什么选择16dp圆角？

- 12dp圆角略显生硬，16dp更加柔和优雅
- 符合M3E的"柔和圆润"设计语言
- 与其他组件（Bottom Sheet 16dp）保持一致

### 为什么增加卡片elevation？

- 原来的2dp阴影在浅色背景下不够明显
- 4dp阴影提供更好的层次感
- 悬停时8dp阴影提供明确的交互反馈

### 为什么使用stagger动画？

- 避免大量元素同时出现造成的视觉冲击
- 提供更自然的"逐个加载"感觉
- 50ms延迟既不会太慢也不会太快

### 为什么在悬停时显示操作按钮？

- 减少界面视觉噪音，保持简洁
- 提供"渐进式披露"的交互体验
- 符合桌面端的交互习惯

### 为什么使用骨架屏而不是加载指示器？

- 骨架屏提供更好的内容预期
- 减少"突然出现"的视觉跳跃
- 提供更流畅的加载体验
