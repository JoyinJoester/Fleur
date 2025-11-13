# Requirements Document

## Introduction

为 Fleur 邮件应用设计和实现一个完善的联系人页面,作为底部导航栏的第三个按钮。该页面需要提供优秀的用户体验,支持快速查看联系人列表、搜索联系人、直接发起聊天对话或发送邮件。设计必须完全符合 Material 3 Extended (M3E) 设计规范,注重流畅的交互和精致的视觉效果。

## Glossary

- **Contacts Page**: 联系人页面,显示用户的所有联系人
- **Contact Item**: 联系人列表项,显示单个联系人的信息
- **Quick Action**: 快速操作,允许用户直接对联系人执行操作(聊天、邮件)
- **Contact Avatar**: 联系人头像,可以是图片或首字母缩写
- **Material 3 Extended (M3E)**: Material Design 3 的扩展设计规范
- **FAB**: Floating Action Button,浮动操作按钮
- **Bottom Sheet**: 底部弹出面板
- **Search Bar**: 搜索栏组件

## Requirements

### Requirement 1

**User Story:** 作为用户,我希望能够看到一个清晰的联系人列表,显示每个联系人的头像、姓名和邮箱地址,以便快速识别和选择联系人

#### Acceptance Criteria

1. THE Contacts Page SHALL 显示所有联系人的列表
2. THE Contact Item SHALL 显示联系人头像(如果有照片则显示照片,否则显示首字母缩写)
3. THE Contact Item SHALL 显示联系人的姓名作为主要文本
4. THE Contact Item SHALL 显示联系人的邮箱地址作为次要文本
5. THE Contact Item SHALL 使用 Material 3 的 ListItem 组件样式
6. THE Contact Item SHALL 在头像区域使用 56dp 的圆形容器
7. THE Contact Item SHALL 使用适当的内边距(16dp 水平,12dp 垂直)确保视觉舒适度

### Requirement 2

**User Story:** 作为用户,我希望能够快速搜索联系人,通过输入姓名或邮箱地址来过滤列表,以便在大量联系人中快速找到目标

#### Acceptance Criteria

1. THE Contacts Page SHALL 在顶部显示一个搜索栏
2. WHEN 用户输入搜索文本时,THE Contacts Page SHALL 实时过滤联系人列表
3. THE Search Bar SHALL 支持按姓名搜索联系人
4. THE Search Bar SHALL 支持按邮箱地址搜索联系人
5. THE Search Bar SHALL 使用 Material 3 的 SearchBar 组件样式
6. THE Search Bar SHALL 显示搜索图标和清除按钮
7. WHEN 搜索结果为空时,THE Contacts Page SHALL 显示友好的空状态提示

### Requirement 3

**User Story:** 作为用户,我希望能够直接从联系人列表项快速发起聊天对话,无需进入详情页面,以提高操作效率

#### Acceptance Criteria

1. THE Contact Item SHALL 在右侧显示一个聊天快速操作按钮
2. WHEN 用户点击聊天按钮时,THE Contacts Page SHALL 导航到与该联系人的聊天详情页面
3. THE Quick Action SHALL 使用 Material 3 的 IconButton 样式
4. THE Quick Action SHALL 使用聊天图标(Icons.Outlined.Chat)
5. THE Quick Action SHALL 在点击时显示涟漪效果
6. THE Quick Action SHALL 使用 48dp 的触摸目标尺寸以确保可访问性

### Requirement 4

**User Story:** 作为用户,我希望能够直接从联系人列表项快速发送邮件,无需进入详情页面,以提高操作效率

#### Acceptance Criteria

1. THE Contact Item SHALL 在右侧显示一个邮件快速操作按钮
2. WHEN 用户点击邮件按钮时,THE Contacts Page SHALL 导航到撰写邮件页面并预填充收件人
3. THE Quick Action SHALL 使用 Material 3 的 IconButton 样式
4. THE Quick Action SHALL 使用邮件图标(Icons.Outlined.Email)
5. THE Quick Action SHALL 在点击时显示涟漪效果
6. THE Quick Action SHALL 使用 48dp 的触摸目标尺寸以确保可访问性

### Requirement 5

**User Story:** 作为用户,我希望能够点击联系人列表项查看更多详细信息和操作选项,以便进行更复杂的操作

#### Acceptance Criteria

1. WHEN 用户点击 Contact Item 的主体区域时,THE Contacts Page SHALL 显示联系人详情底部面板
2. THE Bottom Sheet SHALL 显示联系人的完整信息(姓名、邮箱、电话等)
3. THE Bottom Sheet SHALL 提供多个操作选项(发起聊天、发送邮件、编辑联系人、删除联系人)
4. THE Bottom Sheet SHALL 使用 Material 3 的 ModalBottomSheet 组件
5. THE Bottom Sheet SHALL 支持拖拽关闭手势
6. THE Bottom Sheet SHALL 使用 28dp 的顶部圆角

### Requirement 6

**User Story:** 作为用户,我希望能够添加新的联系人,通过一个明显的浮动操作按钮,以便扩展我的联系人列表

#### Acceptance Criteria

1. THE Contacts Page SHALL 在右下角显示一个浮动操作按钮(FAB)
2. WHEN 用户点击 FAB 时,THE Contacts Page SHALL 显示添加联系人的表单
3. THE FAB SHALL 使用 Material 3 的 FloatingActionButton 组件
4. THE FAB SHALL 使用添加图标(Icons.Filled.Add)
5. THE FAB SHALL 使用主题色(primary)作为背景色
6. THE FAB SHALL 在滚动时自动隐藏和显示以避免遮挡内容

### Requirement 7

**User Story:** 作为用户,我希望联系人列表按字母顺序分组显示,并带有字母索引,以便快速定位到特定字母开头的联系人

#### Acceptance Criteria

1. THE Contacts Page SHALL 按联系人姓名的首字母对联系人进行分组
2. THE Contacts Page SHALL 在每个分组的顶部显示字母标题
3. THE Contacts Page SHALL 在右侧显示字母索引滚动条
4. WHEN 用户点击字母索引时,THE Contacts Page SHALL 快速滚动到对应的分组
5. THE Contacts Page SHALL 使用粘性标题(sticky header)保持当前分组标题可见
6. THE Contacts Page SHALL 为字母标题使用 Material 3 的 surface variant 背景色

### Requirement 8

**User Story:** 作为用户,我希望看到联系人的在线状态指示器,以便知道哪些联系人当前在线可以立即聊天

#### Acceptance Criteria

1. THE Contact Item SHALL 在头像右下角显示在线状态指示器
2. THE Contact Item SHALL 使用绿色圆点表示在线状态
3. THE Contact Item SHALL 使用灰色圆点表示离线状态
4. THE Contact Item SHALL 使用 12dp 的圆形指示器
5. THE Contact Item SHALL 为指示器添加 2dp 的白色边框以确保可见性

### Requirement 9

**User Story:** 作为用户,我希望联系人页面具有流畅的动画效果,包括列表项的进入动画和操作反馈,以获得愉悦的使用体验

#### Acceptance Criteria

1. WHEN Contacts Page 首次加载时,THE Contact Item SHALL 使用淡入和向上滑动的进入动画
2. THE Contact Item SHALL 为每个列表项添加 50-100ms 的延迟以创建交错效果
3. WHEN 用户点击 Contact Item 时,THE Contact Item SHALL 显示缩放和涟漪效果
4. THE Quick Action SHALL 在点击时显示 200ms 的缩放动画
5. THE Contacts Page SHALL 使用 Material 3 的标准缓动曲线(EaseInOut)

### Requirement 10

**User Story:** 作为用户,我希望在没有联系人时看到友好的空状态提示,引导我添加第一个联系人

#### Acceptance Criteria

1. WHEN 联系人列表为空时,THE Contacts Page SHALL 显示空状态视图
2. THE Contacts Page SHALL 在空状态视图中显示插图或图标
3. THE Contacts Page SHALL 显示提示文本"还没有联系人"
4. THE Contacts Page SHALL 显示次要文本"点击右下角的按钮添加联系人"
5. THE Contacts Page SHALL 在空状态视图中使用柔和的颜色和大尺寸图标(120dp)

### Requirement 11

**User Story:** 作为用户,我希望联系人页面能够正确处理加载状态和错误状态,提供清晰的反馈

#### Acceptance Criteria

1. WHEN 联系人数据正在加载时,THE Contacts Page SHALL 显示骨架屏加载动画
2. THE Contacts Page SHALL 使用 Material 3 的 shimmer 效果显示加载占位符
3. WHEN 加载失败时,THE Contacts Page SHALL 显示错误提示和重试按钮
4. THE Contacts Page SHALL 在错误状态下显示友好的错误消息
5. THE Contacts Page SHALL 提供"重试"按钮以重新加载数据

### Requirement 12

**User Story:** 作为用户,我希望联系人页面支持下拉刷新,以便手动同步最新的联系人数据

#### Acceptance Criteria

1. THE Contacts Page SHALL 支持下拉刷新手势
2. WHEN 用户下拉列表时,THE Contacts Page SHALL 显示刷新指示器
3. THE Contacts Page SHALL 在刷新完成后自动隐藏指示器
4. THE Contacts Page SHALL 使用 Material 3 的 PullRefreshIndicator 组件
5. THE Contacts Page SHALL 在刷新时保持列表的滚动位置

