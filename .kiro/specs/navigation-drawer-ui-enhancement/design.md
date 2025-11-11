# 设计文档

## 概述

本设计文档描述了Fleur邮件应用侧边栏（Navigation Drawer）UI优化的详细设计方案。重点优化账户信息区域，采用Material 3大色块风格，实现可展开的账户选择器，并提升整体视觉美观度和交互流畅性。

## 架构

### 组件层次结构

```
FleurNavigationDrawer
├── ModalDrawerSheet
│   └── Column (可滚动)
│       ├── ExpandableAccountCard (新组件)
│       │   ├── CollapsedAccountView (折叠状态)
│       │   └── ExpandedAccountList (展开状态)
│       ├── HorizontalDivider
│       ├── FolderSection (优化)
│       ├── HorizontalDivider
│       └── SettingsSection (优化)
```

### 核心设计原则

1. **Material 3 设计语言**: 使用大色块、圆角卡片、动态配色
2. **渐进式展开**: 默认折叠，点击展开，流畅动画
3. **空间优化**: 限制最大高度，支持内部滚动
4. **视觉层次**: 清晰的信息层级和视觉引导

## 组件和接口

### 1. ExpandableAccountCard (新组件)

可展开的账户卡片组件，是本次优化的核心。

#### 接口定义

```kotlin
@Composable
fun ExpandableAccountCard(
    currentAccount: Account?,
    accounts: List<Account>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onSwitchAccount: (String) -> Unit,
    onNavigateToAccountManagement: () -> Unit,
    modifier: Modifier = Modifier
)
```

#### 状态管理

```kotlin
// 在 FleurNavigationDrawer 中管理展开状态
var isAccountCardExpanded by remember { mutableStateOf(false) }
```

#### 视觉设计

**折叠状态 (CollapsedAccountView)**:
- 使用 `Card` 组件，应用 Material 3 的 `cardColors`
- 背景色：`primaryContainer` (大色块风格)
- 圆角：16.dp
- 内边距：16.dp
- 布局：
  - 左侧：圆形头像 (48.dp)，使用账户主题色
  - 中间：账户名称和邮箱地址
  - 右侧：展开/收起图标 (AnimatedContent)

**展开状态 (ExpandedAccountList)**:
- 最大高度：240.dp (约显示3-4个账户)
- 使用 `LazyColumn` 实现滚动
- 每个账户项：
  - 高度：72.dp
  - 圆角：12.dp
  - 选中状态：`secondaryContainer` 背景色
  - 未选中状态：透明背景，悬停时显示 `surfaceVariant`

### 2. 动画设计

#### 展开/收起动画

```kotlin
AnimatedVisibility(
    visible = isExpanded,
    enter = expandVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    ) + fadeIn(),
    exit = shrinkVertically(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeOut()
)
```

#### 图标旋转动画

```kotlin
val rotation by animateFloatAsState(
    targetValue = if (isExpanded) 180f else 0f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
)
```

#### 账户切换动画

```kotlin
AnimatedContent(
    targetState = currentAccount,
    transitionSpec = {
        slideInVertically { height -> height } + fadeIn() with
        slideOutVertically { height -> -height } + fadeOut()
    }
)
```

### 3. FolderSection 优化

#### 视觉增强

- 选中状态：左侧添加 4.dp 宽的指示条
- 图标大小：24.dp
- 间距：图标与文字间距 12.dp
- Badge 样式：使用 `Badge` 组件，主题色背景

#### 交互增强

```kotlin
NavigationDrawerItem(
    modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(12.dp)),
    colors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedContainerColor = Color.Transparent
    )
)
```

### 4. 毛玻璃效果背景

虽然 `ModalDrawerSheet` 已有默认背景，但可以通过自定义增强：

```kotlin
ModalDrawerSheet(
    modifier = Modifier
        .width(320.dp)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        )
)
```

## 数据模型

### Account 模型扩展

现有的 `Account` 模型已包含所需字段：

```kotlin
data class Account(
    val id: String,
    val email: String,
    val displayName: String,
    val color: Color,
    val isDefault: Boolean,
    // ... 其他字段
)
```

### UI 状态

```kotlin
data class NavigationDrawerUiState(
    val isAccountCardExpanded: Boolean = false,
    val currentAccount: Account? = null,
    val accounts: List<Account> = emptyList(),
    val unreadCounts: Map<String, Int> = emptyMap(),
    val selectedFolder: String = "inbox"
)
```

## 错误处理

### 账户加载失败

- 显示占位符："未登录"
- 提供"添加账户"按钮
- 禁用展开功能

### 账户列表为空

- 折叠状态显示"添加账户"提示
- 点击直接导航到账户管理页面

### 动画性能

- 使用 `remember` 缓存动画状态
- 避免在动画过程中进行重组
- 使用 `derivedStateOf` 优化计算

## 测试策略

### 单元测试

1. **ExpandableAccountCard 组件测试**
   - 测试折叠/展开状态切换
   - 测试账户切换回调
   - 测试空账户列表处理

2. **动画测试**
   - 验证展开动画触发
   - 验证收起动画触发
   - 验证动画完成后的状态

### UI 测试

1. **交互测试**
   - 点击账户卡片展开
   - 点击外部区域收起
   - 滚动账户列表
   - 切换账户

2. **视觉回归测试**
   - 截图对比折叠状态
   - 截图对比展开状态
   - 截图对比深色模式

### 性能测试

1. **滚动性能**
   - 测试大量账户（10+）时的滚动流畅度
   - 测试动画帧率

2. **内存测试**
   - 验证展开/收起不会导致内存泄漏

## 实现细节

### 颜色方案

使用 Material 3 动态配色：

```kotlin
// 折叠状态卡片
containerColor = MaterialTheme.colorScheme.primaryContainer
contentColor = MaterialTheme.colorScheme.onPrimaryContainer

// 展开状态选中项
containerColor = MaterialTheme.colorScheme.secondaryContainer
contentColor = MaterialTheme.colorScheme.onSecondaryContainer

// 展开状态未选中项
containerColor = Color.Transparent
contentColor = MaterialTheme.colorScheme.onSurface
```

### 尺寸规范

```kotlin
object DrawerDimens {
    val AccountCardCornerRadius = 16.dp
    val AccountCardPadding = 16.dp
    val AccountCardAvatarSize = 48.dp
    val AccountCardExpandedMaxHeight = 240.dp
    
    val AccountItemHeight = 72.dp
    val AccountItemCornerRadius = 12.dp
    val AccountItemAvatarSize = 40.dp
    
    val FolderItemCornerRadius = 12.dp
    val FolderItemIndicatorWidth = 4.dp
    val FolderItemPadding = 12.dp
    
    val SectionSpacing = 12.dp
    val ItemSpacing = 4.dp
}
```

### 动画参数

```kotlin
object DrawerAnimations {
    val ExpandSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val CollapseSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val IconRotationSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    const val FadeInDuration = 150
    const val FadeOutDuration = 100
}
```

### 可访问性

1. **语义标签**
   - 为展开/收起按钮添加 `contentDescription`
   - 为账户项添加语义角色

2. **触摸目标**
   - 最小触摸目标：48.dp
   - 账户卡片整体可点击

3. **屏幕阅读器支持**
   - 使用 `semantics` 修饰符
   - 提供状态变化通知

```kotlin
.semantics {
    stateDescription = if (isExpanded) "已展开" else "已折叠"
    role = Role.Button
}
```

## 深色模式适配

### 颜色调整

深色模式下，Material 3 会自动调整配色，但需要注意：

1. **毛玻璃效果**: 降低透明度，增加模糊度
2. **阴影**: 使用更明显的边框代替阴影
3. **对比度**: 确保文字和背景有足够对比度

### 测试

- 在浅色和深色模式下分别测试
- 验证动态配色的正确应用

## 性能优化

### 1. 懒加载

```kotlin
LazyColumn(
    modifier = Modifier.heightIn(max = DrawerDimens.AccountCardExpandedMaxHeight)
) {
    items(
        items = accounts,
        key = { it.id }
    ) { account ->
        AccountListItem(...)
    }
}
```

### 2. 记忆化

```kotlin
val sortedAccounts = remember(accounts) {
    accounts.sortedByDescending { it.isDefault }
}
```

### 3. 避免过度重组

```kotlin
@Composable
fun AccountListItem(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 使用 key 参数避免不必要的重组
}
```

## 迁移策略

### 向后兼容

1. 保留现有的 `FleurNavigationDrawer` 接口
2. 内部重构为新的组件结构
3. 不影响外部调用代码

### 渐进式实现

1. 第一阶段：实现 `ExpandableAccountCard` 组件
2. 第二阶段：优化 `FolderSection` 视觉效果
3. 第三阶段：添加动画和交互增强
4. 第四阶段：性能优化和测试

## 未来扩展

1. **手势支持**: 支持滑动展开/收起账户卡片
2. **快捷操作**: 长按账户显示快捷菜单（设为默认、删除等）
3. **账户分组**: 支持按域名或标签分组显示账户
4. **搜索功能**: 账户过多时提供搜索框
5. **自定义主题**: 允许用户自定义账户卡片颜色

