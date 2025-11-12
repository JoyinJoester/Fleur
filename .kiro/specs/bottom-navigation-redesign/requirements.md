# Requirements Document

## Introduction

重新设计和实现 Fleur 邮件应用的底部导航栏，解决当前存在的视觉问题（系统导航栏区域显示异常、图标位置不当、背景色不协调等），创建一个符合 Material 3 Extended 设计规范的现代化底部导航组件。

## Glossary

- **Bottom Navigation Bar**: 底部导航栏，应用主要导航组件
- **System Navigation Bar**: Android 系统导航栏区域（屏幕底部的系统UI区域）
- **Navigation Item**: 导航项，底部导航栏中的单个可点击项
- **Material 3 Extended**: Material Design 3 的扩展设计规范
- **WindowInsets**: Android 窗口插入区域，包括系统导航栏等

## Requirements

### Requirement 1

**User Story:** 作为用户，我希望底部导航栏能够完全覆盖系统导航栏区域，不出现任何黑条或其他颜色的背景间隙，以获得统一的视觉体验

#### Acceptance Criteria

1. WHEN THE Bottom Navigation Bar 被渲染时，THE Bottom Navigation Bar SHALL 使用 navigationBarsPadding() 扩展到系统导航栏区域
2. THE Bottom Navigation Bar SHALL 使用统一的背景色填充整个区域，包括系统导航栏区域
3. THE Bottom Navigation Bar SHALL 确保背景色与应用主题协调一致
4. THE Bottom Navigation Bar SHALL 在所有 Android 设备上正确显示，无视觉间隙

### Requirement 2

**User Story:** 作为用户，我希望底部导航栏具有现代化的圆角设计，使界面更加美观和符合 Material 3 设计规范

#### Acceptance Criteria

1. THE Bottom Navigation Bar SHALL 在顶部应用 20-24dp 的圆角
2. THE Bottom Navigation Bar SHALL 确保圆角仅应用于导航内容区域，不影响系统导航栏区域
3. THE Bottom Navigation Bar SHALL 使用 clip 修饰符正确裁剪圆角
4. THE Bottom Navigation Bar SHALL 保持圆角在不同屏幕尺寸下的一致性

### Requirement 3

**User Story:** 作为用户，我希望导航图标和标签位置合理居中，不会偏上或偏下，以获得良好的视觉平衡

#### Acceptance Criteria

1. THE Navigation Item SHALL 在垂直方向上居中对齐图标和标签
2. THE Navigation Item SHALL 使用适当的内边距确保图标不会偏下
3. THE Navigation Item SHALL 在 80dp 高度的导航区域内合理分布空间
4. THE Navigation Item SHALL 保持图标和标签之间 4-6dp 的间距

### Requirement 4

**User Story:** 作为用户，我希望选中和未选中的导航项有清晰的视觉区分，包括流畅的动画过渡

#### Acceptance Criteria

1. WHEN 用户点击 Navigation Item 时，THE Navigation Item SHALL 显示选中状态的视觉反馈
2. THE Navigation Item SHALL 使用不同的图标样式（filled vs outlined）区分选中状态
3. THE Navigation Item SHALL 应用 200-300ms 的流畅动画过渡
4. THE Navigation Item SHALL 使用主题色系统（primary, primaryContainer）表示选中状态
5. THE Navigation Item SHALL 使用缩放动画（1.0x 到 1.1-1.15x）增强选中效果

### Requirement 5

**User Story:** 作为用户，我希望底部导航栏包含四个主要功能入口（Inbox、Chat、Contacts、Calendar），方便快速切换

#### Acceptance Criteria

1. THE Bottom Navigation Bar SHALL 包含四个 Navigation Item
2. THE Bottom Navigation Bar SHALL 为每个 Navigation Item 分配相等的宽度
3. THE Bottom Navigation Bar SHALL 使用语义化的图标表示每个功能
4. THE Bottom Navigation Bar SHALL 在每个图标下方显示对应的文本标签
5. THE Bottom Navigation Bar SHALL 支持点击回调以触发导航

### Requirement 6

**User Story:** 作为用户，我希望底部导航栏使用合适的背景色，既能与应用主题协调，又能与内容区域有适当的视觉分离

#### Acceptance Criteria

1. THE Bottom Navigation Bar SHALL 使用 Material 3 主题色系统中的 surface 相关颜色
2. THE Bottom Navigation Bar SHALL 避免使用纯黑或纯白背景
3. THE Bottom Navigation Bar SHALL 使用 surfaceContainer 或 surfaceContainerLow 作为背景色
4. THE Bottom Navigation Bar SHALL 确保背景色在深色和浅色主题下都能正确显示
