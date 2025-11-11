# Design Document

## Overview

本设计文档描述了如何修复收件箱页面邮件卡片的手势操作问题。主要问题是 `Card` 组件的 `onClick` 与 `SwipeToDismissBox` 的手势处理存在冲突，导致点击和滑动手势无法正常工作。

解决方案是使用 Compose 的 `Modifier.combinedClickable` 和 `Modifier.pointerInput` 来手动处理手势，避免使用 `Card` 的内置 `onClick`，并确保手势事件正确传递。

## Architecture

### 组件层次结构

```
EmailListView
  └── LazyColumn
      └── EmailListItem (直接使用) 或 SwipeableEmailItem (包装)
          └── EmailListItem (内部)
              └── Card (不使用onClick参数)
                  └── 邮件内容布局
```

### 手势处理流程

1. **点击手势**: 使用 `Modifier.combinedClickable` 处理单击和长按
2. **滑动手势**: 使用 `SwipeToDismissBox` 处理水平滑动
3. **滚动手势**: 使用 `LazyColumn` 的内置滚动处理垂直滚动

### 手势优先级

1. 垂直滚动（最高优先级）- LazyColumn 处理
2. 水平滑动 - SwipeToDismissBox 处理
3. 长按 - combinedClickable 处理
4. 点击 - combinedClickable 处理

## Components and Interfaces

### EmailListItem 修改

**当前问题:**
- 使用 `Card(onClick = ...)` 导致手势冲突
- 无法正确处理长按手势
- 与 SwipeToDismissBox 的手势处理冲突

**解决方案:**
```kotlin
@Composable
fun EmailListItem(
    email: Email,
    isSelected: Boolean = false,
    animationIndex: Int = 0,
    isScrolling: Boolean = false,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit = {},
    onStar: () -> Unit = {},
    onArchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Card(
        // 移除 onClick 参数
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {
                    Log.d("EmailListItem", "点击邮件: ${email.id}")
                    onItemClick()
                },
                onLongClick = {
                    Log.d("EmailListItem", "长按邮件: ${email.id}")
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onItemLongClick()
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        // 邮件内容布局保持不变
    }
}
```

**关键变更:**
1. 移除 `Card` 的 `onClick` 参数
2. 在 `modifier` 中添加 `combinedClickable` 来处理点击和长按
3. 添加触觉反馈支持
4. 添加日志以便调试

### SwipeableEmailItem 修改

**当前问题:**
- SwipeToDismissBox 可能阻止点击事件传递
- 手势冲突导致滑动不流畅

**解决方案:**
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableEmailItem(
    email: Email,
    isSelected: Boolean = false,
    isMultiSelectMode: Boolean = false,
    isScrolling: Boolean = false,
    leftSwipeAction: SwipeAction? = null,
    rightSwipeAction: SwipeAction? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onSwipeAction: (EmailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // 如果没有配置滑动操作，直接显示邮件项
    if (leftSwipeAction == null && rightSwipeAction == null) {
        EmailListItem(
            email = email,
            isSelected = isSelected,
            isScrolling = isScrolling,
            onItemClick = onClick,
            onItemLongClick = onLongClick,
            modifier = modifier
        )
        return
    }
    
    val haptic = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            // 滑动操作处理逻辑保持不变
        },
        positionalThreshold = { distance -> distance * 0.3f }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = rightSwipeAction != null,
        enableDismissFromEndToStart = leftSwipeAction != null,
        backgroundContent = {
            SwipeBackground(
                dismissState = dismissState,
                leftSwipeAction = leftSwipeAction,
                rightSwipeAction = rightSwipeAction
            )
        },
        content = {
            // 确保 EmailListItem 能够接收点击和长按事件
            EmailListItem(
                email = email,
                isSelected = isSelected,
                isScrolling = isScrolling,
                onItemClick = onClick,
                onItemLongClick = onLongClick
            )
        }
    )
}
```

**关键变更:**
1. 确保 EmailListItem 的手势处理器正确工作
2. SwipeToDismissBox 只处理水平滑动
3. 点击和长按事件由 EmailListItem 内部的 combinedClickable 处理

### EmailListView 修改

**当前状态:**
- 已正确使用 LazyColumn 处理垂直滚动
- 已正确传递回调函数

**无需修改** - EmailListView 的实现已经正确，只需要确保传递的回调函数能够正确触发。

## Data Models

无需修改数据模型。

## Error Handling

### 手势冲突检测

添加日志以便调试手势冲突：

```kotlin
// 在 EmailListItem 中
Log.d("EmailListItem", "点击邮件: ${email.id}")
Log.d("EmailListItem", "长按邮件: ${email.id}")

// 在 EmailListView 中
Log.d("EmailListView", "点击邮件: ${email.id}")

// 在 InboxScreen 中
Log.d("InboxScreen", "onEmailClick 触发: $emailId, 多选模式: ${uiState.isMultiSelectMode}")
```

### 手势失败处理

如果手势仍然无法工作：
1. 检查 Modifier 的顺序（combinedClickable 应该在最后）
2. 检查是否有其他组件拦截了手势事件
3. 使用 `Modifier.pointerInput` 手动处理触摸事件

## Testing Strategy

### 手动测试

1. **点击测试**
   - 点击邮件卡片，验证是否导航到详情页面
   - 在多选模式下点击，验证是否切换选中状态

2. **长按测试**
   - 长按邮件卡片，验证是否进入多选模式
   - 验证是否有触觉反馈

3. **滑动测试**
   - 向右滑动邮件卡片，验证是否触发归档操作
   - 向左滑动邮件卡片，验证是否触发删除操作
   - 验证滑动过程中的背景色和图标渐变效果

4. **滚动测试**
   - 垂直滚动列表，验证是否流畅
   - 验证滚动时不会误触发点击或滑动

5. **手势冲突测试**
   - 快速点击多个邮件，验证是否都能正确响应
   - 在滑动过程中取消，验证UI是否正确恢复
   - 在滚动过程中点击，验证是否正确区分

### 性能测试

1. 使用 Android Studio Profiler 监控手势处理的性能
2. 验证手势操作期间帧率保持在 60fps
3. 验证手势响应延迟小于 100ms

### 调试工具

使用 Logcat 过滤以下标签：
- `EmailListItem`
- `EmailListView`
- `InboxScreen`
- `SwipeableEmailItem`

## Implementation Notes

### Modifier 顺序

Modifier 的顺序很重要，应该按照以下顺序：
1. 布局相关（fillMaxWidth, height, padding）
2. 手势处理（combinedClickable）
3. 其他修饰符

### 触觉反馈

使用 `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.LongPress)` 提供触觉反馈。

### 日志记录

在关键位置添加日志，便于调试手势问题：
- 点击事件触发时
- 长按事件触发时
- 滑动操作触发时
- 导航发生时

## Migration Strategy

1. 修改 EmailListItem，移除 Card 的 onClick，添加 combinedClickable
2. 测试点击和长按功能
3. 测试滑动功能
4. 测试手势冲突场景
5. 如果问题仍然存在，考虑使用 pointerInput 手动处理手势
