# Fleur 邮箱应用需求文档

## 简介

Fleur 是一款现代化的邮箱客户端应用，使用 Kotlin 和 Jetpack Compose 开发，采用 Material 3 Extended (M3E) 设计语言。应用的核心理念是"优雅、美观、好用"，提供传统邮箱视图和类 Telegram 的聊天视图两种交互模式，支持 WebDAV 协议，具有玻璃拟态设计风格、柔和阴影和流畅动效，旨在提供比 Gmail 更清爽、更优雅的用户体验。

## 术语表

- **Fleur_App**: Fleur 邮箱客户端应用系统
- **传统视图**: 以列表形式展示邮件的经典邮箱界面模式
- **聊天视图**: 以对话气泡形式展示邮件往来的类 Telegram 界面模式
- **玻璃拟态**: 一种视觉设计风格，使用半透明背景、模糊效果和柔和阴影营造玻璃质感
- **M3E 组件**: Material 3 Extended 组件，包括 Navigation Drawer、Bottom Sheet、FAB 等
- **WebDAV**: 基于 HTTP 的文件管理协议，用于邮件同步
- **邮件线程**: 具有相同主题的一组相关邮件的集合
- **动效**: 界面元素的动画效果和过渡效果
- **柔和阴影**: 使用较大模糊半径和较低不透明度的阴影效果
- **用户**: 使用 Fleur 应用的人员
- **邮件账户**: 用户在 Fleur 中配置的电子邮件账户

## 需求

### 需求 1: 双视图模式

**用户故事:** 作为用户，我希望能够在传统邮箱视图和聊天视图之间切换，以便根据不同场景选择最舒适的阅读方式。

#### 验收标准

1. THE Fleur_App SHALL 提供传统视图模式，以列表形式展示邮件，每个邮件项显示发件人、主题、预览文本和时间戳
2. THE Fleur_App SHALL 提供聊天视图模式，以对话气泡形式展示邮件线程，发送的邮件显示在右侧，接收的邮件显示在左侧
3. WHEN 用户点击视图切换按钮时，THE Fleur_App SHALL 在 300 毫秒内完成视图切换，并播放流畅的过渡动画
4. THE Fleur_App SHALL 持久化用户的视图偏好设置，在应用重启后保持用户上次选择的视图模式
5. WHERE 用户处于聊天视图时，THE Fleur_App SHALL 自动将同一邮件线程的邮件分组显示

### 需求 2: Material 3 Extended 设计系统

**用户故事:** 作为用户，我希望应用具有现代化、优雅的视觉设计，以便获得愉悦的使用体验。

#### 验收标准

1. THE Fleur_App SHALL 使用 Material 3 Extended 设计规范，包括 Dynamic Color、Typography 和 Motion 系统
2. THE Fleur_App SHALL 提供浅色模式和深色模式，浅色模式使用米白色背景（#F5F5F0），深色模式使用深蓝黑色渐变背景（#0A0E1A → #0D1B2A）
3. THE Fleur_App SHALL 在浅色模式下应用玻璃拟态效果，卡片背景使用 20 像素模糊半径和 80% 不透明度的半透明白色
4. THE Fleur_App SHALL 使用柔和阴影，elevation 为 2dp 时模糊半径为 8 像素，elevation 为 4dp 时模糊半径为 12 像素
5. THE Fleur_App SHALL 支持 Android 12+ 的动态配色功能，从系统壁纸提取主题色

### 需求 3: M3E 导航组件

**用户故事:** 作为用户，我希望使用直观的导航方式访问不同功能，以便快速完成操作。

#### 验收标准

1. WHERE 设备屏幕宽度大于等于 600dp 时，THE Fleur_App SHALL 使用 Navigation Drawer 作为主导航方式
2. WHERE 设备屏幕宽度小于 600dp 时，THE Fleur_App SHALL 使用 Bottom Navigation Bar 作为主导航方式
3. THE Fleur_App SHALL 在 Navigation Drawer 中显示邮件文件夹（收件箱、已发送、草稿箱、垃圾箱）、账户列表和设置入口
4. WHEN 用户打开 Navigation Drawer 时，THE Fleur_App SHALL 在 250 毫秒内播放滑入动画，并应用半透明遮罩层
5. THE Fleur_App SHALL 使用 Modal Bottom Sheet 展示邮件操作选项（回复、转发、归档、删除、标记）

### 需求 4: 优雅的动效系统

**用户故事:** 作为用户，我希望界面交互具有流畅的动画效果，以便获得高品质的使用体验。

#### 验收标准

1. WHEN 用户点击邮件项时，THE Fleur_App SHALL 播放 200 毫秒的涟漪效果动画，并在 300 毫秒内完成页面过渡
2. WHEN 邮件列表加载新内容时，THE Fleur_App SHALL 使用淡入动画（fade in）和向上滑动动画（slide up），每个邮件项延迟 50 毫秒依次出现
3. WHEN 用户滑动删除或归档邮件时，THE Fleur_App SHALL 播放 400 毫秒的滑出动画，并在动画完成后更新列表
4. WHEN 用户悬停或按下卡片时，THE Fleur_App SHALL 在 150 毫秒内提升 elevation 至 6dp，并显示微妙的缩放效果（scale 1.02）
5. THE Fleur_App SHALL 使用 FastOutSlowIn 缓动曲线作为默认动画曲线，确保动画自然流畅

### 需求 5: WebDAV 协议支持

**用户故事:** 作为用户，我希望通过 WebDAV 协议同步邮件，以便使用自托管或支持 WebDAV 的邮件服务。

#### 验收标准

1. THE Fleur_App SHALL 支持通过 WebDAV 协议连接邮件服务器，使用 HTTPS 加密传输
2. WHEN 用户添加邮件账户时，THE Fleur_App SHALL 验证 WebDAV 服务器地址、端口、用户名和密码的有效性
3. THE Fleur_App SHALL 每 15 分钟自动同步邮件，增量获取新邮件和更新
4. IF WebDAV 连接失败时，THEN THE Fleur_App SHALL 使用指数退避策略重试，最多重试 3 次
5. THE Fleur_App SHALL 使用 Android Keystore 加密存储 WebDAV 账户凭证

### 需求 6: 邮件撰写与发送

**用户故事:** 作为用户，我希望能够撰写和发送邮件，支持富文本格式和附件，以便完成日常邮件沟通。

#### 验收标准

1. THE Fleur_App SHALL 提供邮件撰写界面，包含收件人、抄送、密送、主题和正文输入字段
2. THE Fleur_App SHALL 支持富文本编辑，包括粗体、斜体、下划线、有序列表和无序列表
3. THE Fleur_App SHALL 支持添加附件，单个附件大小不超过 25MB，总附件大小不超过 50MB
4. THE Fleur_App SHALL 每 30 秒自动保存草稿，或在用户停止输入 3 秒后保存草稿
5. WHEN 用户点击发送按钮时，THE Fleur_App SHALL 验证收件人地址格式，并在 5 秒内完成邮件发送或返回错误提示

### 需求 7: 邮件搜索功能

**用户故事:** 作为用户，我希望能够快速搜索邮件，以便找到需要的信息。

#### 验收标准

1. THE Fleur_App SHALL 提供全文搜索功能，搜索范围包括发件人、收件人、主题和正文
2. THE Fleur_App SHALL 在用户输入搜索关键词后 300 毫秒内返回搜索结果
3. THE Fleur_App SHALL 在搜索结果中高亮显示匹配的关键词
4. THE Fleur_App SHALL 支持搜索过滤器，包括日期范围、发件人、账户和是否有附件
5. THE Fleur_App SHALL 保存最近 10 条搜索历史，并在搜索界面显示

### 需求 8: 手势操作

**用户故事:** 作为用户，我希望通过手势快速操作邮件，以便提高效率。

#### 验收标准

1. WHEN 用户在邮件项上向右滑动超过 50% 宽度时，THE Fleur_App SHALL 归档该邮件
2. WHEN 用户在邮件项上向左滑动超过 50% 宽度时，THE Fleur_App SHALL 删除该邮件
3. THE Fleur_App SHALL 在滑动过程中显示操作图标和背景色提示，归档使用绿色背景，删除使用红色背景
4. THE Fleur_App SHALL 允许用户在设置中自定义左右滑动操作
5. WHEN 用户长按邮件项时，THE Fleur_App SHALL 进入多选模式，显示复选框和批量操作工具栏

### 需求 9: 多账户管理

**用户故事:** 作为用户，我希望能够管理多个邮件账户，以便在一个应用中处理所有邮件。

#### 验收标准

1. THE Fleur_App SHALL 支持添加多个邮件账户，每个账户使用不同的颜色标识
2. THE Fleur_App SHALL 在收件箱中显示所有账户的邮件，并在邮件项上显示账户颜色指示器
3. THE Fleur_App SHALL 允许用户按账户过滤邮件
4. THE Fleur_App SHALL 允许用户设置默认发件账户
5. WHEN 用户撰写邮件时，THE Fleur_App SHALL 允许用户通过 Bottom Sheet 选择发件账户

### 需求 10: 通知系统

**用户故事:** 作为用户，我希望收到新邮件通知，以便及时处理重要邮件。

#### 验收标准

1. WHEN 收到新邮件时，THE Fleur_App SHALL 在 2 秒内显示系统通知，包含发件人、主题和预览文本
2. THE Fleur_App SHALL 按账户分组通知，每个账户显示未读邮件数量
3. THE Fleur_App SHALL 在通知中提供快捷操作按钮，包括归档、删除和回复
4. WHEN 用户点击通知时，THE Fleur_App SHALL 直接打开对应的邮件详情页
5. THE Fleur_App SHALL 允许用户在设置中配置通知偏好，包括声音、震动和优先级

### 需求 11: 离线模式

**用户故事:** 作为用户，我希望在没有网络连接时仍能查看已缓存的邮件，以便随时访问重要信息。

#### 验收标准

1. THE Fleur_App SHALL 在本地数据库中缓存最近 30 天的邮件
2. WHEN 设备无网络连接时，THE Fleur_App SHALL 显示离线指示器，并仅展示缓存的邮件
3. THE Fleur_App SHALL 在离线模式下允许用户撰写邮件，并在网络恢复后自动发送
4. THE Fleur_App SHALL 在离线模式下将用户操作（删除、归档、标记）加入队列，在网络恢复后执行
5. WHEN 网络恢复时，THE Fleur_App SHALL 在 10 秒内自动同步邮件并执行队列中的操作

### 需求 12: 性能优化

**用户故事:** 作为用户，我希望应用响应迅速、流畅运行，以便获得高效的使用体验。

#### 验收标准

1. THE Fleur_App SHALL 在应用启动后 2 秒内显示收件箱界面
2. THE Fleur_App SHALL 使用分页加载，每页加载 50 封邮件，滚动到底部时自动加载下一页
3. THE Fleur_App SHALL 使用图片懒加载，仅加载可见区域的头像和附件缩略图
4. THE Fleur_App SHALL 在邮件列表中仅加载邮件预览（前 200 字符），在详情页加载完整正文
5. THE Fleur_App SHALL 保持 UI 线程帧率不低于 60 FPS，在中端设备（骁龙 778G 或同等性能）上流畅运行

### 需求 13: 可访问性

**用户故事:** 作为有视觉障碍的用户，我希望应用支持屏幕阅读器和高对比度模式，以便我也能使用该应用。

#### 验收标准

1. THE Fleur_App SHALL 为所有交互元素提供语义化的内容描述（content description）
2. THE Fleur_App SHALL 确保所有触摸目标的最小尺寸为 48dp × 48dp
3. THE Fleur_App SHALL 确保文本和背景的颜色对比度符合 WCAG 2.1 AA 级标准（至少 4.5:1）
4. THE Fleur_App SHALL 支持 Android TalkBack 屏幕阅读器，正确朗读界面元素
5. THE Fleur_App SHALL 支持系统字体缩放，在 200% 字体大小下仍保持界面可用

### 需求 14: 错误处理

**用户故事:** 作为用户，我希望在出现错误时能够获得清晰的提示和解决方案，以便快速恢复正常使用。

#### 验收标准

1. WHEN 网络请求失败时，THE Fleur_App SHALL 显示 Snackbar 提示，包含错误原因和重试按钮
2. WHEN 账户认证失败时，THE Fleur_App SHALL 显示 Alert Dialog，提示用户重新登录
3. WHEN 邮件发送失败时，THE Fleur_App SHALL 保存邮件到草稿箱，并显示失败原因
4. WHEN 存储空间不足时，THE Fleur_App SHALL 提示用户清理缓存或删除旧邮件
5. THE Fleur_App SHALL 记录错误日志到本地文件，供用户反馈问题时使用
