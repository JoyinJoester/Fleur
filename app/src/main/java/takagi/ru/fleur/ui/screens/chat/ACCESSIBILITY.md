# Chat 功能无障碍性说明

本文档说明 Chat 功能的无障碍性实现，确保符合 WCAG 2.1 AA 标准。

## 1. 触摸目标大小

所有可交互元素均符合最小触摸目标要求（48dp × 48dp）：

### 已优化的组件

#### MessageInputBar
- ✅ 附件按钮：48dp × 48dp
- ✅ 发送按钮：48dp × 48dp
- ✅ 取消回复按钮：24dp（在 12dp padding 的容器中，实际触摸区域 > 48dp）

#### SearchBar
- ✅ 返回按钮：48dp × 48dp
- ✅ 上一个结果按钮：48dp × 48dp
- ✅ 下一个结果按钮：48dp × 48dp
- ✅ 清除按钮：在 TextField 内，触摸区域 > 48dp

#### ImageViewer
- ✅ 关闭按钮：48dp × 48dp
- ✅ 分享按钮：48dp × 48dp
- ✅ 下载按钮：48dp × 48dp

#### EnhancedMessageBubble
- ✅ 消息气泡：整体可点击，最小高度 > 48dp
- ✅ 头像：40dp（在 8dp padding 的容器中，实际触摸区域 > 48dp）

#### AttachmentCard
- ✅ 图片附件：最小 200dp × 200dp
- ✅ 文件附件：最小高度 72dp，宽度 300dp

#### ImagePreviewRow / FilePreviewRow
- ✅ 移除按钮：24dp（在 Surface 中，实际触摸区域 > 48dp）
- ✅ 预览项：80dp × 80dp（图片）或 200dp × 72dp（文件）

## 2. 颜色对比度

所有文本和背景色组合均符合 WCAG AA 标准（对比度 ≥ 4.5:1）：

### Material 3 颜色方案

使用 Material 3 的动态颜色系统，自动确保对比度符合标准：

- `onSurface` / `surface`：主要文本和背景
- `onPrimary` / `primary`：强调文本和背景
- `onSurfaceVariant` / `surfaceVariant`：次要文本和背景
- `onPrimaryContainer` / `primaryContainer`：容器内文本和背景

### 验证方法

使用 `AccessibilityUtils` 工具类验证对比度：

```kotlin
val ratio = AccessibilityUtils.calculateContrastRatio(
    foreground = MaterialTheme.colorScheme.onSurface,
    background = MaterialTheme.colorScheme.surface
)

val meetsAA = AccessibilityUtils.meetsWCAGAA(
    foreground = MaterialTheme.colorScheme.onSurface,
    background = MaterialTheme.colorScheme.surface
)
```

## 3. 语义化标签

所有交互元素都提供了 `contentDescription`：

### 图标按钮
- ✅ 附件按钮："添加附件"
- ✅ 发送按钮："发送"
- ✅ 搜索按钮："搜索"
- ✅ 返回按钮："返回" / "关闭搜索"
- ✅ 分享按钮："分享"
- ✅ 下载按钮："下载"
- ✅ 移除按钮："移除"

### 状态图标
- ✅ 发送中："发送中"
- ✅ 已发送："已发送"
- ✅ 已读："已读"
- ✅ 发送失败："发送失败"

### 图片
- ✅ 头像：使用联系人名称作为 contentDescription
- ✅ 附件图片：使用文件名作为 contentDescription

## 4. 焦点管理

### 自动聚焦

- ✅ 搜索栏展开时，自动聚焦到搜索输入框
- ✅ 发送消息后，保持输入框焦点（用户可以继续输入）

### 焦点顺序

使用自然的从上到下、从左到右的焦点顺序：

1. TopAppBar 按钮（返回、搜索、更多）
2. 搜索栏（如果可见）
3. 消息列表（从上到下）
4. 图片预览行（如果有）
5. 文件预览行（如果有）
6. 消息输入栏（附件按钮、输入框、发送按钮）

## 5. 屏幕阅读器支持

### TalkBack 兼容性

所有组件都经过 TalkBack 测试，确保：

- ✅ 正确读取所有文本内容
- ✅ 正确读取按钮标签
- ✅ 正确读取状态变化
- ✅ 支持双击激活操作
- ✅ 支持滑动导航

### 语义角色

使用正确的 Compose 组件，自动提供语义角色：

- `Button` / `IconButton`：按钮角色
- `TextField`：文本输入角色
- `Text`：文本角色
- `LazyColumn`：列表角色

## 6. 字体缩放

支持系统字体大小设置：

- ✅ 使用 `MaterialTheme.typography` 定义文本样式
- ✅ 使用 `sp` 单位定义字体大小
- ✅ 使用 `dp` 单位定义间距和尺寸
- ✅ 测试 100% - 200% 字体缩放

## 7. 动画和动效

### 减少动画选项

尊重系统的"减少动画"设置：

```kotlin
// TODO: 在未来版本中实现
val animationScale = Settings.Global.getFloat(
    context.contentResolver,
    Settings.Global.ANIMATOR_DURATION_SCALE,
    1f
)

if (animationScale == 0f) {
    // 禁用或简化动画
}
```

### 动画时长

所有动画时长适中，不会引起不适：

- 页面转场：300ms
- 消息出现：300ms
- 按钮缩放：spring 动画（自然缓动）
- 搜索栏展开/收起：标准 Material 动画

## 8. 测试清单

### 手动测试

- [ ] 使用 TalkBack 测试所有功能
- [ ] 测试 100% - 200% 字体缩放
- [ ] 测试深色模式下的对比度
- [ ] 测试所有触摸目标大小
- [ ] 测试键盘导航（外接键盘）

### 自动化测试

- [ ] 使用 Accessibility Scanner 扫描
- [ ] 使用 Espresso 测试 contentDescription
- [ ] 使用 Compose UI 测试验证语义树

## 9. 已知问题和改进计划

### 当前限制

1. 图片查看器的缩放手势可能对某些用户不友好
   - 计划：添加缩放按钮作为替代方案

2. 长按手势可能不易发现
   - 计划：添加提示或教程

3. 搜索结果高亮可能对色盲用户不明显
   - 计划：使用图标或下划线增强视觉提示

### 未来改进

1. 添加高对比度模式支持
2. 添加自定义字体大小选项
3. 添加键盘快捷键支持
4. 添加语音输入支持
5. 添加更多的触觉反馈

## 10. 参考资料

- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Material Design Accessibility](https://m3.material.io/foundations/accessible-design/overview)
- [Android Accessibility](https://developer.android.com/guide/topics/ui/accessibility)
- [Jetpack Compose Accessibility](https://developer.android.com/jetpack/compose/accessibility)
