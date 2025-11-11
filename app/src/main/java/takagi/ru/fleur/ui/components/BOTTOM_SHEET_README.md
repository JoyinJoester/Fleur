# Modal Bottom Sheet 实现文档

## 概述

Fleur 邮箱应用实现了三种 Modal Bottom Sheet 组件，用于不同的交互场景。所有组件都采用统一的毛玻璃效果和动画设计。

## 组件列表

### 1. EmailActionsBottomSheet - 邮件操作菜单

**用途**: 提供邮件的快捷操作选项

**操作项**:
- 回复
- 转发
- 归档
- 标记/取消星标
- 删除

**使用场景**:
- 邮件详情页面
- 邮件列表项长按
- 邮件卡片操作按钮

### 2. AccountSelectorBottomSheet - 账户选择器

**用途**: 选择发件账户

**功能**:
- 显示所有可用账户
- 账户颜色指示器
- 高亮当前选中账户
- 显示账户名称和邮箱地址

**使用场景**:
- 撰写邮件页面
- 回复/转发邮件时选择发件人

### 3. AttachmentOptionsBottomSheet - 附件操作菜单

**用途**: 提供附件操作选项

**操作项**:
- 预览/打开
- 下载
- 分享
- 保存到相册（仅图片）

**使用场景**:
- 邮件详情页面点击附件
- 附件列表项操作

## 核心特性

### 1. 毛玻璃效果（Glassmorphism）

**规格**:
- 模糊半径: 12dp
- 不透明度: 90%
- 实现方式: `bottomSheetGlassmorphism()` 修饰符

```kotlin
ModalBottomSheet(
    modifier = modifier.bottomSheetGlassmorphism(),
    containerColor = Color.Transparent,
    // ...
)
```

**效果**:
- 自动适配浅色/深色模式
- Android 12+ 使用原生模糊
- 旧版本使用半透明背景模拟

### 2. 滑入动画

**规格**:
- 动画时长: 300ms
- 缓动曲线: DecelerateEasing
- 动画效果: 从底部滑入

**实现**:
Material 3 的 `ModalBottomSheet` 组件自动提供滑入动画，无需额外配置。

### 3. 手势拖拽关闭

**功能**:
- 支持向下拖拽关闭
- 自定义拖拽手柄
- 流畅的交互体验

**实现**:
```kotlin
ModalBottomSheet(
    sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    ),
    dragHandle = {
        // 自定义拖拽手柄
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            ) {}
        }
    }
)
```

### 4. 自适应内容

**特性**:
- 根据内容自动调整高度
- 支持滚动（内容过多时）
- `skipPartiallyExpanded = false` 允许部分展开

### 5. 操作反馈

**交互**:
- 点击项目后自动关闭
- 涟漪效果
- 清晰的视觉反馈

## 使用方法

### EmailActionsBottomSheet

```kotlin
var showBottomSheet by remember { mutableStateOf(false) }
var isStarred by remember { mutableStateOf(false) }

// 触发显示
Button(onClick = { showBottomSheet = true }) {
    Text("邮件操作")
}

// Bottom Sheet
if (showBottomSheet) {
    EmailActionsBottomSheet(
        onDismiss = { showBottomSheet = false },
        onReply = { /* 回复逻辑 */ },
        onForward = { /* 转发逻辑 */ },
        onArchive = { /* 归档逻辑 */ },
        onToggleStar = { 
            isStarred = !isStarred
        },
        onDelete = { /* 删除逻辑 */ },
        isStarred = isStarred
    )
}
```

### AccountSelectorBottomSheet

```kotlin
var showBottomSheet by remember { mutableStateOf(false) }
var selectedAccountId by remember { mutableStateOf<String?>(null) }

// 触发显示
Button(onClick = { showBottomSheet = true }) {
    Text("选择账户")
}

// Bottom Sheet
if (showBottomSheet) {
    AccountSelectorBottomSheet(
        accounts = accounts,
        selectedAccountId = selectedAccountId,
        onDismiss = { showBottomSheet = false },
        onAccountSelected = { accountId ->
            selectedAccountId = accountId
        }
    )
}
```

### AttachmentOptionsBottomSheet

```kotlin
var showBottomSheet by remember { mutableStateOf(false) }

// 触发显示
Button(onClick = { showBottomSheet = true }) {
    Text("附件操作")
}

// Bottom Sheet
if (showBottomSheet) {
    AttachmentOptionsBottomSheet(
        attachmentName = "document.pdf",
        attachmentSize = "2.5 MB",
        isImage = false,
        onDismiss = { showBottomSheet = false },
        onOpen = { /* 打开逻辑 */ },
        onDownload = { /* 下载逻辑 */ },
        onShare = { /* 分享逻辑 */ }
    )
}
```

### 图片附件特殊处理

```kotlin
AttachmentOptionsBottomSheet(
    attachmentName = "photo.jpg",
    attachmentSize = "1.2 MB",
    isImage = true,  // 标记为图片
    onDismiss = { showBottomSheet = false },
    onOpen = { /* 预览图片 */ },
    onDownload = { /* 下载图片 */ },
    onShare = { /* 分享图片 */ },
    onSaveToGallery = { /* 保存到相册 */ }  // 图片专属选项
)
```

## 设计规范

### 尺寸
- **拖拽手柄**: 32dp × 4dp
- **图标大小**: 24dp
- **内边距**: 16dp (水平), 16dp (垂直)
- **项目间距**: 4dp

### 颜色
- **背景**: 根据主题自动适配（毛玻璃效果）
- **文字**: onSurface (主要), onSurfaceVariant (次要)
- **图标**: onSurface (默认), error (删除操作)
- **选中状态**: secondaryContainer 50% opacity

### 动画
- **滑入时长**: 300ms
- **缓动曲线**: DecelerateEasing
- **拖拽响应**: 实时跟随手势

### 间距
- **标题边距**: 24dp (水平), 8dp (垂直)
- **项目边距**: 8dp (水平), 4dp (垂直)
- **项目内边距**: 16dp (水平), 16dp (垂直)
- **底部安全区**: 16dp

## 可访问性

- ✅ 所有图标都有 `contentDescription`
- ✅ 触摸目标最小 48dp × 48dp
- ✅ 颜色对比度符合 WCAG 2.1 AA 标准
- ✅ 支持屏幕阅读器
- ✅ 清晰的视觉层次
- ✅ 语义化的组件结构

## 性能优化

- 使用 `ModalBottomSheet` 的内置优化
- 仅在显示时渲染内容
- 使用 `remember` 缓存状态
- 避免不必要的重组
- 高效的列表渲染

## 最佳实践

### 1. 状态管理

```kotlin
// 推荐：使用 remember 管理显示状态
var showBottomSheet by remember { mutableStateOf(false) }

// 不推荐：使用外部状态可能导致重组问题
```

### 2. 关闭时机

```kotlin
// 推荐：操作完成后自动关闭
onClick = {
    performAction()
    onDismiss()
}

// 不推荐：需要用户手动关闭
onClick = {
    performAction()
    // 忘记调用 onDismiss()
}
```

### 3. 错误处理

```kotlin
// 推荐：在回调中处理错误
onDownload = {
    try {
        downloadAttachment()
        onDismiss()
    } catch (e: Exception) {
        showError(e.message)
        // 不关闭 Bottom Sheet，让用户重试
    }
}
```

### 4. 加载状态

```kotlin
// 推荐：显示加载指示器
var isLoading by remember { mutableStateOf(false) }

onClick = {
    isLoading = true
    performAsyncAction {
        isLoading = false
        onDismiss()
    }
}
```

## 常见问题

### Q: 如何自定义 Bottom Sheet 的高度？

A: `ModalBottomSheet` 会根据内容自动调整高度。如果需要固定高度，可以在内容 Column 上设置 `height` 修饰符。

### Q: 如何禁用拖拽关闭？

A: 设置 `sheetState` 的 `skipPartiallyExpanded = true` 并移除 `dragHandle`。

### Q: 如何添加背景遮罩？

A: `ModalBottomSheet` 自动提供背景遮罩，无需额外配置。

### Q: 如何处理键盘遮挡？

A: 使用 `imePadding()` 修饰符自动处理键盘遮挡。

## 未来改进

1. **动画增强**: 添加项目进入的 stagger 动画
2. **手势优化**: 支持更多手势操作
3. **主题定制**: 提供更多主题选项
4. **无障碍增强**: 改进屏幕阅读器支持

## 相关文件

- `EmailActionsBottomSheet.kt`: 邮件操作菜单实现
- `AccountSelectorBottomSheet.kt`: 账户选择器实现
- `AttachmentOptionsBottomSheet.kt`: 附件操作菜单实现
- `BottomSheetUsageExample.kt`: 使用示例
- `Glassmorphism.kt`: 毛玻璃效果实现

## 需求映射

本实现满足以下需求：

- **需求 3.5**: Modal Bottom Sheet 用于邮件操作、账户选择和附件操作
- 毛玻璃效果：12dp blur + 90% opacity
- 滑入动画：300ms
- 手势拖拽关闭

## 测试建议

1. **视觉测试**: 验证毛玻璃效果在不同主题下的表现
2. **动画测试**: 验证滑入/滑出动画的流畅性
3. **手势测试**: 验证拖拽关闭功能
4. **交互测试**: 验证所有操作项的点击响应
5. **可访问性测试**: 使用 TalkBack 验证屏幕阅读器支持
