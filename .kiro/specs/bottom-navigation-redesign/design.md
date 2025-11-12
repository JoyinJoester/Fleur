# Design Document

## Overview

重新设计底部导航栏组件，采用清晰的层次结构和正确的 WindowInsets 处理方式，确保在所有设备上都能完美显示，无视觉间隙或布局问题。

## Architecture

### Component Hierarchy

```
Column (处理系统导航栏区域)
├── background: surfaceContainer
├── navigationBarsPadding()
└── Box (导航内容区域)
    ├── height: 72dp
    ├── clip: RoundedCornerShape(topStart: 20dp, topEnd: 20dp)
    ├── background: surfaceContainer
    └── Row (导航项容器)
        ├── Arrangement.SpaceEvenly
        └── NavigationItem × 4
            ├── Column
            ├── Icon (24dp)
            ├── Spacer (4dp)
            └── Text (11sp)
```

### Key Design Decisions

1. **双层结构**：
   - 外层 Column 负责扩展到系统导航栏区域
   - 内层 Box 负责导航内容的圆角和布局

2. **背景色策略**：
   - 使用 `MaterialTheme.colorScheme.surfaceContainer`
   - 确保外层和内层使用相同背景色，无缝衔接

3. **高度设计**：
   - 导航内容区域：72dp（标准 Material 3 高度）
   - 系统导航栏区域：由 navigationBarsPadding() 自动处理

4. **圆角处理**：
   - 仅在内层 Box 应用圆角
   - 使用 clip() 而非 shape，确保正确裁剪

## Components and Interfaces

### FleurBottomNavigationBar

主组件，负责整体布局和状态管理。

```kotlin
@Composable
fun FleurBottomNavigationBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
)
```

**参数**：
- `selectedItem`: 当前选中项的索引（0-3）
- `onItemSelected`: 选中回调，传入新选中项的索引
- `modifier`: 外部修饰符

**实现要点**：
- 使用 `remember` 缓存导航项列表
- 外层 Column 应用 `navigationBarsPadding()`
- 内层 Box 固定高度 72dp，应用顶部圆角

### NavigationItem

单个导航项组件。

```kotlin
@Composable
private fun NavigationItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**视觉状态**：
- 选中：primary 色图标，primaryContainer 背景，SemiBold 字体
- 未选中：onSurfaceVariant 色图标，透明背景，Normal 字体

**动画**：
- 图标缩放：1.0x → 1.12x（250ms）
- 颜色过渡：250ms
- 背景色过渡：250ms

## Data Models

### BottomNavItem

```kotlin
private data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val index: Int
)
```

**导航项配置**：
1. Inbox: Icons.Filled.Inbox / Icons.Outlined.Inbox
2. Chat: Icons.Filled.Chat / Icons.Outlined.Chat
3. Contacts: Icons.Filled.People / Icons.Outlined.People
4. Calendar: Icons.Filled.CalendarToday / Icons.Outlined.CalendarToday

## Layout Specifications

### Dimensions

- 导航栏内容高度：72dp
- 圆角半径：20dp（顶部左右）
- 图标尺寸：24dp
- 图标容器尺寸：48dp
- 图标与标签间距：4dp
- 标签字体大小：11sp
- 水平内边距：0dp（使用 SpaceEvenly）
- 垂直内边距：12dp（顶部）、8dp（底部）

### Colors

- 背景色：`MaterialTheme.colorScheme.surfaceContainer`
- 选中图标：`MaterialTheme.colorScheme.primary`
- 未选中图标：`MaterialTheme.colorScheme.onSurfaceVariant`
- 选中背景：`MaterialTheme.colorScheme.primaryContainer`
- 选中文本：`MaterialTheme.colorScheme.primary`
- 未选中文本：`MaterialTheme.colorScheme.onSurfaceVariant`

### Animation Specs

```kotlin
animationSpec = tween(
    durationMillis = 250,
    easing = FastOutSlowInEasing
)
```

## Error Handling

### WindowInsets 处理

- 使用 `navigationBarsPadding()` 自动适配不同设备
- 确保在手势导航和按钮导航模式下都能正确显示

### 主题兼容性

- 使用 MaterialTheme.colorScheme 确保深色/浅色主题兼容
- 避免硬编码颜色值

## Testing Strategy

### 视觉测试

1. 在不同设备上验证系统导航栏区域无间隙
2. 验证圆角在不同屏幕尺寸下的显示
3. 验证深色/浅色主题下的颜色正确性

### 交互测试

1. 验证点击响应和状态切换
2. 验证动画流畅性
3. 验证选中状态的视觉反馈

### 布局测试

1. 验证图标和标签的垂直居中
2. 验证四个导航项的均匀分布
3. 验证在不同屏幕宽度下的适配
