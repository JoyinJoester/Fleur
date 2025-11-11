# Implementation Plan - 文件夹页面 M3E 优化

- [x] 1. 创建共享基础设施和配置




  - 创建 `FolderConfig.kt` 定义文件夹配置数据类（FolderConfig、FolderType、SwipeActionsConfig、EmptyStateConfig）
  - 创建 `FolderUiState.kt` 定义文件夹页面状态（emails、isLoading、isMultiSelectMode、selectedEmailIds、lastAction）
  - 创建 `EmailAction.kt` 枚举定义所有邮件操作类型（DELETE、ARCHIVE、UNARCHIVE、STAR、UNSTAR、RESTORE）
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1_

- [x] 2. 扩展数据层支持文件夹查询



  - 在 `EmailDao.kt` 中添加 `getSentEmails()` 方法查询已发送邮件
  - 在 `EmailDao.kt` 中添加 `getDraftEmails()` 方法查询草稿邮件
  - 在 `EmailDao.kt` 中添加 `getStarredEmails()` 方法查询星标邮件
  - 在 `EmailDao.kt` 中添加 `getArchivedEmails()` 方法查询归档邮件
  - 在 `EmailDao.kt` 中添加 `getTrashedEmails()` 方法查询垃圾箱邮件
  - 在 `EmailRepository.kt` 和 `EmailRepositoryImpl.kt` 中添加按文件夹类型查询的方法
  - _Requirements: 1.2, 2.2, 3.2, 4.2, 5.2_

- [x] 3. 创建领域层 UseCase


  - 创建 `RestoreEmailUseCase.kt` 实现邮件恢复逻辑（从垃圾箱恢复到收件箱）
  - 创建 `GetFolderEmailsUseCase.kt` 实现按文件夹类型获取邮件列表
  - 在现有 `ToggleStarUseCase.kt` 中添加批量星标操作支持
  - _Requirements: 3.4, 3.5, 4.4, 4.5, 5.4, 5.5_

- [x] 4. 实现共享 FolderViewModel



  - 创建 `FolderViewModel.kt` 管理文件夹页面状态和业务逻辑
  - 实现 `loadEmails()` 方法根据 folderType 加载对应邮件列表
  - 实现 `refreshEmails()` 方法支持下拉刷新
  - 实现 `loadNextPage()` 方法支持分页加载
  - 实现 `performAction()` 方法处理单个邮件操作（删除、归档、星标等）
  - 实现 `performBatchAction()` 方法处理批量邮件操作
  - 实现 `undoLastAction()` 方法支持撤销最近的操作
  - 实现多选模式相关方法（enterMultiSelectMode、exitMultiSelectMode、toggleEmailSelection）
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 7.6, 9.5_

- [x] 5. 创建空状态组件


  - 创建 `FolderEmptyState.kt` 组件显示文件夹为空时的引导界面
  - 实现图标、标题、描述文本的布局（垂直居中，32dp 内边距）
  - 实现可选操作按钮（如草稿箱的"撰写邮件"按钮）
  - 使用 Material 3 颜色和字体样式（outline 颜色 50% 透明度）
  - _Requirements: 1.3, 2.3, 3.3, 4.3, 5.3_


- [x] 6. 创建错误状态组件

  - 创建 `FolderErrorState.kt` 组件显示加载失败时的错误界面
  - 实现错误图标、错误消息和重试按钮的布局
  - 使用 `FleurError.getUserMessage()` 显示用户友好的错误信息
  - 添加重试按钮触发重新加载
  - _Requirements: 10.3, 10.4_

- [x] 7. 实现 FolderScreenTemplate 核心模板



  - 创建 `FolderScreenTemplate.kt` 作为所有文件夹页面的共享模板
  - 实现顶部应用栏（标题、返回按钮、操作按钮）
  - 实现邮件列表渲染（使用 LazyColumn 虚拟滚动）
  - 实现下拉刷新功能（PullRefresh）
  - 实现分页加载触发器（滚动到底部自动加载）
  - 实现空状态和错误状态的条件渲染
  - 实现 FAB 显示逻辑（根据 config.showFab 决定）
  - 实现 Snackbar 提示（操作结果反馈和撤销选项）
  - _Requirements: 1.1, 1.4, 1.5, 7.6, 9.1, 9.3, 9.4, 10.1, 10.3_

- [x] 8. 实现多选模式 UI

  - 在 `FolderScreenTemplate.kt` 中实现多选工具栏（替换顶部应用栏）
  - 显示已选邮件数量和批量操作按钮（删除、归档、标记已读）
  - 在邮件列表项中添加选中状态视觉反馈（复选框、背景色）
  - 实现长按进入多选模式的触觉反馈（HapticFeedbackType.LongPress）
  - 实现点击切换选中状态的缩放动画（200ms，scale 0.95f）
  - 实现"全选"和"取消"按钮
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 10.1_


- [x] 9. 优化滑动操作体验





  - 在 `SwipeableEmailItem.kt` 中调整滑动阈值为 30%（positionalThreshold）
  - 实现滑动进度实时反馈（背景色渐变、图标缩放）
  - 在滑动触发时添加触觉反馈（HapticFeedbackType.LongPress）
  - 实现滑动释放后的弹性恢复动画（小于阈值时）
  - 实现滑动完成后的滑出动画（大于阈值时）
  - 在滑动过程中禁用列表垂直滚动
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 10.1_


- [x] 10. 实现已发送页面

  - 创建 `SentScreen.kt` 使用 FolderScreenTemplate
  - 配置 FolderConfig（title="已发送"，folderType=SENT）
  - 配置空状态（icon=Send，title="暂无已发送邮件"）
  - 配置滑动操作（左滑删除）
  - 连接 ViewModel 和导航回调
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_


- [x] 11. 实现草稿箱页面

  - 创建 `DraftsScreen.kt` 使用 FolderScreenTemplate
  - 配置 FolderConfig（title="草稿箱"，folderType=DRAFTS）
  - 配置空状态（icon=Edit，title="暂无草稿"，actionButton="撰写邮件"）
  - 配置滑动操作（左滑删除）
  - 配置 FAB（显示撰写邮件按钮）
  - 实现点击草稿项导航到撰写页面并加载草稿内容
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_


- [x] 12. 实现星标邮件页面

  - 创建 `StarredScreen.kt` 使用 FolderScreenTemplate
  - 配置 FolderConfig（title="星标邮件"，folderType=STARRED）
  - 配置空状态（icon=Star，title="暂无星标邮件"，description="为重要邮件添加星标"）
  - 配置滑动操作（左滑取消星标，右滑归档）
  - 实现取消星标的淡出动画和 Snackbar 提示
  - 实现撤销取消星标的淡入动画
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [x] 13. 实现归档页面


  - 创建 `ArchiveScreen.kt` 使用 FolderScreenTemplate
  - 配置 FolderConfig（title="归档"，folderType=ARCHIVE）
  - 配置空状态（icon=Archive，title="暂无归档邮件"，description="归档的邮件将显示在这里"）
  - 配置滑动操作（右滑移至收件箱，左滑删除）
  - 实现移至收件箱的滑出动画和 Snackbar 提示
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

- [x] 14. 实现垃圾箱页面



  - 创建 `TrashScreen.kt` 使用 FolderScreenTemplate
  - 配置 FolderConfig（title="垃圾箱"，folderType=TRASH）
  - 配置空状态（icon=Delete，title="垃圾箱为空"）
  - 配置滑动操作（右滑恢复到收件箱）
  - 在顶部应用栏添加"清空垃圾箱"操作按钮
  - 实现清空垃圾箱确认对话框（AlertDialog）
  - 实现永久删除所有邮件的逻辑和 Snackbar 提示
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8_

- [x] 15. 更新导航图


  - 在 `NavGraph.kt` 中添加 Sent、Drafts、Starred、Archive、Trash 路由
  - 为每个路由配置 composable 并传递导航回调
  - 使用 pageEnterAnimation 和 pageExitAnimation 实现 300ms 过渡动画
  - 更新 `Screen.kt` 添加新的路由常量
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1_


- [x] 16. 更新导航抽屉连接


  - 在 `FleurNavigationDrawer.kt` 中更新导航回调连接到新页面
  - 确保 onNavigateToSent、onNavigateToDrafts 等回调正确导航
  - 在 `AppScaffold.kt` 中连接导航抽屉和 NavController
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1_

- [x] 17. 实现自适应布局支持

  - 创建 `WindowSizeClass.kt` 定义响应式断点（COMPACT、MEDIUM、EXPANDED）
  - 创建 `rememberWindowSizeClass()` Composable 函数检测当前窗口大小
  - 在 `FolderScreenTemplate.kt` 中实现自适应布局切换逻辑
  - 实现 CompactLayout（单列）、MediumLayout（双列）、ExpandedLayout（双窗格）
  - 实现设备方向切换的平滑过渡动画（300ms）
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_


- [x] 18. 实现性能优化





  - 在邮件列表中实现虚拟滚动（LazyColumn 已有，确保使用 key）
  - 实现图片延迟加载（检测滚动状态，滚动时显示占位符）
  - 创建 `EmailListItemSkeleton.kt` 骨架屏组件
  - 在首次加载时显示骨架屏占位符（500ms 内）
  - 实现邮件列表项的交错淡入动画（每项延迟 50ms）
  - _Requirements: 9.1, 9.2, 9.3, 9.4_


- [x] 19. 实现视觉反馈优化





  - 确保所有邮件列表项有 100ms 涟漪动画（Material 3 默认）
  - 在邮件操作执行中显示半透明遮罩和进度指示器
  - 实现操作成功的 Snackbar（绿色强调色）
  - 实现操作失败的 Snackbar（红色强调色，带重试按钮）
  - 在同步数据时显示顶部线性进度指示器
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_


- [x] 20. 添加无障碍支持

  - 为所有邮件列表项添加语义化 contentDescription
  - 为未读邮件添加 stateDescription="未读"
  - 确保所有可交互元素至少 48dp × 48dp 触摸目标
  - 为多选模式添加屏幕阅读器提示
  - 为滑动操作添加替代的长按菜单（无障碍模式）
  - _Requirements: 6.1, 7.1, 10.1_

- [ ]* 21. 编写单元测试
  - 为 FolderViewModel 编写测试（邮件加载、分页、操作、多选、撤销）
  - 为各个 UseCase 编写测试（RestoreEmailUseCase、GetFolderEmailsUseCase）
  - 为 EmailDao 的新查询方法编写测试
  - _Requirements: 1.1-5.8_

- [ ]* 22. 编写 UI 测试
  - 为 FolderScreenTemplate 编写 Compose 测试（空状态、列表渲染、刷新、滑动）
  - 为多选模式编写交互测试
  - 为每个具体页面编写集成测试（完整用户流程）
  - _Requirements: 1.1-10.6_

