# 需求文档

## 简介

优化邮件列表卡片的视觉设计和交互体验，解决已读/未读状态区分不明显、收藏标志布局问题以及状态同步问题。

## 术语表

- **EmailListItem**: 邮件列表项组件，显示单个邮件的卡片
- **Fleur系统**: 邮件客户端应用系统
- **已读状态**: 用户已打开查看过的邮件
- **未读状态**: 用户尚未打开查看的邮件
- **标星状态**: 用户标记为重要的邮件
- **卡片容器**: 包含邮件信息的Card组件

## 需求

### 需求 1: 已读/未读视觉区分

**用户故事:** 作为邮件用户，我希望能够一眼区分已读和未读邮件，以便快速识别需要处理的邮件

#### 验收标准

1. WHEN 邮件处于未读状态时，THE EmailListItem SHALL 使用加粗字体显示发件人名称
2. WHEN 邮件处于未读状态时，THE EmailListItem SHALL 使用加粗字体显示邮件主题
3. WHEN 邮件处于已读状态时，THE EmailListItem SHALL 使用常规字体显示发件人名称和主题
4. WHEN 邮件处于未读状态时，THE EmailListItem SHALL 在卡片左侧显示蓝色指示条
5. WHEN 邮件处于未读状态时，THE EmailListItem SHALL 使用更高的背景不透明度以增强视觉对比

### 需求 2: 收藏标志布局优化

**用户故事:** 作为邮件用户，我希望收藏标志不会影响时间显示，以便同时清晰看到邮件时间和收藏状态

#### 验收标准

1. WHEN 邮件被标星时，THE EmailListItem SHALL 在时间戳下方独立显示星标图标
2. THE EmailListItem SHALL 确保星标图标与时间戳垂直对齐
3. THE EmailListItem SHALL 保持卡片固定高度为88dp
4. THE EmailListItem SHALL 确保星标图标不会导致时间戳文本换行或被遮挡
5. WHEN 邮件未被标星时，THE EmailListItem SHALL 不显示星标图标且不保留占位空间

### 需求 3: 标星状态实时同步

**用户故事:** 作为邮件用户，我希望在邮件详情页标星后，返回列表时能立即看到更新的标星状态

#### 验收标准

1. WHEN 用户在邮件详情页标星邮件时，THE Fleur系统 SHALL 更新数据库中的邮件标星状态
2. WHEN 用户返回邮件列表时，THE EmailListItem SHALL 显示最新的标星状态
3. THE Fleur系统 SHALL 在标星状态变更后200毫秒内更新UI显示
4. WHEN 标星状态变更时，THE EmailListItem SHALL 使用平滑动画过渡显示星标图标
5. THE Fleur系统 SHALL 确保标星状态在所有邮件列表视图（收件箱、已发送、文件夹等）中保持一致

### 需求 4: 卡片尺寸和动画稳定性

**用户故事:** 作为邮件用户，我希望卡片在状态变化时保持稳定，不会出现跳动或尺寸变化

#### 验收标准

1. THE EmailListItem SHALL 保持固定高度88dp，不受内容变化影响
2. WHEN 邮件状态从未读变为已读时，THE EmailListItem SHALL 保持卡片尺寸不变
3. WHEN 星标状态变更时，THE EmailListItem SHALL 保持卡片尺寸不变
4. THE EmailListItem SHALL 使用淡入淡出动画显示或隐藏星标图标，持续时间不超过200毫秒
5. THE EmailListItem SHALL 确保所有动画不会导致列表滚动位置偏移

### 需求 5: 性能和可访问性

**用户故事:** 作为邮件用户，我希望邮件列表滚动流畅，且支持无障碍功能

#### 验收标准

1. THE EmailListItem SHALL 在快速滚动时保持60fps的渲染性能
2. THE EmailListItem SHALL 为星标图标提供内容描述以支持屏幕阅读器
3. THE EmailListItem SHALL 为已读/未读状态提供语义化标记
4. THE EmailListItem SHALL 确保未读指示条的颜色对比度符合WCAG AA标准
5. THE EmailListItem SHALL 在低端设备上保持流畅的动画效果
