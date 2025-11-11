# Implementation Plan

- [x] 1. 优化EmailListItem卡片设计和视觉效果





  - 更新FleurCard组件，将默认圆角从12dp改为16dp
  - 调整卡片elevation：默认从2dp改为4dp，悬停从6dp改为8dp
  - 优化卡片内边距为16dp，外边距为水平16dp、垂直6dp
  - 增强玻璃拟态效果：浅色模式下使用20dp blur和90%透明度
  - 更新卡片选中状态的边框宽度为2dp
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. 实现EmailListItem的交错进入动画


  - 为EmailListItem添加animationIndex参数
  - 实现stagger动画：每项延迟50ms，使用fadeIn + slideInVertically
  - 配置动画时长为200ms，使用FastOutSlowIn缓动曲线
  - 在LazyColumn中为每个item传递正确的index
  - 使用animateItemPlacement()实现列表项位置变化动画
  - _Requirements: 2.1, 2.5_

- [x] 3. 优化EmailListItem的悬停和交互动画


  - 实现悬停状态的elevation动画（4dp → 8dp，150ms）
  - 实现悬停状态的scale动画（1.0 → 1.02，150ms）
  - 优化点击涟漪效果，使用150ms duration
  - 实现选中状态的边框动画
  - 添加状态转换的流畅过渡效果
  - _Requirements: 1.3, 2.4, 5.1, 5.2_

- [x] 4. 实现EmailListItem悬停时的操作按钮


  - 创建HoverActionButtons组件，包含星标、归档、删除三个按钮
  - 实现按钮的淡入淡出动画（fadeIn + expandHorizontally，200ms）
  - 配置按钮尺寸为40dp×40dp，间距4dp
  - 集成到EmailListItem右侧，垂直居中对齐
  - 实现按钮点击事件处理（onStar, onArchive, onDelete）
  - _Requirements: 5.5_

- [x] 5. 优化EmailListItem的滑动操作动画


  - 更新SwipeableEmailItem的滑动动画时长为400ms
  - 使用DecelerateEasing缓动曲线替代默认曲线
  - 优化滑动背景色：左滑绿色（归档），右滑红色（删除）
  - 实现滑动操作完成时的触觉反馈
  - 调整滑动threshold为0.3（30%宽度触发）
  - _Requirements: 2.3, 5.3, 5.4_

- [x] 6. 增强EmailListItem的信息层级和视觉设计



  - 优化账户颜色指示器尺寸为8dp圆形
  - 优化未读标记尺寸为6dp圆形，位置在账户指示器下方4dp
  - 为未读邮件的发件人和主题应用粗体字重
  - 优化时间戳显示格式和样式（labelSmall）
  - 优化附件图标和星标图标尺寸为16dp
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 7. 优化InboxTopAppBar的设计和交互


  - 将搜索框圆角从当前值改为24dp（完全圆角）
  - 优化搜索框高度为48dp，使用surfaceVariant背景色
  - 优化头像按钮尺寸为40dp圆形
  - 实现TopAppBar的滚动阴影效果（滚动时elevation 0dp → 4dp）
  - 优化搜索框和头像之间的间距为8dp
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 8. 实现下拉刷新的流畅动画


  - 配置PullRefreshIndicator的动画时长为300ms
  - 使用MaterialTheme颜色：背景surface，内容primary
  - 优化刷新指示器的位置和大小
  - 实现刷新完成后的淡出动画
  - 添加刷新触发时的触觉反馈
  - _Requirements: 2.2_


- [x] 9. 实现加载状态的骨架屏效果

  - 创建EmailListItemSkeleton组件
  - 实现shimmer动画效果（透明度0.3 ↔ 0.7，1000ms循环）
  - 设计骨架屏布局：账户指示器、发件人、主题、预览
  - 在初始加载时显示3-5个骨架屏项
  - 配置骨架屏的圆角和间距与实际卡片一致
  - _Requirements: 6.4_


- [x] 10. 实现空状态的友好设计

  - 创建EmptyInboxState组件
  - 添加收件箱图标（120dp大小）
  - 设计空状态文案："收件箱为空" + "您的所有邮件都已处理完毕"
  - 使用合适的颜色和透明度（onSurfaceVariant，60%透明度）
  - 实现空状态的淡入动画
  - _Requirements: 6.2_


- [ ] 11. 优化EmailList的性能
  - 确认LazyColumn使用正确的key（email.id）
  - 实现分页加载：每次加载20封邮件
  - 优化列表项的remember使用，缓存不变的计算结果
  - 添加contentPadding为垂直8dp
  - 确保滚动性能达到60fps
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_


- [x] 12. 实现无障碍支持

  - 为所有图标添加contentDescription
  - 确保所有可点击元素的触摸目标至少48dp×48dp
  - 验证文本与背景的颜色对比度≥4.5:1
  - 测试系统字体缩放支持
  - 为邮件卡片添加语义标签（isRead, isStarred等）
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ]* 13. 编写组件单元测试
  - 测试EmailListItem的不同状态渲染（默认、悬停、选中）
  - 测试stagger动画的延迟计算逻辑
  - 测试elevation和scale动画的目标值
  - 测试滑动操作的threshold计算
  - 测试骨架屏的shimmer动画

- [ ]* 14. 编写UI集成测试
  - 测试邮件卡片的点击和长按交互
  - 测试滑动操作（归档、删除）
  - 测试多选模式的进入和退出
  - 测试下拉刷新功能
  - 测试空状态和加载状态的显示

- [ ]* 15. 性能测试和优化
  - 测试大列表（1000+项）的渲染性能
  - 使用Profiler监控滚动帧率
  - 测试内存占用情况
  - 优化动画性能，确保无掉帧
  - 验证初始加载时间< 500ms
