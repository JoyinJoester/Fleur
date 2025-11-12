# Implementation Plan

- [x] 1. 完全重写 FleurBottomNavigationBar 组件


  - 删除现有的所有代码
  - 实现新的双层结构（Column + Box）
  - 正确处理 navigationBarsPadding()
  - 应用圆角和背景色
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4, 6.1, 6.2, 6.3, 6.4_


- [ ] 2. 实现 NavigationItem 组件
  - 创建单个导航项的布局结构
  - 实现图标和标签的垂直居中布局
  - 应用正确的尺寸和间距

  - _Requirements: 3.1, 3.2, 3.3, 3.4, 5.4_

- [ ] 3. 实现选中状态的视觉效果
  - 实现图标切换逻辑（filled/outlined）
  - 实现颜色状态切换

  - 实现背景色状态切换
  - _Requirements: 4.1, 4.2, 4.4_

- [ ] 4. 添加动画效果
  - 实现图标缩放动画
  - 实现颜色过渡动画

  - 实现背景色过渡动画
  - 统一动画时长为 250ms
  - _Requirements: 4.3, 4.5_

- [x] 5. 配置四个导航项


  - 定义 BottomNavItem 数据类
  - 配置 Inbox、Chat、Contacts、Calendar 四个项
  - 设置正确的图标和标签
  - _Requirements: 5.1, 5.2, 5.3, 5.5_

- [ ] 6. 验证和测试
  - 在设备上验证系统导航栏区域无间隙
  - 验证圆角显示正确
  - 验证图标位置居中
  - 验证动画流畅性
  - _Requirements: 1.4, 2.4, 3.3, 4.3_
