# Implementation Plan

- [x] 1. 创建数据模型和领域层





  - 创建 ContactUiModel 数据类,包含联系人的所有信息字段
  - 创建 ContactsUiState 数据类,管理页面状态
  - 实现 GetContactsUseCase,从邮件中提取联系人
  - 实现 SearchContactsUseCase,支持按姓名和邮箱搜索
  - 实现 ContactMapper,负责从 Email 转换为 ContactUiModel
  - _Requirements: 1.1, 1.2, 2.1, 2.2_

- [x] 2. 实现 ContactsViewModel


  - 创建 ContactsViewModel 类,继承 ViewModel
  - 实现 loadContacts() 方法,加载联系人列表
  - 实现 searchContacts() 方法,处理搜索逻辑
  - 实现 setSearchActive() 方法,管理搜索状态
  - 实现 showContactDetail() 和 hideContactDetail() 方法
  - 实现 navigateToChat() 和 navigateToCompose() 导航方法
  - 添加错误处理和状态更新逻辑
  - _Requirements: 1.1, 2.1, 5.1, 5.2_

- [x] 3. 创建 ContactAvatar 组件


  - 实现 ContactAvatar Composable 函数
  - 支持从 URL 加载头像图片(使用 Coil)
  - 实现首字母缩写头像逻辑
  - 实现 getColorForName() 函数,根据姓名生成颜色
  - 实现 getInitials() 函数,提取姓名首字母
  - 使用 CircleShape 裁剪头像
  - _Requirements: 1.2, 1.6_

- [x] 4. 创建 OnlineIndicator 组件


  - 实现 OnlineIndicator Composable 函数
  - 使用 14dp 圆形,2dp 白色边框
  - 在线状态使用绿色(#4CAF50)
  - 离线状态使用 outlineVariant 颜色
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 5. 创建 ContactItem 组件


  - 实现 ContactItem Composable 函数
  - 使用 Row 布局,高度 72dp
  - 左侧显示 ContactAvatar 和 OnlineIndicator
  - 中间显示姓名和邮箱(Column 布局)
  - 右侧显示快速操作按钮(聊天、邮件)
  - 实现点击事件处理
  - 添加涟漪效果和悬停状态
  - 实现进入动画(fadeIn + slideInVertically)
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 9.1, 9.2, 9.3, 9.4, 9.5_

- [x] 6. 创建 ContactGroupHeader 组件


  - 实现 ContactGroupHeader Composable 函数
  - 使用 Box 布局,高度 48dp
  - 背景色使用 surfaceVariant
  - 显示字母标题,使用 titleMedium 样式
  - _Requirements: 7.2, 7.5, 7.6_

- [x] 7. 创建 AlphabetIndex 组件


  - 实现 AlphabetIndex Composable 函数
  - 使用 Column 布局,显示 A-Z 字母
  - 当前字母使用 primary 色和加粗样式
  - 其他字母使用 onSurfaceVariant 色
  - 实现点击事件,触发滚动到对应分组
  - _Requirements: 7.3, 7.4_

- [x] 8. 创建 ContactsList 组件



  - 实现 ContactsList Composable 函数
  - 使用 LazyColumn 实现虚拟滚动
  - 按首字母分组联系人(使用 groupBy)
  - 使用 stickyHeader 显示字母标题
  - 集成 AlphabetIndex 组件
  - 实现滚动到指定字母的逻辑
  - 添加分隔线(Divider)
  - 使用 animateItemPlacement 实现列表动画
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [x] 9. 创建 ContactsSearchBar 组件


  - 实现 ContactsSearchBar Composable 函数
  - 使用 Material 3 SearchBar 组件
  - 高度 56dp,圆角 28dp
  - 添加搜索图标和清除按钮
  - 实现展开/收起动画
  - 在展开状态下显示搜索结果列表
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

- [x] 10. 创建 AddContactFAB 组件


  - 实现 AddContactFAB Composable 函数
  - 使用 FloatingActionButton,尺寸 56dp
  - 使用 Icons.Filled.Add 图标
  - 背景色使用 primaryContainer
  - 实现滚动时自动隐藏/显示逻辑
  - 添加 scaleIn/scaleOut 动画
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

- [x] 11. 创建 ContactDetailBottomSheet 组件


  - 实现 ContactDetailBottomSheet Composable 函数
  - 使用 ModalBottomSheet,圆角 28dp
  - 顶部显示大头像(80dp)和基本信息
  - 实现 ActionButton 子组件(聊天、邮件、编辑、删除)
  - 实现 DetailItem 子组件(电话、地址、备注)
  - 添加拖拽关闭手势支持
  - 实现内容淡入动画
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [x] 12. 创建加载和空状态组件


  - 实现 ContactsLoadingState Composable 函数
  - 创建 ContactItemSkeleton 骨架屏组件
  - 实现 shimmer 动画效果
  - 实现 ContactsEmptyState Composable 函数
  - 显示空状态图标(120dp)和提示文本
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 11.1, 11.2, 11.3, 11.4, 11.5_

- [x] 13. 实现 ContactsScreen 主页面


  - 创建 ContactsScreen Composable 函数
  - 使用 Scaffold 布局,包含 TopAppBar
  - 集成 ContactsSearchBar 组件
  - 集成 ContactsList 组件
  - 集成 AddContactFAB 组件
  - 集成 ContactDetailBottomSheet 组件
  - 实现下拉刷新功能(PullRefresh)
  - 处理加载、空状态和错误状态
  - 实现 Snackbar 错误提示
  - _Requirements: 1.1, 2.1, 5.1, 6.1, 10.1, 11.1, 12.1, 12.2, 12.3, 12.4, 12.5_

- [x] 14. 添加导航路由和底部导航栏集成


  - 在 Screen.kt 中添加 Contacts 路由定义
  - 在 NavGraph.kt 中添加 ContactsScreen 路由配置
  - 在 FleurBottomNavigationBar 中添加联系人按钮
  - 使用 Icons.Filled.People 和 Icons.Outlined.People 图标
  - 实现页面转场动画(slideIn/slideOut)
  - 测试从联系人页面导航到聊天和撰写邮件页面
  - _Requirements: 3.1, 3.2, 4.1, 4.2_

- [x] 15. 实现性能优化


  - 为 LazyColumn 添加稳定的 key
  - 使用 remember 缓存计算结果
  - 使用 derivedStateOf 优化分组逻辑
  - 为头像加载配置 Coil 缓存策略
  - 实现搜索防抖(300ms debounce)
  - 优化列表滚动性能,确保 60fps
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_

- [x] 16. 添加无障碍支持


  - 为所有图标添加 contentDescription
  - 确保所有可点击元素至少 48dp × 48dp
  - 为 ContactItem 添加语义化标签
  - 验证颜色对比度符合 WCAG AA 标准
  - 实现焦点管理,搜索框自动获取焦点
  - _Requirements: 1.6, 3.6, 4.6_

- [ ]* 17. 编写单元测试
  - 测试 ContactsViewModel 的 loadContacts 方法
  - 测试 ContactsViewModel 的 searchContacts 方法
  - 测试 GetContactsUseCase 的联系人提取和去重逻辑
  - 测试 SearchContactsUseCase 的搜索过滤逻辑
  - 测试 ContactMapper 的转换逻辑
  - _Requirements: 1.1, 2.1_

- [ ]* 18. 编写 UI 测试
  - 测试点击联系人显示详情面板
  - 测试点击聊天按钮导航到聊天页面
  - 测试点击邮件按钮导航到撰写页面
  - 测试搜索功能的过滤效果
  - 测试字母索引的滚动功能
  - 测试 FAB 的显示/隐藏逻辑
  - _Requirements: 3.1, 4.1, 5.1, 7.4_

- [x] 19. 更新搜索栏占位符文本







  - 修改 ContactsSearchBar 组件的 placeholder 文本
  - 将"搜索联系人(支持拼音/首字母)"改为"搜索联系人"
  - 确保占位符文本简洁明了
  - _Requirements: 2.7_

- [x] 20. 更新 AlphabetIndex 为动态显示

- [x] 20. 更新 AlphabetIndex 为动态显示

  - 修改 AlphabetIndex 组件接受 availableLetters 参数
  - 实现 getContactInitial() 辅助函数,将数字和特殊字符映射为"#"
  - 更新 ContactsList 组件,计算实际存在的首字母列表
  - 只渲染实际存在的首字母,不显示全部A-Z
  - 确保"#"符号正确显示在列表中(如果有数字或特殊字符开头的联系人)
  - 更新滚动逻辑以适配动态字母列表
  - _Requirements: 7.4, 7.5, 7.6_

