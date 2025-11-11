# Requirements Document

## Introduction

收件箱页面的邮件卡片无法触发手势操作（点击、长按、滑动）。这个问题影响了用户与邮件列表的交互体验。需要修复手势冲突问题，确保所有手势操作能够正常工作。

## Glossary

- **EmailListItem**: 邮件列表项组件，显示单个邮件的卡片UI
- **SwipeableEmailItem**: 可滑动的邮件项组件，包装EmailListItem并添加滑动手势支持
- **EmailListView**: 邮件列表视图，使用LazyColumn显示多个邮件项
- **Gesture Conflict**: 手势冲突，当多个组件同时处理相同的手势输入时发生
- **Card Component**: Material 3的卡片组件，支持点击交互
- **SwipeToDismissBox**: Material 3的滑动消除组件，用于实现滑动操作

## Requirements

### Requirement 1

**User Story:** 作为用户，我希望能够点击邮件卡片查看邮件详情，以便阅读完整的邮件内容

#### Acceptance Criteria

1. WHEN 用户点击邮件卡片, THE EmailListItem SHALL 触发onItemClick回调并导航到邮件详情页面
2. THE EmailListItem SHALL 在点击时提供视觉反馈（按压效果）
3. THE EmailListItem SHALL 在多选模式下点击时切换选中状态而不是导航

### Requirement 2

**User Story:** 作为用户，我希望能够长按邮件卡片进入多选模式，以便批量操作多封邮件

#### Acceptance Criteria

1. WHEN 用户长按邮件卡片, THE EmailListItem SHALL 触发onItemLongClick回调并进入多选模式
2. THE EmailListItem SHALL 在长按时提供触觉反馈
3. THE EmailListItem SHALL 在长按后显示选中状态

### Requirement 3

**User Story:** 作为用户，我希望能够左右滑动邮件卡片执行快捷操作，以便快速归档或删除邮件

#### Acceptance Criteria

1. WHEN 用户向右滑动邮件卡片超过30%宽度, THE SwipeableEmailItem SHALL 触发右滑操作（归档）
2. WHEN 用户向左滑动邮件卡片超过30%宽度, THE SwipeableEmailItem SHALL 触发左滑操作（删除）
3. THE SwipeableEmailItem SHALL 在滑动过程中显示背景色和图标渐变效果
4. THE SwipeableEmailItem SHALL 在达到触发阈值时提供触觉反馈

### Requirement 4

**User Story:** 作为开发者，我需要解决手势冲突问题，以便所有手势操作能够正常工作

#### Acceptance Criteria

1. THE EmailListItem SHALL 正确处理点击和长按手势而不与滑动手势冲突
2. THE SwipeableEmailItem SHALL 正确区分水平滑动和垂直滚动手势
3. THE EmailListView SHALL 允许垂直滚动而不干扰邮件项的手势操作
4. THE System SHALL 确保手势事件正确传递到目标组件

### Requirement 5

**User Story:** 作为用户，我希望手势操作响应迅速且流畅，以便获得良好的交互体验

#### Acceptance Criteria

1. THE EmailListItem SHALL 在用户触摸后立即响应（延迟小于100ms）
2. THE SwipeableEmailItem SHALL 使用流畅的动画效果（400ms，DecelerateEasing）
3. THE System SHALL 在手势操作期间保持60fps的帧率
4. THE System SHALL 在手势操作完成后正确恢复UI状态
