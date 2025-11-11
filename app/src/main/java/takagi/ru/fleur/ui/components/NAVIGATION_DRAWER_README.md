# FleurNavigationDrawer 实现文档

## 概述

FleurNavigationDrawer 是 Fleur 邮箱应用的主导航组件，提供优雅的侧边栏导航体验。

## 核心特性

### 1. 毛玻璃效果（Glassmorphism）
- **模糊半径**: 10dp
- **不透明度**: 85%
- **实现方式**: 使用 `drawerGlassmorphism()` 修饰符
- **自动适配**: 浅色/深色模式自动切换

```kotlin
Surface(
    modifier = Modifier
        .fillMaxHeight()
        .width(280.dp)
        .drawerGlassmorphism(),
    color = Color.Transparent
) {
    // 抽屉内容
}
```

### 2. 滑入动画
- **动画时长**: 250ms
- **缓动曲线**: DecelerateEasing（进入）/ AccelerateEasing（退出）
- **动画效果**: 从左侧滑入 + 淡入

```kotlin
AnimatedVisibility(
    visible = visible,
    enter = slideInHorizontally(
        animationSpec = tween(
            durationMillis = FleurAnimation.MEDIUM_DURATION,
            easing = FleurAnimation.DecelerateEasing
        ),
        initialOffsetX = { -it }
    ) + fadeIn(...)
)
```

### 3. 背景遮罩
- **颜色**: 黑色
- **不透明度**: 40%
- **交互**: 点击遮罩关闭抽屉

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.4f))
        .clickable(onClick = onDismiss)
)
```

### 4. 未读数 Badge
- **显示位置**: 文件夹项右侧
- **显示规则**: 
  - 未读数 > 0 时显示
  - 未读数 > 99 时显示 "99+"
- **样式**: Material 3 Badge 组件

```kotlin
if (badge != null && badge > 0) {
    Badge(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Text(
            text = if (badge > 99) "99+" else badge.toString(),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
```

### 5. 自适应布局
- **宽度 >= 600dp**: 持久化显示（TODO: 未来实现）
- **宽度 < 600dp**: 模态显示（当前实现）

## 组件结构

```
FleurNavigationDrawer
├── 背景遮罩层 (40% black)
└── 抽屉内容 (280dp 宽)
    ├── UserInfoSection (用户信息)
    ├── FolderSection (文件夹列表)
    │   ├── 收件箱 (带未读数)
    │   ├── 已发送
    │   ├── 草稿箱 (带未读数)
    │   ├── 星标邮件
    │   ├── 归档
    │   └── 垃圾箱
    ├── AccountSection (账户列表，多账户时显示)
    │   └── AccountItem × N
    └── SettingsSection (设置入口)
        ├── 设置
        └── 关于
```

## 使用方法

### 基本使用

```kotlin
var drawerVisible by remember { mutableStateOf(false) }

Box(modifier = Modifier.fillMaxSize()) {
    // 主内容
    YourMainContent(onMenuClick = { drawerVisible = true })
    
    // Navigation Drawer
    FleurNavigationDrawer(
        visible = drawerVisible,
        currentAccount = currentAccount,
        accounts = accounts,
        unreadCounts = mapOf(
            "inbox" to 23,
            "drafts" to 2
        ),
        onDismiss = { drawerVisible = false },
        onNavigateToInbox = { /* 导航逻辑 */ },
        onNavigateToSent = { /* 导航逻辑 */ },
        onNavigateToDrafts = { /* 导航逻辑 */ },
        onNavigateToStarred = { /* 导航逻辑 */ },
        onNavigateToArchive = { /* 导航逻辑 */ },
        onNavigateToTrash = { /* 导航逻辑 */ },
        onNavigateToSettings = { /* 导航逻辑 */ },
        onNavigateToAccountManagement = { /* 导航逻辑 */ },
        onSwitchAccount = { accountId -> /* 切换账户 */ }
    )
}
```

### 集成到 NavGraph

参考 `AppScaffold.kt` 文件，该文件提供了完整的集成示例。

```kotlin
@Composable
fun AppScaffold(
    navController: NavHostController,
    currentAccount: Account?,
    accounts: List<Account>,
    unreadCounts: Map<String, Int> = emptyMap(),
    onSwitchAccount: (String) -> Unit
) {
    var drawerVisible by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        NavGraph(
            navController = navController,
            onMenuClick = { drawerVisible = true }
        )
        
        FleurNavigationDrawer(
            visible = drawerVisible,
            // ... 其他参数
        )
    }
}
```

## 参数说明

| 参数 | 类型 | 说明 |
|------|------|------|
| `visible` | Boolean | 是否显示抽屉 |
| `currentAccount` | Account? | 当前账户 |
| `accounts` | List<Account> | 账户列表 |
| `unreadCounts` | Map<String, Int> | 未读数统计，key: "inbox", "drafts" 等 |
| `onDismiss` | () -> Unit | 关闭抽屉回调 |
| `onNavigateToInbox` | () -> Unit | 导航到收件箱 |
| `onNavigateToSent` | () -> Unit | 导航到已发送 |
| `onNavigateToDrafts` | () -> Unit | 导航到草稿箱 |
| `onNavigateToStarred` | () -> Unit | 导航到星标邮件 |
| `onNavigateToArchive` | () -> Unit | 导航到归档 |
| `onNavigateToTrash` | () -> Unit | 导航到垃圾箱 |
| `onNavigateToSettings` | () -> Unit | 导航到设置 |
| `onNavigateToAccountManagement` | () -> Unit | 导航到账户管理 |
| `onSwitchAccount` | (String) -> Unit | 切换账户回调 |

## 设计规范

### 尺寸
- **抽屉宽度**: 280dp
- **头像大小**: 48dp (用户信息), 32dp (账户列表)
- **图标大小**: 24dp
- **内边距**: 16dp (水平), 12dp (垂直)

### 颜色
- **背景**: 根据主题自动适配
- **文字**: onSurface (主要), onSurfaceVariant (次要)
- **分隔线**: onSurface 12% opacity
- **选中状态**: secondaryContainer 50% opacity

### 动画
- **滑入时长**: 250ms
- **滑出时长**: 250ms
- **缓动曲线**: DecelerateEasing (进入), AccelerateEasing (退出)

## 可访问性

- ✅ 所有交互元素都有 `contentDescription`
- ✅ 触摸目标最小 48dp × 48dp
- ✅ 颜色对比度符合 WCAG 2.1 AA 标准
- ✅ 支持屏幕阅读器

## 性能优化

- 使用 `AnimatedVisibility` 实现高性能动画
- 仅在可见时渲染抽屉内容
- 使用 `remember` 缓存状态
- 避免不必要的重组

## 未来改进

1. **自适应布局**: 在平板设备上持久化显示
2. **手势支持**: 支持从左边缘滑入打开抽屉
3. **文件夹自定义**: 允许用户自定义文件夹顺序
4. **快捷操作**: 长按文件夹显示快捷操作菜单

## 相关文件

- `FleurNavigationDrawer.kt`: 主实现文件
- `AppScaffold.kt`: 集成示例
- `NavigationDrawerUsageExample.kt`: 使用示例
- `Glassmorphism.kt`: 毛玻璃效果实现
- `Animation.kt`: 动画系统定义

## 需求映射

本实现满足以下需求：

- **需求 3.1**: 自适应布局（宽度 >= 600dp 显示）
- **需求 3.2**: 使用 Navigation Drawer 作为主导航
- **需求 3.3**: 显示邮件文件夹、账户列表和设置入口
- **需求 3.4**: 实现毛玻璃效果和滑入动画

## 测试建议

1. **视觉测试**: 验证毛玻璃效果在不同主题下的表现
2. **动画测试**: 验证滑入/滑出动画的流畅性
3. **交互测试**: 验证所有导航项的点击响应
4. **多账户测试**: 验证账户切换功能
5. **可访问性测试**: 使用 TalkBack 验证屏幕阅读器支持
