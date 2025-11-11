# Design Document

## Overview

本设计旨在简化应用的页面过渡动画，将当前的"淡入+水平滑动"组合动画改为纯淡入淡出效果。这将提供更直接、更符合用户预期的页面切换体验。

## Architecture

### Current Implementation

当前在 `Animation.kt` 中定义的页面过渡动画：
- `pageEnterAnimation()`: fadeIn + slideInHorizontally (从右侧 1/3 屏幕宽度滑入)
- `pageExitAnimation()`: fadeOut + slideOutHorizontally (向左侧 1/3 屏幕宽度滑出)

这些动画在 `NavGraph.kt` 中通过 `NavHost` 的 `enterTransition` 和 `exitTransition` 参数应用到所有页面导航。

### Proposed Changes

修改 `Animation.kt` 中的页面过渡动画函数，移除水平滑动效果，仅保留淡入淡出：
- `pageEnterAnimation()`: 仅使用 fadeIn
- `pageExitAnimation()`: 仅使用 fadeOut

保持现有的动画参数：
- 时长: 300ms (STANDARD_DURATION)
- 缓动曲线: FastOutSlowIn

## Components and Interfaces

### Modified Components

#### Animation.kt

**函数: pageEnterAnimation()**
```kotlin
fun pageEnterAnimation() = fadeIn(
    animationSpec = tween(
        durationMillis = FleurAnimation.STANDARD_DURATION,
        easing = FleurAnimation.FastOutSlowIn
    )
)
```

**函数: pageExitAnimation()**
```kotlin
fun pageExitAnimation() = fadeOut(
    animationSpec = tween(
        durationMillis = FleurAnimation.STANDARD_DURATION,
        easing = FleurAnimation.FastOutSlowIn
    )
)
```

### Unchanged Components

- `NavGraph.kt`: 无需修改，继续使用 `pageEnterAnimation()` 和 `pageExitAnimation()`
- 其他动画函数（列表项动画、视图切换动画等）保持不变

## Data Models

无需修改数据模型。

## Error Handling

本修改不涉及错误处理逻辑，因为：
- 动画是声明式的，由 Compose 框架管理
- 不存在可能失败的操作
- 如果动画定义有问题，会在编译时被发现

## Testing Strategy

### Manual Testing

1. **页面导航测试**
   - 从收件箱导航到各个文件夹页面（已发送、草稿、星标等）
   - 验证页面直接淡入，无水平滑动
   - 验证过渡流畅，时长合适

2. **返回导航测试**
   - 从各个页面返回到收件箱
   - 验证页面直接淡出，无水平滑动
   - 验证返回动画与进入动画一致

3. **深层导航测试**
   - 测试多层级导航（如：收件箱 → 邮件详情 → 撰写回复）
   - 验证每次切换都使用淡入淡出效果

4. **其他动画验证**
   - 确认列表项动画（向上滑入）未受影响
   - 确认抽屉动画未受影响
   - 确认其他 UI 元素动画正常工作

### Visual Comparison

对比修改前后的页面切换效果：
- 修改前：页面从右侧滑入，同时淡入
- 修改后：页面直接淡入，无位置变化

## Implementation Notes

### Simplicity

这是一个非常简单的修改：
- 只需修改两个函数
- 移除 `slideInHorizontally` 和 `slideOutHorizontally` 调用
- 保留 `fadeIn` 和 `fadeOut`

### Performance

淡入淡出动画比组合动画性能更好：
- 减少了位置计算
- 降低了重组开销
- 提升了低端设备上的流畅度

### Consistency

修改后的动画与现有的视图切换动画（`viewSwitchEnterAnimation` 和 `viewSwitchExitAnimation`）保持一致，都使用纯淡入淡出效果。

## Alternative Approaches Considered

### 1. 保留滑动但减少距离

将滑动距离从 `it / 3` 减少到 `it / 10`，使滑动效果更微妙。

**不采用原因**: 用户明确表示希望"直接出现"，任何滑动效果都不符合需求。

### 2. 使用缩放动画

使用 `scaleIn` 和 `scaleOut` 替代滑动。

**不采用原因**: 缩放动画可能比滑动更引人注意，不符合"直接出现"的需求。

### 3. 完全移除动画

将动画时长设为 0 或使用 `EnterTransition.None`。

**不采用原因**: 完全没有过渡会显得生硬，淡入淡出提供了适度的视觉连续性。
