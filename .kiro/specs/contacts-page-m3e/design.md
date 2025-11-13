# Design Document

## Overview

本设计文档详细描述了 Fleur 邮件应用中联系人页面的完整设计方案。联系人页面作为底部导航栏的第三个按钮,提供优雅、高效的联系人管理和快速操作体验。设计完全遵循 Material 3 Extended (M3E) 设计规范,注重流畅的交互、精致的视觉效果和出色的可用性。

### 设计目标

1. **高效操作**: 提供快速的聊天和邮件发送入口,减少操作步骤
2. **清晰展示**: 使用清晰的视觉层次展示联系人信息
3. **流畅体验**: 实现 60fps 的流畅滚动和自然的动画效果
4. **完美设计**: 符合 M3E 规范,提供精致的视觉体验

### 技术栈

- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **State Management**: StateFlow + Compose State
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil

## Architecture

### 组件层级结构

```
ContactsScreen (主容器)
├── TopAppBar (顶部栏)
│   ├── Title ("联系人")
│   ├── SearchIcon
│   └── MenuIcon
├── SearchBar (搜索栏 - 可展开)
│   ├── SearchTextField
│   ├── ClearButton
│   └── SearchResults
├── ContactsList (联系人列表)
│   ├── AlphabetIndex (字母索引)
│   └── LazyColumn
│       └── ContactGroup (多个)
│           ├── StickyHeader (字母标题)
│           └── ContactItem (多个)
│               ├── Avatar (头像)
│               ├── ContactInfo (信息)
│               │   ├── Name (姓名)
│               │   └── Email (邮箱)
│               ├── OnlineIndicator (在线状态)
│               └── QuickActions (快速操作)
│                   ├── ChatButton
│                   └── EmailButton
├── FloatingActionButton (添加联系人)
└── ContactDetailBottomSheet (详情面板)
    ├── ContactHeader
    │   ├── LargeAvatar
    │   ├── Name
    │   └── Email
    ├── ActionButtons
    │   ├── ChatAction
    │   ├── EmailAction
    │   ├── EditAction
    │   └── DeleteAction
    └── ContactDetails
        ├── PhoneNumbers
        ├── Addresses
        └── Notes
```


### 设计模式

1. **组件化设计**: 将联系人卡片、搜索栏、详情面板拆分为独立的可复用组件
2. **状态驱动UI**: 通过 ContactsUiState 统一管理界面状态
3. **响应式布局**: 支持不同屏幕尺寸和方向
4. **主题适配**: 完整支持浅色/深色模式和动态颜色

## Components and Interfaces

### 1. ContactsScreen (联系人主页面)

主容器组件,负责整体布局和状态管理。

```kotlin
@Composable
fun ContactsScreen(
    navController: NavController,
    viewModel: ContactsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
)
```

**职责**:
- 管理页面整体布局
- 协调子组件交互
- 处理导航逻辑
- 管理底部面板状态

### 2. ContactItem (联系人列表项)

#### 设计规格

**尺寸与间距**
- 列表项高度: 72dp (标准 Material 3 ListItem 高度)
- 头像直径: 56dp
- 水平内边距: 16dp
- 垂直内边距: 8dp
- 头像与内容间距: 16dp
- 内容与操作按钮间距: 8dp

**视觉效果**
- 背景色: surface
- 悬停背景: surfaceVariant.copy(alpha = 0.08f)
- 按压涟漪: primary.copy(alpha = 0.12f)
- 分隔线: 1dp, outlineVariant.copy(alpha = 0.12f)

**颜色方案**
```kotlin
// 头像
containerColor = MaterialTheme.colorScheme.primaryContainer
contentColor = MaterialTheme.colorScheme.onPrimaryContainer

// 文本
nameColor = MaterialTheme.colorScheme.onSurface
emailColor = MaterialTheme.colorScheme.onSurfaceVariant

// 在线状态
onlineColor = Color(0xFF4CAF50) // 绿色
offlineColor = MaterialTheme.colorScheme.outlineVariant
```

#### 布局结构

```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(72.dp)
        .clickable { onContactClick() }
        .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    // 头像 + 在线状态
    Box {
        ContactAvatar(
            imageUrl = contact.avatarUrl,
            name = contact.name,
            size = 56.dp
        )
        OnlineIndicator(
            isOnline = contact.isOnline,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
    
    Spacer(modifier = Modifier.width(16.dp))
    
    // 联系人信息
    Column(
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = contact.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = contact.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    
    Spacer(modifier = Modifier.width(8.dp))
    
    // 快速操作按钮
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onChatClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Chat,
                contentDescription = "发起聊天",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        IconButton(
            onClick = onEmailClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = "发送邮件",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
```


#### 交互状态

**1. 默认状态**
- 背景: 透明
- 操作按钮: 可见

**2. 悬停状态 (Hover)**
- 背景: surfaceVariant.copy(alpha = 0.08f)
- 动画: 150ms fadeIn

**3. 按压状态 (Pressed)**
- 涟漪效果: 从触摸点扩散
- duration: 150ms
- color: primary.copy(alpha = 0.12f)

**4. 选中状态 (长按多选)**
- 左侧蓝色竖条: 4dp宽
- 背景: primaryContainer.copy(alpha = 0.12f)
- 显示复选框替代头像

#### 动画规格

**进入动画 (Stagger)**
```kotlin
fadeIn(
    animationSpec = tween(
        durationMillis = 200,
        delayMillis = index * 30,
        easing = FastOutSlowInEasing
    )
) + slideInVertically(
    animationSpec = tween(
        durationMillis = 200,
        delayMillis = index * 30,
        easing = FastOutSlowInEasing
    ),
    initialOffsetY = { it / 6 }
)
```

**点击动画**
```kotlin
// 缩放反馈
animateFloatAsState(
    targetValue = if (isPressed) 0.98f else 1f,
    animationSpec = tween(100)
)
```

### 3. ContactAvatar (联系人头像)

```kotlin
@Composable
fun ContactAvatar(
    imageUrl: String?,
    name: String,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
)
```

**实现逻辑**:
- 如果有头像URL,使用 Coil 加载图片
- 如果没有头像,显示姓名首字母缩写
- 首字母背景色根据姓名哈希值生成

**首字母头像设计**:
```kotlin
Box(
    modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(getColorForName(name)),
    contentAlignment = Alignment.Center
) {
    Text(
        text = getInitials(name),
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,
        fontWeight = FontWeight.SemiBold
    )
}

// 根据姓名生成颜色
fun getColorForName(name: String): Color {
    val colors = listOf(
        Color(0xFF1976D2), // Blue
        Color(0xFF388E3C), // Green
        Color(0xFFD32F2F), // Red
        Color(0xFFF57C00), // Orange
        Color(0xFF7B1FA2), // Purple
        Color(0xFF0097A7), // Cyan
        Color(0xFFC2185B), // Pink
        Color(0xFF5D4037)  // Brown
    )
    val hash = name.hashCode()
    return colors[abs(hash) % colors.size]
}

// 获取首字母
fun getInitials(name: String): String {
    val parts = name.trim().split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}"
        parts.size == 1 && parts[0].isNotEmpty() -> parts[0].take(2)
        else -> "?"
    }.uppercase()
}
```

### 4. OnlineIndicator (在线状态指示器)

```kotlin
@Composable
fun OnlineIndicator(
    isOnline: Boolean,
    modifier: Modifier = Modifier
)
```

**设计规格**:
- 尺寸: 14dp 圆形
- 边框: 2dp 白色边框
- 在线颜色: #4CAF50 (绿色)
- 离线颜色: outlineVariant

```kotlin
Box(
    modifier = modifier
        .size(14.dp)
        .border(2.dp, Color.White, CircleShape)
        .clip(CircleShape)
        .background(
            if (isOnline) Color(0xFF4CAF50)
            else MaterialTheme.colorScheme.outlineVariant
        )
)
```

### 5. SearchBar (搜索栏)

```kotlin
@Composable
fun ContactsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    isActive: Boolean,
    modifier: Modifier = Modifier
)
```

**设计规格**:
- 高度: 56dp (默认), 展开后全屏
- 圆角: 28dp (完全圆角)
- 背景: surfaceVariant
- elevation: 0dp (默认), 3dp (激活)

**布局**:
```kotlin
SearchBar(
    query = query,
    onQueryChange = onQueryChange,
    onSearch = { /* 执行搜索 */ },
    active = isActive,
    onActiveChange = onSearchActiveChange,
    modifier = modifier.fillMaxWidth(),
    placeholder = { Text("搜索联系人") },
    leadingIcon = {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "搜索"
        )
    },
    trailingIcon = {
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "清除"
                )
            }
        }
    }
) {
    // 搜索结果列表
    LazyColumn {
        items(searchResults) { contact ->
            ContactItem(
                contact = contact,
                onContactClick = { /* ... */ },
                onChatClick = { /* ... */ },
                onEmailClick = { /* ... */ }
            )
        }
    }
}
```


### 6. AlphabetIndex (字母索引)

```kotlin
@Composable
fun AlphabetIndex(
    currentLetter: String,
    onLetterClick: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

**设计规格**:
- 位置: 屏幕右侧,垂直居中
- 宽度: 24dp
- 字母间距: 2dp
- 字母大小: 10sp
- 当前字母: primary 色,加粗
- 其他字母: onSurfaceVariant 色

**实现**:
```kotlin
Column(
    modifier = modifier
        .width(24.dp)
        .padding(vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp)
) {
    ('A'..'Z').forEach { letter ->
        val isSelected = letter.toString() == currentLetter
        Text(
            text = letter.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clickable { onLetterClick(letter.toString()) }
                .padding(vertical = 2.dp)
        )
    }
}
```

### 7. StickyHeader (粘性字母标题)

```kotlin
@Composable
fun ContactGroupHeader(
    letter: String,
    modifier: Modifier = Modifier
)
```

**设计规格**:
- 高度: 48dp
- 背景: surfaceVariant
- 字体: titleMedium, SemiBold
- 颜色: onSurface
- 左边距: 16dp

```kotlin
Box(
    modifier = modifier
        .fillMaxWidth()
        .height(48.dp)
        .background(MaterialTheme.colorScheme.surfaceVariant),
    contentAlignment = Alignment.CenterStart
) {
    Text(
        text = letter,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 16.dp)
    )
}
```

### 8. ContactsList (联系人列表)

```kotlin
@Composable
fun ContactsList(
    contacts: List<ContactUiModel>,
    onContactClick: (ContactUiModel) -> Unit,
    onChatClick: (ContactUiModel) -> Unit,
    onEmailClick: (ContactUiModel) -> Unit,
    modifier: Modifier = Modifier
)
```

**实现**:
```kotlin
val groupedContacts = remember(contacts) {
    contacts.groupBy { 
        it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#" 
    }.toSortedMap()
}

val listState = rememberLazyListState()

Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 88.dp) // FAB 空间
    ) {
        groupedContacts.forEach { (letter, contactsInGroup) ->
            stickyHeader {
                ContactGroupHeader(letter = letter)
            }
            
            itemsIndexed(
                items = contactsInGroup,
                key = { _, contact -> contact.id }
            ) { index, contact ->
                ContactItem(
                    contact = contact,
                    onContactClick = { onContactClick(contact) },
                    onChatClick = { onChatClick(contact) },
                    onEmailClick = { onEmailClick(contact) },
                    modifier = Modifier.animateItemPlacement()
                )
                
                if (index < contactsInGroup.size - 1) {
                    Divider(
                        modifier = Modifier.padding(start = 88.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f)
                    )
                }
            }
        }
    }
    
    // 字母索引
    val currentLetter by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            // 计算当前可见的字母
            groupedContacts.keys.elementAtOrNull(firstVisibleIndex) ?: "A"
        }
    }
    
    AlphabetIndex(
        currentLetter = currentLetter,
        onLetterClick = { letter ->
            // 滚动到对应字母
            val targetIndex = groupedContacts.keys.indexOf(letter)
            if (targetIndex >= 0) {
                scope.launch {
                    listState.animateScrollToItem(targetIndex)
                }
            }
        },
        modifier = Modifier.align(Alignment.CenterEnd)
    )
}
```

### 9. FloatingActionButton (添加联系人)

```kotlin
@Composable
fun AddContactFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**设计规格**:
- 尺寸: 56dp × 56dp (标准 FAB)
- 图标: Icons.Filled.Add, 24dp
- 背景: primaryContainer
- 图标颜色: onPrimaryContainer
- elevation: 6dp
- 位置: 右下角,距离边缘 16dp

**滚动行为**:
```kotlin
val listState = rememberLazyListState()
val fabVisible by remember {
    derivedStateOf {
        listState.firstVisibleItemIndex < 5
    }
}

AnimatedVisibility(
    visible = fabVisible,
    enter = scaleIn(tween(200)) + fadeIn(tween(200)),
    exit = scaleOut(tween(200)) + fadeOut(tween(200))
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "添加联系人"
        )
    }
}
```


### 10. ContactDetailBottomSheet (联系人详情面板)

```kotlin
@Composable
fun ContactDetailBottomSheet(
    contact: ContactUiModel,
    onDismiss: () -> Unit,
    onChatClick: () -> Unit,
    onEmailClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**设计规格**:
- 圆角: 28dp (顶部)
- 背景: surface
- 最大高度: 屏幕高度的 90%
- 拖拽手柄: 32dp × 4dp, 圆角 2dp

**布局结构**:
```kotlin
ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头像和基本信息
        ContactAvatar(
            imageUrl = contact.avatarUrl,
            name = contact.name,
            size = 80.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = contact.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = contact.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 快速操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                icon = Icons.Outlined.Chat,
                label = "聊天",
                onClick = onChatClick
            )
            
            ActionButton(
                icon = Icons.Outlined.Email,
                label = "邮件",
                onClick = onEmailClick
            )
            
            ActionButton(
                icon = Icons.Outlined.Edit,
                label = "编辑",
                onClick = onEditClick
            )
            
            ActionButton(
                icon = Icons.Outlined.Delete,
                label = "删除",
                onClick = onDeleteClick,
                tint = MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 详细信息
        if (contact.phoneNumber != null) {
            DetailItem(
                icon = Icons.Outlined.Phone,
                label = "电话",
                value = contact.phoneNumber
            )
        }
        
        if (contact.address != null) {
            DetailItem(
                icon = Icons.Outlined.LocationOn,
                label = "地址",
                value = contact.address
            )
        }
        
        if (contact.notes != null) {
            DetailItem(
                icon = Icons.Outlined.Notes,
                label = "备注",
                value = contact.notes
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = tint.copy(alpha = 0.12f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
```

### 11. EmptyState (空状态)

```kotlin
@Composable
fun ContactsEmptyState(
    onAddContactClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**设计**:
```kotlin
Column(
    modifier = modifier
        .fillMaxSize()
        .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Icon(
        imageVector = Icons.Outlined.People,
        contentDescription = null,
        modifier = Modifier.size(120.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
    
    Spacer(modifier = Modifier.height(24.dp))
    
    Text(
        text = "还没有联系人",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Text(
        text = "点击右下角的按钮添加联系人",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}
```

### 12. LoadingState (加载状态)

```kotlin
@Composable
fun ContactsLoadingState(
    modifier: Modifier = Modifier
)
```

**骨架屏设计**:
```kotlin
LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(vertical = 8.dp)
) {
    items(10) {
        ContactItemSkeleton()
    }
}

@Composable
private fun ContactItemSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像骨架
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .shimmer()
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            // 姓名骨架
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 邮箱骨架
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 操作按钮骨架
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .shimmer()
                )
            }
        }
    }
}

@Composable
fun Modifier.shimmer(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    return background(
        MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
    )
}
```


## Data Models

### ContactUiModel

```kotlin
data class ContactUiModel(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val phoneNumber: String?,
    val address: String?,
    val notes: String?,
    val isOnline: Boolean = false,
    val lastContactTime: Instant?,
    val isFavorite: Boolean = false,
    val conversationId: String? = null // 关联的对话ID
)
```

### ContactsUiState

```kotlin
data class ContactsUiState(
    val contacts: List<ContactUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<ContactUiModel> = emptyList(),
    val isSearchActive: Boolean = false,
    val selectedContact: ContactUiModel? = null,
    val showDetailSheet: Boolean = false
)
```

## Domain Layer

### Use Cases

#### 1. GetContactsUseCase

```kotlin
class GetContactsUseCase(
    private val emailRepository: EmailRepository
) {
    operator fun invoke(): Flow<Result<List<ContactUiModel>>> {
        // 1. 从邮件中提取所有联系人
        // 2. 去重并合并信息
        // 3. 按姓名排序
        // 4. 转换为 ContactUiModel
    }
}
```

#### 2. SearchContactsUseCase

```kotlin
class SearchContactsUseCase(
    private val emailRepository: EmailRepository
) {
    operator fun invoke(query: String): Flow<Result<List<ContactUiModel>>> {
        // 1. 搜索姓名匹配的联系人
        // 2. 搜索邮箱匹配的联系人
        // 3. 合并结果并排序
    }
}
```

#### 3. GetContactDetailUseCase

```kotlin
class GetContactDetailUseCase(
    private val emailRepository: EmailRepository
) {
    suspend operator fun invoke(contactId: String): Result<ContactUiModel> {
        // 获取联系人完整信息
    }
}
```

### Mappers

#### ContactMapper

```kotlin
object ContactMapper {
    fun fromEmail(email: Email): ContactUiModel {
        return ContactUiModel(
            id = email.from.address,
            name = email.from.name ?: email.from.address,
            email = email.from.address,
            avatarUrl = null,
            phoneNumber = null,
            address = null,
            notes = null,
            isOnline = false,
            lastContactTime = email.timestamp,
            isFavorite = false,
            conversationId = email.threadId
        )
    }
    
    fun mergeContacts(contacts: List<ContactUiModel>): List<ContactUiModel> {
        return contacts
            .groupBy { it.email }
            .map { (_, contactGroup) ->
                contactGroup.reduce { acc, contact ->
                    acc.copy(
                        name = contact.name.takeIf { it.isNotBlank() } ?: acc.name,
                        lastContactTime = maxOf(
                            acc.lastContactTime ?: Instant.DISTANT_PAST,
                            contact.lastContactTime ?: Instant.DISTANT_PAST
                        )
                    )
                }
            }
            .sortedBy { it.name }
    }
}
```

## ViewModels

### ContactsViewModel

```kotlin
@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val searchContactsUseCase: SearchContactsUseCase,
    private val getContactDetailUseCase: GetContactDetailUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()
    
    init {
        loadContacts()
    }
    
    fun loadContacts(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !refresh, isRefreshing = refresh) }
            
            getContactsUseCase().collect { result ->
                result.fold(
                    onSuccess = { contacts ->
                        _uiState.update {
                            it.copy(
                                contacts = contacts,
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = error.message
                            )
                        }
                    }
                )
            }
        }
    }
    
    fun searchContacts(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        
        viewModelScope.launch {
            searchContactsUseCase(query).collect { result ->
                result.fold(
                    onSuccess = { results ->
                        _uiState.update { it.copy(searchResults = results) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(error = error.message) }
                    }
                )
            }
        }
    }
    
    fun setSearchActive(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active) }
        if (!active) {
            _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
        }
    }
    
    fun showContactDetail(contact: ContactUiModel) {
        _uiState.update {
            it.copy(
                selectedContact = contact,
                showDetailSheet = true
            )
        }
    }
    
    fun hideContactDetail() {
        _uiState.update {
            it.copy(
                selectedContact = null,
                showDetailSheet = false
            )
        }
    }
    
    fun navigateToChat(contact: ContactUiModel, navController: NavController) {
        contact.conversationId?.let { conversationId ->
            navController.navigate(Screen.ChatDetail.createRoute(conversationId))
        }
    }
    
    fun navigateToCompose(contact: ContactUiModel, navController: NavController) {
        navController.navigate(
            Screen.Compose.createRoute().plus("?to=${contact.email}")
        )
    }
}
```

## Error Handling

### 错误类型

1. **加载错误**: 获取联系人列表失败
2. **搜索错误**: 搜索功能失败
3. **网络错误**: 无法同步联系人数据
4. **权限错误**: 缺少联系人访问权限

### 错误处理策略

```kotlin
// 显示 Snackbar 提示
LaunchedEffect(uiState.error) {
    uiState.error?.let { error ->
        val result = snackbarHostState.showSnackbar(
            message = error,
            actionLabel = "重试",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.loadContacts(refresh = true)
        }
    }
}

// 空状态处理
if (uiState.contacts.isEmpty() && !uiState.isLoading) {
    ContactsEmptyState(
        onAddContactClick = { /* 导航到添加联系人 */ }
    )
}
```

## Testing Strategy

### 单元测试

#### 1. ViewModel 测试

```kotlin
@Test
fun `loadContacts should update state with contacts`() = runTest {
    // Given
    val mockContacts = listOf(/* mock data */)
    coEvery { getContactsUseCase() } returns flowOf(Result.success(mockContacts))
    
    // When
    viewModel.loadContacts()
    
    // Then
    assertEquals(mockContacts, viewModel.uiState.value.contacts)
    assertFalse(viewModel.uiState.value.isLoading)
}

@Test
fun `searchContacts should filter contacts by query`() = runTest {
    // Given
    val mockResults = listOf(/* filtered contacts */)
    coEvery { searchContactsUseCase(any()) } returns flowOf(Result.success(mockResults))
    
    // When
    viewModel.searchContacts("test")
    
    // Then
    assertEquals("test", viewModel.uiState.value.searchQuery)
    assertEquals(mockResults, viewModel.uiState.value.searchResults)
}
```

#### 2. UseCase 测试

```kotlin
@Test
fun `GetContactsUseCase should extract and merge contacts from emails`() = runTest {
    // Given
    val mockEmails = listOf(/* emails with duplicate senders */)
    coEvery { emailRepository.getEmails(any(), any(), any()) } returns flowOf(Result.success(mockEmails))
    
    // When
    val result = getContactsUseCase().first()
    
    // Then
    assertTrue(result.isSuccess)
    // 验证去重逻辑
}
```

### UI 测试

```kotlin
@Test
fun `clicking contact should show detail bottom sheet`() {
    composeTestRule.setContent {
        ContactsScreen(/* ... */)
    }
    
    // When
    composeTestRule.onNodeWithText("Test Contact").performClick()
    
    // Then
    composeTestRule.onNodeWithText("聊天").assertIsDisplayed()
    composeTestRule.onNodeWithText("邮件").assertIsDisplayed()
}

@Test
fun `clicking chat button should navigate to chat detail`() {
    // Given
    val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    
    composeTestRule.setContent {
        ContactsScreen(navController = navController)
    }
    
    // When
    composeTestRule.onNodeWithContentDescription("发起聊天").performClick()
    
    // Then
    assertEquals(
        "chat_detail/thread1",
        navController.currentBackStackEntry?.destination?.route
    )
}
```


## Performance Optimization

### 1. LazyColumn 优化

```kotlin
LazyColumn(
    state = listState,
    // 使用稳定的 key 避免重组
    key = { contact -> contact.id }
) {
    groupedContacts.forEach { (letter, contacts) ->
        stickyHeader(key = letter) {
            ContactGroupHeader(letter = letter)
        }
        
        items(
            items = contacts,
            key = { it.id }
        ) { contact ->
            ContactItem(
                contact = contact,
                // 使用 remember 缓存计算结果
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}
```

### 2. 头像加载优化

```kotlin
// 使用 Coil 的内存和磁盘缓存
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(contact.avatarUrl)
        .memoryCacheKey(contact.id)
        .diskCacheKey(contact.id)
        .crossfade(true)
        .size(56.dp.value.toInt())
        .build(),
    contentDescription = null,
    modifier = Modifier
        .size(56.dp)
        .clip(CircleShape)
)
```

### 3. 搜索防抖

```kotlin
// 使用 debounce 避免频繁搜索
LaunchedEffect(searchQuery) {
    snapshotFlow { searchQuery }
        .debounce(300)
        .collectLatest { query ->
            if (query.isNotBlank()) {
                viewModel.searchContacts(query)
            }
        }
}
```

### 4. 状态优化

```kotlin
// 使用 derivedStateOf 避免不必要的重组
val groupedContacts by remember {
    derivedStateOf {
        contacts.groupBy { 
            it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#" 
        }.toSortedMap()
    }
}

val filteredContacts by remember {
    derivedStateOf {
        if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }
}
```

### 5. 下拉刷新优化

```kotlin
val pullRefreshState = rememberPullRefreshState(
    refreshing = uiState.isRefreshing,
    onRefresh = { viewModel.loadContacts(refresh = true) }
)

Box(
    modifier = Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState)
) {
    ContactsList(/* ... */)
    
    PullRefreshIndicator(
        refreshing = uiState.isRefreshing,
        state = pullRefreshState,
        modifier = Modifier.align(Alignment.TopCenter),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    )
}
```

## Accessibility

### 1. 语义化标签

```kotlin
ContactItem(
    modifier = Modifier.semantics {
        contentDescription = "联系人 ${contact.name}, 邮箱 ${contact.email}"
        role = Role.Button
    }
)

IconButton(
    onClick = onChatClick,
    modifier = Modifier.semantics {
        contentDescription = "与 ${contact.name} 发起聊天"
    }
) {
    Icon(/* ... */)
}
```

### 2. 触摸目标大小

所有可交互元素最小触摸目标为 48dp × 48dp:

```kotlin
IconButton(
    onClick = { /* ... */ },
    modifier = Modifier.size(48.dp) // 最小触摸目标
) {
    Icon(
        imageVector = Icons.Outlined.Chat,
        contentDescription = "发起聊天",
        modifier = Modifier.size(24.dp) // 图标大小
    )
}
```

### 3. 颜色对比度

确保文字和背景的对比度符合 WCAG AA 标准 (至少 4.5:1):

```kotlin
// 姓名文本
color = MaterialTheme.colorScheme.onSurface // 高对比度

// 邮箱文本
color = MaterialTheme.colorScheme.onSurfaceVariant // 中等对比度

// 在线状态
color = Color(0xFF4CAF50) // 确保与背景对比度足够
```

### 4. 屏幕阅读器支持

```kotlin
// 为头像提供描述
ContactAvatar(
    modifier = Modifier.semantics {
        contentDescription = "${contact.name} 的头像"
    }
)

// 为在线状态提供描述
OnlineIndicator(
    modifier = Modifier.semantics {
        contentDescription = if (contact.isOnline) "在线" else "离线"
    }
)
```

### 5. 焦点管理

```kotlin
// 搜索框自动获取焦点
val focusRequester = remember { FocusRequester() }

LaunchedEffect(isSearchActive) {
    if (isSearchActive) {
        focusRequester.requestFocus()
    }
}

TextField(
    value = searchQuery,
    onValueChange = { /* ... */ },
    modifier = Modifier.focusRequester(focusRequester)
)
```

## Animation and Transitions

### 1. 页面转场动画

```kotlin
// 从联系人列表到详情页
composable(
    route = Screen.Contacts.route,
    enterTransition = {
        slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300))
    },
    exitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(300))
    }
)
```

### 2. 列表项进入动画

```kotlin
@Composable
fun AnimatedContactItem(
    contact: ContactUiModel,
    index: Int,
    /* ... */
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(index * 30L)
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)) + 
                slideInVertically(
                    initialOffsetY = { it / 6 },
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
    ) {
        ContactItem(contact = contact, /* ... */)
    }
}
```

### 3. 底部面板动画

```kotlin
ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(),
    // 使用默认的滑入滑出动画
) {
    // 内容淡入动画
    var contentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        contentVisible = true
    }
    
    AnimatedVisibility(
        visible = contentVisible,
        enter = fadeIn(tween(200)) + expandVertically(tween(200))
    ) {
        ContactDetailContent(/* ... */)
    }
}
```

### 4. FAB 动画

```kotlin
// 滚动时自动隐藏/显示
val fabVisible by remember {
    derivedStateOf {
        listState.firstVisibleItemIndex < 5
    }
}

AnimatedVisibility(
    visible = fabVisible,
    enter = scaleIn(
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(200)),
    exit = scaleOut(
        animationSpec = tween(200, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(200))
) {
    FloatingActionButton(/* ... */)
}
```

### 5. 搜索栏展开动画

```kotlin
SearchBar(
    query = searchQuery,
    onQueryChange = onQueryChange,
    active = isSearchActive,
    onActiveChange = onSearchActiveChange,
    // 使用默认的展开/收起动画
    modifier = Modifier
        .fillMaxWidth()
        .animateContentSize(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
)
```

## Implementation Notes

### 性能基准

- **初始加载时间**: < 500ms (100个联系人)
- **滚动帧率**: 60fps (稳定)
- **搜索响应时间**: < 300ms (包含防抖)
- **动画流畅度**: 60fps (无掉帧)
- **内存占用**: < 50MB (1000个联系人)

### 主题适配

1. **浅色模式**: 使用 surface 和 surfaceVariant 背景
2. **深色模式**: 使用深色 surface,避免过亮的颜色
3. **动态颜色**: 支持 Android 12+ 的 Material You 动态颜色
4. **对比度模式**: 支持高对比度模式

### 导航集成

```kotlin
// 在 NavGraph 中添加联系人路由
composable(Screen.Contacts.route) {
    ContactsScreen(
        navController = navController,
        onChatClick = { contact ->
            contact.conversationId?.let { conversationId ->
                navController.navigate(Screen.ChatDetail.createRoute(conversationId))
            }
        },
        onEmailClick = { contact ->
            navController.navigate(
                Screen.Compose.createRoute().plus("?to=${contact.email}")
            )
        }
    )
}
```

### 底部导航栏集成

```kotlin
// 在 FleurBottomNavigationBar 中添加联系人按钮
private val navItems = listOf(
    BottomNavItem("收件箱", Icons.Filled.Inbox, Icons.Outlined.Inbox, 0),
    BottomNavItem("聊天", Icons.Filled.Chat, Icons.Outlined.Chat, 1),
    BottomNavItem("联系人", Icons.Filled.People, Icons.Outlined.People, 2),
    BottomNavItem("日历", Icons.Filled.CalendarToday, Icons.Outlined.CalendarToday, 3)
)
```

## Design Decisions

### 为什么使用字母分组和索引?

- 提供快速定位功能,在大量联系人中快速找到目标
- 符合用户对联系人列表的心智模型
- 提供清晰的视觉组织结构

### 为什么在列表项中直接显示快速操作按钮?

- 减少操作步骤,提高效率
- 聊天和邮件是最常用的操作,应该一键可达
- 避免用户每次都需要打开详情面板

### 为什么使用底部面板而不是新页面显示详情?

- 保持上下文,用户可以快速返回列表
- 减少页面跳转,提供更流畅的体验
- 符合 Material 3 的设计模式

### 为什么使用首字母头像?

- 在没有照片时提供视觉识别
- 使用颜色编码增强记忆
- 提供更美观的视觉效果

### 为什么添加在线状态指示器?

- 帮助用户判断是否适合发起聊天
- 提供实时的联系人状态信息
- 增强社交属性

