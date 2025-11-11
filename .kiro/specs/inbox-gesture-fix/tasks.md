# Implementation Plan

- [x] 1. 修改 EmailListItem 组件以修复手势冲突





  - 移除 Card 组件的 onClick 参数
  - 在 modifier 中添加 combinedClickable 来处理点击和长按手势
  - 添加触觉反馈支持（长按时）
  - 添加调试日志以便追踪手势事件
  - 确保 Modifier 的顺序正确（布局 → 手势 → 其他）
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 4.1, 5.1_

- [x] 2. 验证 SwipeableEmailItem 的手势处理



  - 确认 SwipeToDismissBox 不会阻止点击和长按事件传递
  - 验证滑动手势与点击手势能够正确区分
  - 确保触觉反馈在滑动达到阈值时触发
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 4.2_

- [ ] 3. 测试手势操作功能
  - 测试点击邮件卡片是否能够导航到详情页面
  - 测试长按邮件卡片是否能够进入多选模式
  - 测试左右滑动邮件卡片是否能够触发快捷操作
  - 测试垂直滚动是否流畅且不会误触发其他手势
  - 测试多选模式下点击是否切换选中状态
  - _Requirements: 1.1, 1.3, 2.1, 2.3, 3.1, 3.2, 4.3_

- [ ]* 4. 性能优化和调试
  - 使用 Android Studio Profiler 监控手势处理性能
  - 验证手势操作期间帧率保持在 60fps
  - 验证手势响应延迟小于 100ms
  - 如果问题仍然存在，考虑使用 pointerInput 手动处理手势
  - _Requirements: 4.4, 5.1, 5.2, 5.3, 5.4_
