# 实现计划

- [x] 1. 创建设计常量和动画配置





  - 在 `ui/theme` 目录下创建 `DrawerDimens.kt` 文件，定义所有尺寸常量（卡片圆角、内边距、头像大小、最大高度等）
  - 在 `ui/theme` 目录下创建 `DrawerAnimations.kt` 文件，定义动画参数（Spring配置、持续时间等）
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 2. 实现 ExpandableAccountCard 核心组件


- [x] 2.1 创建折叠状态视图 (CollapsedAccountView)


  - 实现 `CollapsedAccountView` Composable，使用 Material 3 Card 组件
  - 应用 `primaryContainer` 背景色和 16.dp 圆角
  - 布局包含：圆形头像（48.dp）、账户名称、邮箱地址、展开图标
  - 添加点击事件处理和语义标签
  - _Requirements: 3.1, 3.2, 8.1_


- [x] 2.2 创建展开状态视图 (ExpandedAccountList)

  - 实现 `ExpandedAccountList` Composable，使用 LazyColumn
  - 设置最大高度为 240.dp，启用垂直滚动
  - 实现单个账户项布局（72.dp高度，圆形头像40.dp）
  - 添加选中状态样式（`secondaryContainer` 背景）和默认账户星标显示
  - _Requirements: 3.4, 3.5, 5.1, 5.2, 5.3_


- [x] 2.3 组合 ExpandableAccountCard 主组件

  - 创建 `ExpandableAccountCard` Composable，管理展开/收起状态
  - 使用 `AnimatedVisibility` 实现展开/收起动画（expandVertically + fadeIn/fadeOut）
  - 使用 `animateFloatAsState` 实现图标旋转动画（0° ↔ 180°）
  - 实现账户切换时的 `AnimatedContent` 过渡效果
  - 添加点击外部区域自动收起的逻辑
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.3, 5.4_

- [x] 3. 优化 FolderSection 视觉效果




- [x] 3.1 增强文件夹项样式

  - 修改 `DrawerItem` Composable，添加 12.dp 圆角
  - 为选中状态添加左侧 4.dp 宽的指示条
  - 优化图标大小（24.dp）和间距（图标与文字 12.dp）
  - 应用 `primaryContainer` 作为选中背景色
  - _Requirements: 1.2, 4.1, 4.3_


- [x] 3.2 优化 Badge 显示效果

  - 更新 Badge 组件样式，使用主题色背景
  - 添加 Badge 出现/消失的淡入淡出动画
  - 确保 Badge 数字格式化（>99 显示 "99+"）
  - _Requirements: 4.2_

- [x] 4. 集成新组件到 FleurNavigationDrawer



- [x] 4.1 重构 FleurNavigationDrawer 结构

  - 替换现有的 `UserInfoSection` 为新的 `ExpandableAccountCard`
  - 添加 `isAccountCardExpanded` 状态管理
  - 移除旧的 `AccountSection`（功能已整合到 ExpandableAccountCard）
  - 保持现有接口不变，确保向后兼容
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 4.2 优化整体布局和间距

  - 应用 `DrawerDimens` 中定义的间距常量
  - 优化各区域之间的分隔线样式（使用细线或留白）
  - 确保侧边栏边缘有适当内边距（避免内容贴边）
  - 优化区域标题样式（小号大写字母、适当字间距）
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 5. 实现交互增强和动画效果

- [x] 5.1 添加悬停和点击反馈

  - 为所有 Drawer Item 添加悬停时的背景色过渡动画
  - 实现点击时的涟漪效果和微妙缩放动画
  - 为图标添加悬停时的缩放动画
  - _Requirements: 2.1, 2.2, 8.3_

- [x] 5.2 优化侧边栏打开/关闭动画

  - 调整 `ModalNavigationDrawer` 的滑入滑出动画参数
  - 确保动画流畅且符合 Material 3 规范
  - _Requirements: 2.4_

- [x] 6. 深色模式适配和主题优化



- [x] 6.1 验证深色模式配色

  - 测试所有组件在深色模式下的显示效果
  - 调整毛玻璃效果的透明度和模糊度（如需要）
  - 确保文字和背景有足够对比度
  - _Requirements: 7.1, 7.2, 7.3, 7.4_




- [x] 6.2 优化图标颜色和填充

  - 为选中状态的图标添加主题色填充
  - 考虑为特定文件夹使用自定义彩色图标
  - _Requirements: 8.2, 8.4_

- [x] 7. 错误处理和边界情况


- [x] 7.1 处理空账户列表


  - 实现账户列表为空时的占位符显示
  - 显示"添加账户"提示和按钮
  - 禁用展开功能，点击直接导航到账户管理
  - _Requirements: 3.1, 3.2_

- [x] 7.2 处理单个账户情况

  - 当只有一个账户时，隐藏展开图标
  - 点击账户卡片直接导航到账户管理
  - _Requirements: 3.2_

- [x] 7.3 优化性能和内存管理


  - 使用 `remember` 和 `derivedStateOf` 优化计算
  - 为 LazyColumn 的 items 添加 key 参数
  - 验证展开/收起不会导致内存泄漏
  - _Requirements: 1.3, 2.3_

- [x] 8. 可访问性增强



- [x] 8.1 添加语义标签和角色

  - 为展开/收起按钮添加 `contentDescription`
  - 为账户项添加语义角色（Role.Button）
  - 添加状态描述（"已展开"/"已折叠"）
  - _Requirements: 所有交互元素_

- [x] 8.2 确保触摸目标大小

  - 验证所有可点击元素的最小触摸目标为 48.dp
  - 确保账户卡片整体可点击
  - _Requirements: 所有交互元素_

- [ ]* 9. 测试和验证
- [ ]* 9.1 编写组件单元测试
  - 测试 ExpandableAccountCard 的展开/收起逻辑
  - 测试账户切换回调
  - 测试空账户列表处理
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ]* 9.2 编写 UI 交互测试
  - 测试点击账户卡片展开
  - 测试滚动账户列表
  - 测试切换账户
  - 测试深色模式切换
  - _Requirements: 所有需求_

- [ ]* 9.3 性能测试
  - 测试大量账户（10+）时的滚动流畅度
  - 测试动画帧率
  - 使用 Profiler 检查内存使用
  - _Requirements: 3.5, 性能相关_

