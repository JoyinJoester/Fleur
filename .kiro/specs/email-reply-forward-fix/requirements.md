# Requirements Document

## Introduction

本需求文档定义了邮件详情页面中回复、转发和全部回复功能的修复需求。当前这些按钮虽然在UI上可见，但点击后没有任何响应，因为对应的ViewModel方法只有TODO注释而没有实际实现。本功能将实现这些按钮的完整导航逻辑，使用户能够正常使用邮件回复和转发功能。

## Glossary

- **EmailDetailScreen**: 邮件详情页面，显示单封邮件的完整内容和操作按钮
- **EmailDetailViewModel**: 邮件详情页面的视图模型，管理页面状态和业务逻辑
- **ComposeScreen**: 邮件撰写页面，用于编写新邮件、回复或转发邮件
- **Navigation System**: 应用的导航系统，负责页面间的跳转和参数传递
- **Reply Action**: 回复操作，向原邮件发件人发送回复
- **Reply All Action**: 全部回复操作，向原邮件的所有收件人和发件人发送回复
- **Forward Action**: 转发操作，将原邮件内容转发给其他收件人

## Requirements

### Requirement 1

**User Story:** 作为邮件应用用户，我希望能够点击"回复"按钮回复邮件，以便快速回复发件人

#### Acceptance Criteria

1. WHEN User_System 点击邮件详情页面的"回复"按钮或FAB, THE EmailDetailViewModel SHALL 触发导航到撰写页面
2. WHEN EmailDetailViewModel 触发回复导航, THE Navigation_System SHALL 传递原邮件ID和回复类型参数到撰写页面
3. WHEN ComposeScreen 接收到回复类型参数, THE ComposeScreen SHALL 预填充收件人为原邮件发件人
4. WHEN ComposeScreen 预填充回复内容, THE ComposeScreen SHALL 在主题前添加"Re: "前缀（如果尚未存在）
5. WHEN ComposeScreen 显示回复界面, THE ComposeScreen SHALL 在独立的只读区域显示原始邮件内容
6. WHEN ComposeScreen 显示回复界面, THE ComposeScreen SHALL 提供独立的可编辑输入区域供用户输入回复内容

### Requirement 2

**User Story:** 作为邮件应用用户，我希望能够点击"全部回复"按钮回复所有人，以便让所有相关人员都能看到我的回复

#### Acceptance Criteria

1. WHEN User_System 点击邮件详情页面的"全部回复"按钮, THE EmailDetailViewModel SHALL 触发导航到撰写页面
2. WHEN EmailDetailViewModel 触发全部回复导航, THE Navigation_System SHALL 传递原邮件ID和全部回复类型参数到撰写页面
3. WHEN ComposeScreen 接收到全部回复类型参数, THE ComposeScreen SHALL 预填充收件人为原邮件发件人和所有收件人（排除当前用户）
4. WHEN ComposeScreen 预填充全部回复内容, THE ComposeScreen SHALL 预填充抄送人为原邮件的所有抄送人（排除当前用户）
5. WHEN ComposeScreen 预填充全部回复内容, THE ComposeScreen SHALL 在主题前添加"Re: "前缀（如果尚未存在）

### Requirement 3

**User Story:** 作为邮件应用用户，我希望能够点击"转发"按钮转发邮件，以便将邮件内容分享给其他人

#### Acceptance Criteria

1. WHEN User_System 点击邮件详情页面的"转发"按钮, THE EmailDetailViewModel SHALL 触发导航到撰写页面
2. WHEN EmailDetailViewModel 触发转发导航, THE Navigation_System SHALL 传递原邮件ID和转发类型参数到撰写页面
3. WHEN ComposeScreen 接收到转发类型参数, THE ComposeScreen SHALL 保持收件人字段为空
4. WHEN ComposeScreen 预填充转发内容, THE ComposeScreen SHALL 在主题前添加"Fwd: "前缀（如果尚未存在）
5. WHEN ComposeScreen 预填充转发内容, THE ComposeScreen SHALL 在正文中包含原邮件的完整内容和附件信息

### Requirement 4

**User Story:** 作为邮件应用用户，我希望回复和转发操作能够保持原邮件的上下文，以便收件人了解邮件的来龙去脉

#### Acceptance Criteria

1. WHEN ComposeScreen 显示原邮件内容区域, THE ComposeScreen SHALL 在独立的卡片或面板中显示原邮件
2. WHEN ComposeScreen 显示原邮件信息, THE ComposeScreen SHALL 包含原邮件的发件人、日期、收件人和主题
3. WHEN ComposeScreen 显示原邮件正文, THE ComposeScreen SHALL 在只读区域中完整显示原邮件内容
4. WHEN ComposeScreen 处理HTML格式的原邮件, THE ComposeScreen SHALL 保持原邮件的基本格式
5. WHEN ComposeScreen 转发包含附件的邮件, THE ComposeScreen SHALL 在转发邮件中保留原附件信息

### Requirement 5

**User Story:** 作为邮件应用用户，我希望回复界面清晰地区分原邮件和我的回复内容，以便更好地组织我的回复

#### Acceptance Criteria

1. WHEN ComposeScreen 处于回复或全部回复模式, THE ComposeScreen SHALL 在顶部显示回复内容输入区域
2. WHEN ComposeScreen 处于回复或全部回复模式, THE ComposeScreen SHALL 在下方显示原始邮件的只读展示区域
3. WHEN ComposeScreen 显示原始邮件区域, THE ComposeScreen SHALL 使用视觉分隔（如卡片、边框或背景色）区分原邮件和回复输入区
4. WHEN ComposeScreen 显示原始邮件区域, THE ComposeScreen SHALL 允许用户折叠或展开原邮件内容以节省屏幕空间
5. WHEN User_System 发送回复邮件, THE ComposeScreen SHALL 将回复内容和原邮件内容合并为符合邮件标准的格式
