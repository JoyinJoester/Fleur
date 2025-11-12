# 实现计划

- [x] 1. 创建未读指示器组件





  - 创建UnreadIndicator可组合函数，显示4dp宽的蓝色指示条
  - 实现左侧圆角效果（topStart和bottomStart为2dp）
  - 添加淡入淡出动画（200ms持续时间）
  - _需求: 1.4_

- [x] 2. 创建时间和星标列组件


  - 创建TimeAndStarColumn可组合函数，垂直排列时间戳和星标图标
  - 实现时间戳显示（labelSmall样式）
  - 实现星标图标（16dp尺寸，primary颜色）
  - 添加AnimatedVisibility包装星标图标
  - 实现淡入淡出+缩放动画（200ms，FastOutSlowIn缓动）
  - 添加2dp的垂直间距
  - _需求: 2.1, 2.2, 2.3, 3.4_


- [x] 3. 重构EmailListItem组件布局

  - 修改Row布局，在最左侧添加UnreadIndicator（条件显示）
  - 调整头像左侧间距（未读时8dp，已读时12dp）
  - 修改第一行布局，将时间戳替换为TimeAndStarColumn
  - 移除原有的星标图标显示逻辑
  - 确保卡片固定高度88dp
  - 添加右侧8dp间距
  - _需求: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 2.5, 4.1, 4.2, 4.3_


- [x] 4. 优化字体样式以区分已读/未读

  - 修改发件人文本样式，未读时使用FontWeight.Bold，已读时使用FontWeight.Normal
  - 修改主题文本样式，未读时使用FontWeight.SemiBold，已读时使用FontWeight.Normal
  - 确保字体变化不影响布局尺寸
  - _需求: 1.1, 1.2, 1.3_

- [x] 5. 实现滚动时禁用动画优化


  - 在EmailListView和EmailChatView中检测滚动状态
  - 使用derivedStateOf创建isScrolling状态
  - 将isScrolling参数传递给EmailListItem
  - 在TimeAndStarColumn中根据isScrolling条件显示星标
  - _需求: 4.4, 5.1_

- [x] 6. 优化InboxViewModel的状态更新逻辑


  - 修改toggleStar方法，实现乐观UI更新
  - 添加失败回滚逻辑
  - 确保状态更新在200ms内完成
  - 验证邮件列表去重功能正常工作
  - _需求: 3.1, 3.2, 3.3_

- [x] 7. 确保状态在所有视图中同步



  - 验证FolderScreenTemplate中的邮件列表使用相同的EmailListItem组件
  - 确保StarredScreen、SentScreen等文件夹视图正确显示星标状态
  - 测试从详情页返回后列表状态更新
  - _需求: 3.5_

- [ ]* 8. 添加可访问性支持
  - 为EmailListItem添加semantics修饰符
  - 实现contentDescription，包含已读/未读、发件人、主题、星标状态
  - 添加customActions支持标星、归档、删除操作
  - 为UnreadIndicator添加语义标记
  - 为星标图标添加contentDescription
  - _需求: 5.2, 5.3_

- [ ]* 9. 验证颜色对比度
  - 检查未读指示条与背景的对比度（≥ 3:1）
  - 检查文本与背景的对比度（≥ 4.5:1）
  - 检查星标图标与背景的对比度（≥ 3:1）
  - 在浅色和深色主题下分别验证
  - _需求: 5.4_

- [ ]* 10. 性能优化和测试
  - 为星标图标添加graphicsLayer启用硬件加速
  - 测试在低端设备上的动画性能
  - 验证列表滚动保持60fps
  - 测试大量邮件（100+）的渲染性能
  - _需求: 4.5, 5.1, 5.5_
