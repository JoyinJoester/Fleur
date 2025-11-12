# Implementation Plan

- [x] 1. 创建基础数据模型和工具类





  - 创建 `ComposeMode` 枚举定义撰写模式（NEW, REPLY, REPLY_ALL, FORWARD, DRAFT）
  - 创建 `EmailContentFormatter` 工具类，实现邮件内容格式化功能（添加前缀、构建引用正文）
  - _Requirements: 1.4, 1.5, 2.5, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4_

- [x] 2. 修改路由系统支持撰写模式参数


  - 修改 `Screen.Compose` 对象，添加 `mode` 和 `referenceId` 参数支持
  - 实现 `createReplyRoute()`, `createReplyAllRoute()`, `createForwardRoute()` 便捷方法
  - 修改 `NavGraph` 中的 `compose` 路由配置，添加参数定义
  - _Requirements: 1.2, 2.2, 3.2_

- [x] 3. 扩展 ComposeViewModel 支持回复和转发


  - 修改 `ComposeUiState` 添加 `isLoading`, `composeMode`, `referenceEmailId` 字段
  - 在 `ComposeViewModel` 构造函数中从 `SavedStateHandle` 获取模式和原邮件 ID 参数
  - 添加 `EmailRepository` 依赖到 `ComposeViewModel`
  - 实现 `loadReferenceEmail()` 方法加载原邮件
  - 实现 `prefillContent()` 方法根据模式分发预填充逻辑
  - _Requirements: 1.2, 2.2, 3.2_

- [x] 4. 实现回复模式的预填充逻辑

  - 实现 `prefillReply()` 方法
  - 预填充收件人为原邮件发件人
  - 使用 `EmailContentFormatter.addReplyPrefix()` 添加主题前缀
  - 使用 `EmailContentFormatter.buildReplyBody()` 构建引用正文
  - _Requirements: 1.3, 1.4, 1.5, 4.1, 4.2, 4.3, 4.4_


- [ ] 5. 实现全部回复模式的预填充逻辑
  - 实现 `prefillReplyAll()` 方法
  - 预填充收件人为原发件人和所有收件人（排除当前用户）
  - 预填充抄送人为原抄送人（排除当前用户）
  - 自动显示抄送/密送字段（如果有抄送人）
  - 使用 `EmailContentFormatter.addReplyPrefix()` 添加主题前缀
  - 使用 `EmailContentFormatter.buildReplyBody()` 构建引用正文
  - _Requirements: 2.3, 2.4, 2.5, 4.1, 4.2, 4.3, 4.4_


- [ ] 6. 实现转发模式的预填充逻辑
  - 实现 `prefillForward()` 方法
  - 保持收件人字段为空
  - 使用 `EmailContentFormatter.addForwardPrefix()` 添加主题前缀
  - 使用 `EmailContentFormatter.buildForwardBody()` 构建转发正文
  - 保留原邮件附件引用

  - _Requirements: 3.3, 3.4, 3.5, 4.1, 4.2, 4.4, 4.5_

- [x] 7. 实现 EmailDetailViewModel 的导航逻辑

  - 添加 `onNavigateToCompose` 导航回调字段
  - 实现 `setNavigationCallback()` 方法设置导航回调
  - 实现 `reply()` 方法触发回复导航
  - 实现 `replyAll()` 方法触发全部回复导航
  - 实现 `forward()` 方法触发转发导航
  - 添加空值检查确保邮件存在时才触发导航
  - _Requirements: 1.1, 2.1, 3.1_

- [x] 8. 更新 EmailDetailScreen 和 NavGraph


  - 修改 `EmailDetailScreen` 的 `onNavigateToCompose` 参数签名，接收 `emailId` 和 `mode`
  - 在 `EmailDetailScreen` 中使用 `LaunchedEffect` 设置 ViewModel 的导航回调
  - 修改 `NavGraph` 中 `EmailDetail` 的 composable，实现导航逻辑根据模式构建正确的路由
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 3.1, 3.2_

- [x]* 9. 在 ComposeScreen 添加加载状态显示

  - 在 `ComposeScreen` 中检测 `uiState.isLoading` 状态
  - 显示加载指示器（骨架屏或进度条）当加载原邮件时
  - 确保加载完成后正确显示预填充的内容
  - _Requirements: 1.2, 2.2, 3.2_



- [x] 10. 添加错误处理和用户提示

  - 在 `ComposeViewModel.loadReferenceEmail()` 中添加 try-catch 错误处理
  - 加载失败时显示友好的错误消息
  - 允许用户在加载失败后继续撰写新邮件
  - 添加日志记录用于调试
  - _Requirements: 1.2, 2.2, 3.2_
