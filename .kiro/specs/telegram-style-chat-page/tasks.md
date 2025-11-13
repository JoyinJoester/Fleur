# Implementation Plan

- [x] 1. 创建数据模型和映射器





  - 创建 ConversationUiModel、MessageUiModel、AttachmentUiModel 数据类
  - 实现 ConversationMapper 将邮件线程转换为对话模型
  - 实现 MessageMapper 将邮件转换为消息模型
  - 实现 AttachmentMapper 将附件转换为 UI 模型
  - _Requirements: 1.1, 1.2, 2.1_

- [x] 2. 实现 Domain Layer Use Cases


  - 创建 GetConversationsUseCase 获取对话列表，按线程分组邮件
  - 创建 GetConversationMessagesUseCase 获取对话中的所有消息
  - 创建 SendMessageUseCase 发送消息功能
  - 创建 SearchMessagesUseCase 在对话中搜索消息
  - _Requirements: 1.1, 2.1, 3.1, 8.1_

- [x] 3. 设置导航路由


  - 在 Screen.kt 中添加 Chat 和 ChatDetail 路由定义
  - 在 NavGraph.kt 中添加 Chat 和 ChatDetail 的 composable 路由
  - 更新 AppScaffold.kt 中的底部导航栏点击处理，连接 Chat 按钮到 Chat 路由
  - _Requirements: 1.2_

- [x] 4. 实现对话列表页面


- [x] 4.1 创建 ChatViewModel


  - 实现状态管理 (ChatUiState)
  - 实现 loadConversations 方法支持刷新和分页
  - 实现 loadMore 方法加载更多对话
  - 处理加载状态和错误
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 4.2 创建 ConversationItem 组件


  - 实现头像、联系人名称、最后消息预览、时间戳布局
  - 添加未读徽章显示
  - 实现点击交互
  - 应用 Material 3 设计规范
  - _Requirements: 1.3, 1.4, 1.5_

- [x] 4.3 创建 ChatScreen UI


  - 实现 TopAppBar 包含标题、搜索和菜单图标
  - 使用 LazyColumn 显示对话列表
  - 实现下拉刷新功能
  - 实现滚动到底部时自动加载更多
  - 添加空状态和错误状态显示
  - _Requirements: 1.1, 1.2, 1.6_


- [x] 5. 实现对话详情页面基础结构


- [x] 5.1 创建 ChatDetailViewModel


  - 实现状态管理 (ChatDetailUiState)
  - 实现 loadMessages 方法支持刷新和分页加载历史消息
  - 实现 updateInputText 方法管理输入文本
  - 实现 addAttachment 和 removeAttachment 方法管理附件
  - 实现 setReplyTo 方法设置回复消息
  - _Requirements: 2.1, 2.2, 3.1, 6.1_

- [x] 5.2 创建 DateDivider 组件


  - 显示日期分隔线 (今天、昨天、具体日期)
  - 应用 Material 3 样式
  - _Requirements: 2.1_

- [x] 5.3 增强 MessageBubble 组件


  - 基于现有 MessageBubble.kt 扩展功能
  - 添加长按手势支持
  - 添加发送状态指示器 (发送中、已发送、失败)
  - 支持显示回复引用
  - 优化气泡样式和布局
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.2, 7.3_

- [x] 5.4 创建 ChatDetailScreen UI


  - 实现 TopAppBar 包含返回按钮、联系人信息、搜索和更多图标
  - 使用 LazyColumn (reverseLayout) 显示消息列表
  - 实现消息按日期分组
  - 实现下拉刷新和滚动到顶部加载更多
  - 添加加载状态和错误状态显示
  - _Requirements: 2.1, 2.2, 2.5, 9.1, 9.2_

- [x] 6. 实现消息输入功能


- [x] 6.1 创建 MessageInputBar 组件


  - 实现文本输入框，支持多行自动扩展 (最大 5 行)
  - 添加附件按钮
  - 添加发送按钮，根据内容状态启用/禁用
  - 实现回复模式显示引用消息卡片
  - 应用 Material 3 样式和动画
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 6.2 实现发送消息功能

  - 在 ChatDetailViewModel 中实现 sendMessage 方法
  - 调用 SendMessageUseCase 发送消息
  - 处理发送状态 (发送中、成功、失败)
  - 发送成功后清空输入框和附件
  - 发送失败时显示错误并提供重试选项
  - _Requirements: 3.3, 3.4, 3.5, 7.4, 7.5_


- [x] 7. 实现图片附件功能




- [x] 7.1 创建 AttachmentBottomSheet 组件


  - 实现 ModalBottomSheet 包含拍照、图片、文件选项
  - 应用 Material 3 样式
  - 实现选项点击回调
  - _Requirements: 4.1, 5.1_

- [x] 7.2 实现图片选择和预览


  - 使用 ActivityResultContracts 实现图片选择
  - 支持多选图片 (最多 10 张)
  - 在 MessageInputBar 上方显示图片缩略图预览
  - 实现移除图片功能
  - _Requirements: 4.2, 4.3_

- [x] 7.3 创建 AttachmentCard 组件用于消息中的图片显示


  - 在 MessageBubble 中显示图片附件
  - 使用 Coil 加载图片，支持缩略图
  - 实现图片点击打开全屏查看
  - 显示图片加载状态和错误
  - _Requirements: 4.4, 4.5_

- [x] 7.4 实现图片压缩和上传


  - 压缩大于 5MB 的图片
  - 实现图片上传到邮件服务器
  - 显示上传进度
  - 处理上传失败
  - _Requirements: 4.6_

- [x] 8. 实现文件附件功能

- [x] 8.1 实现文件选择


  - 使用 Storage Access Framework 选择文件
  - 支持常见文件类型 (PDF, DOC, XLS, ZIP, TXT)
  - 限制文件大小为 25MB
  - 在 MessageInputBar 上方显示文件预览
  - _Requirements: 5.1, 5.2, 5.6_

- [x] 8.2 创建文件附件显示组件


  - 在 MessageBubble 中显示文件附件卡片
  - 显示文件名、大小、类型图标
  - 实现文件点击下载和打开
  - 显示下载进度
  - _Requirements: 5.2, 5.3, 5.4, 5.5_

- [x] 9. 实现图片全屏查看器

- [x] 9.1 创建 ImageViewer 组件


  - 使用 HorizontalPager 实现图片滑动切换
  - 使用 Modifier.transformable 实现缩放和平移手势
  - 使用 Coil 加载高清图片
  - 实现顶部工具栏 (关闭、分享、下载)
  - 实现底部图片索引指示器
  - _Requirements: 4.5_

- [x] 9.2 添加 ImageViewer 路由和导航


  - 在 Screen.kt 中添加 ImageViewer 路由
  - 在 NavGraph.kt 中添加 ImageViewer composable
  - 从 ChatDetailScreen 导航到 ImageViewer
  - _Requirements: 4.5_


- [x] 10. 实现消息操作功能

- [x] 10.1 创建 MessageActionsBottomSheet 组件


  - 实现 ModalBottomSheet 包含复制、回复、转发、删除选项
  - 根据消息类型条件显示选项
  - 应用 Material 3 样式
  - _Requirements: 6.1, 6.2_

- [x] 10.2 实现长按手势和操作

  - 在 MessageBubble 中添加长按手势检测
  - 长按时显示 MessageActionsBottomSheet
  - 实现复制文本到剪贴板
  - 实现回复功能，设置 replyTo 状态
  - 实现转发功能，导航到 ComposeScreen
  - 实现删除功能，调用 EmailRepository
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 10.3 实现滑动操作

  - 在 MessageBubble 中添加滑动手势
  - 左滑显示快速回复和删除按钮
  - 实现滑动动画
  - _Requirements: 6.6_

- [x] 11. 实现对话内搜索功能

- [x] 11.1 添加搜索 UI


  - 在 ChatDetailScreen TopAppBar 添加搜索图标
  - 点击搜索图标显示搜索输入框
  - 实现搜索输入框动画展开/收起
  - _Requirements: 8.1, 8.2_

- [x] 11.2 实现搜索功能

  - 在 ChatDetailViewModel 中实现 searchMessages 方法
  - 调用 SearchMessagesUseCase 搜索消息
  - 在 MessageBubble 中高亮匹配文本
  - 实现搜索结果导航 (上一个/下一个)
  - 显示搜索结果计数
  - _Requirements: 8.2, 8.3, 8.4, 8.5, 8.6_

- [x] 12. 性能优化

- [x] 12.1 优化 LazyColumn 性能


  - 为 LazyColumn items 使用稳定的 key
  - 使用 remember 缓存计算结果
  - 使用 derivedStateOf 优化状态派生
  - _Requirements: 9.3_

- [x] 12.2 优化图片加载

  - 配置 Coil 内存和磁盘缓存
  - 实现图片预加载策略
  - 为缩略图指定合适的尺寸
  - _Requirements: 9.3_

- [x] 12.3 实现分页加载优化

  - 检测滚动位置自动加载更多
  - 实现懒加载，避免一次加载过多数据
  - 优化加载状态显示
  - _Requirements: 9.1, 9.4, 9.5_


- [x] 13. 实现动画和转场

- [x] 13.1 添加页面转场动画


  - 配置 ChatScreen 和 ChatDetailScreen 的进入/退出动画
  - 使用 slideInHorizontally 和 fadeIn 组合
  - 应用 FastOutSlowInEasing 缓动函数
  - _Requirements: 9.2_

- [x] 13.2 添加消息出现动画


  - 为新消息添加 fadeIn 和 slideInVertically 动画
  - 使用 AnimatedVisibility 包装 MessageBubble
  - 配置合适的动画时长和缓动
  - _Requirements: 9.2_

- [x] 13.3 添加输入框和按钮动画

  - 为 MessageInputBar 添加 animateContentSize
  - 为发送按钮添加 scale 动画
  - 使用 spring 动画提升交互感
  - _Requirements: 9.6_

- [x] 14. 实现无障碍支持
- [x] 14.1 添加语义化标签


  - 为所有交互元素添加 contentDescription
  - 为消息气泡添加语义化角色
  - 为状态图标添加描述性文本
  - _Requirements: 所有需求_
标和颜色对比度
  - 确保所有可交互元素最小 48dp × 48dp
  - 验证文字和背景对比度符合 WCAG AA 标准
  - 实现焦点管理，发送后自动聚焦输入框
  - _Requirements: 所有需求_

- [x] 15. 错误处理和边界情况


- [x] 15.1 实现错误处理

  - 处理网络错误，显示 Snackbar 和重试选项
  - 处理加载错误，显示错误状态和重试按钮
  - 处理发送错误，在消息气泡显示错误图标
  - 处理附件错误，显示错误状态和重试选项
  - _Requirements: 所有需求_

- [x] 15.2 处理权限请求


  - 实现相机权限请求
  - 实现存储权限请求
  - 显示权限说明对话框
  - 处理权限拒绝情况
  - _Requirements: 4.1, 5.1_

- [x] 15.3 处理边界情况


  - 处理空对话列表状态
  - 处理空消息列表状态
  - 处理网络离线状态
  - 处理大文件和多图片情况
  - _Requirements: 所有需求_


- [x] 14.2 优化触摸目标和颜色对比度
  - 确保所有可交互元素最小 48dp × 48dp
  - 验证文字和背景对比度符合 WCAG AA 标准
  - 实现焦点管理，发送后自动聚焦输入框
  - _Requirements: 所有需求_

- [ ]* 16. 测试
- [ ]* 16.1 编写 ViewModel 单元测试
  - 测试 ChatViewModel 的 loadConversations 和 loadMore
  - 测试 ChatDetailViewModel 的 sendMessage 和 searchMessages
  - 测试错误处理和状态更新
  - _Requirements: 所有需求_

- [ ]* 16.2 编写 UseCase 单元测试
  - 测试 GetConversationsUseCase 的邮件分组逻辑
  - 测试 GetConversationMessagesUseCase 的消息排序
  - 测试 SendMessageUseCase 的邮件构建
  - 测试 SearchMessagesUseCase 的搜索逻辑
  - _Requirements: 所有需求_

- [ ]* 16.3 编写 Mapper 单元测试
  - 测试 ConversationMapper 的数据转换
  - 测试 MessageMapper 的状态判断
  - 测试 AttachmentMapper 的类型识别
  - _Requirements: 所有需求_

- [ ]* 16.4 编写 UI 组件测试
  - 测试 ConversationItem 的未读徽章显示
  - 测试 MessageBubble 的对齐和样式
  - 测试 MessageInputBar 的启用/禁用状态
  - 测试导航流程
  - _Requirements: 所有需求_

- [ ]* 16.5 性能测试
  - 测试 1000 条消息的滚动性能
  - 测试内存使用情况
  - 测试图片加载性能
  - 优化性能瓶颈
  - _Requirements: 9.3_

- [x] 17. 集成和完善




- [x] 17.1 集成到主应用


  - 确保底部导航栏 Chat 按钮正确导航
  - 测试与其他页面的导航流程
  - 验证状态保存和恢复
  - _Requirements: 所有需求_

- [x] 17.2 添加依赖项


  - 在 build.gradle.kts 中添加 Coil、Telephoto 等依赖
  - 同步项目并解决依赖冲突
  - _Requirements: 所有需求_

- [x] 17.3 最终测试和 Bug 修复


  - 进行端到端测试
  - 修复发现的 Bug
  - 优化用户体验
  - 更新文档
  - _Requirements: 所有需求_
