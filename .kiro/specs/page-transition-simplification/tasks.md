# Implementation Plan

- [x] 1. 修改页面过渡动画函数




  - 在 `app/src/main/java/takagi/ru/fleur/ui/theme/Animation.kt` 中修改 `pageEnterAnimation()` 函数
  - 移除 `slideInHorizontally` 组件，仅保留 `fadeIn` 动画
  - 保持现有的动画时长 (300ms) 和缓动曲线 (FastOutSlowIn)
  - _Requirements: 1.1, 1.3, 1.4_

- [x] 2. 修改页面退出动画函数


  - 在 `app/src/main/java/takagi/ru/fleur/ui/theme/Animation.kt` 中修改 `pageExitAnimation()` 函数
  - 移除 `slideOutHorizontally` 组件，仅保留 `fadeOut` 动画
  - 保持现有的动画时长 (300ms) 和缓动曲线 (FastOutSlowIn)
  - _Requirements: 1.2, 1.3, 1.4_

- [x] 3. 验证动画效果



  - 测试从收件箱到各个文件夹页面的导航，确认页面使用淡入效果
  - 测试返回导航，确认页面使用淡出效果
  - 验证所有页面切换都使用一致的淡入淡出效果
  - 确认列表项动画和其他 UI 动画未受影响
  - _Requirements: 1.1, 1.2, 2.1, 2.4_
