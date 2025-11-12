# 本地优先架构重构需求文档

## 简介

将 Fleur 邮件应用从当前的"远程优先"架构重构为"本地优先"（Local-First）架构。WebDAV 同步将成为可选的备份功能，而不是核心依赖。所有用户操作应该立即在本地生效，然后在后台异步同步到 WebDAV（如果用户启用了该功能）。

## 术语表

- **Local-First Architecture（本地优先架构）**：一种软件架构模式，优先使用本地数据存储，所有操作立即在本地生效，远程同步作为可选的后台任务
- **WebDAV Client（WebDAV 客户端）**：用于与 WebDAV 服务器通信的组件
- **Email Repository（邮件仓库）**：管理邮件数据访问的领域层组件
- **Sync Queue（同步队列）**：存储待同步操作的本地队列
- **Optimistic UI（乐观 UI）**：立即更新 UI，假设操作会成功，失败时再回滚的策略

## 需求

### 需求 1：本地优先的数据操作

**用户故事：** 作为用户，我希望所有邮件操作（标记已读/未读、归档、删除、星标等）立即生效，不受网络状态影响，这样我可以流畅地使用应用。

#### 验收标准

1. WHEN 用户执行任何邮件操作（标记已读、归档、删除等），THE Email Repository SHALL 立即更新本地数据库
2. WHEN 本地数据库更新完成，THE Email Repository SHALL 立即返回成功结果给调用者
3. WHEN 本地操作成功后，THE Email Repository SHALL 将操作添加到同步队列以便后台同步
4. WHEN 用户在离线状态下操作邮件，THE System SHALL 正常执行操作并在本地生效
5. WHEN 网络恢复后，THE System SHALL 自动同步所有待处理的操作到 WebDAV

### 需求 2：WebDAV 作为可选备份

**用户故事：** 作为用户，我希望 WebDAV 同步是可选的，这样不使用 WebDAV 的用户也能完整使用应用的所有功能。

#### 验收标准

1. WHEN 用户未配置 WebDAV，THE System SHALL 正常运行所有邮件功能
2. WHEN 用户配置了 WebDAV，THE System SHALL 在后台自动同步邮件数据
3. WHEN WebDAV 同步失败，THE System SHALL 保留本地数据并记录失败原因
4. WHEN 用户禁用 WebDAV，THE System SHALL 停止同步但保留本地数据
5. WHERE WebDAV 已配置，THE System SHALL 在设置中显示同步状态和最后同步时间

### 需求 3：同步队列管理

**用户故事：** 作为开发者，我需要一个可靠的同步队列系统，确保所有本地操作最终都能同步到 WebDAV。

#### 验收标准

1. WHEN 邮件操作完成，THE Sync Queue SHALL 记录操作类型、邮件 ID 和时间戳
2. WHEN 网络可用且 WebDAV 已配置，THE Sync Queue SHALL 按顺序处理待同步操作
3. IF 同步操作失败，THEN THE Sync Queue SHALL 使用指数退避策略重试
4. WHEN 同步操作成功，THE Sync Queue SHALL 从队列中移除该操作
5. WHEN 同步队列中有待处理操作，THE System SHALL 在 UI 中显示待同步数量

### 需求 4：数据一致性保证

**用户故事：** 作为用户，我希望在多设备使用时，数据能够正确同步，不会出现冲突或数据丢失。

#### 验收标准

1. WHEN 从 WebDAV 拉取数据时，THE System SHALL 使用时间戳比较本地和远程数据
2. IF 远程数据更新，THEN THE System SHALL 更新本地数据库
3. IF 本地数据更新，THEN THE System SHALL 保留本地数据并标记为待同步
4. WHEN 检测到冲突时，THE System SHALL 优先使用最新时间戳的数据
5. WHEN 同步完成后，THE System SHALL 更新 UI 以反映最新状态

### 需求 5：错误处理和恢复

**用户故事：** 作为用户，我希望即使 WebDAV 同步出现问题，也不会影响我的正常使用。

#### 验收标准

1. WHEN WebDAV 同步失败，THE System SHALL 在通知栏显示错误信息
2. WHEN 同步失败次数超过阈值，THE System SHALL 暂停自动同步并通知用户
3. WHEN 用户手动触发同步，THE System SHALL 重新尝试所有待同步操作
4. IF WebDAV 服务器不可用，THEN THE System SHALL 继续使用本地数据
5. WHEN 同步错误解决后，THE System SHALL 自动恢复同步

### 需求 6：性能优化

**用户故事：** 作为用户，我希望应用响应迅速，不会因为同步操作而卡顿。

#### 验收标准

1. WHEN 用户执行邮件操作，THE System SHALL 在 200ms 内更新 UI
2. WHEN 后台同步运行时，THE System SHALL 不影响 UI 响应速度
3. WHEN 同步大量数据时，THE System SHALL 使用批处理以提高效率
4. WHEN 应用启动时，THE System SHALL 优先加载本地数据
5. WHEN 后台同步完成后，THE System SHALL 静默更新数据而不打断用户操作

### 需求 7：向后兼容

**用户故事：** 作为现有用户，我希望升级后我的数据和设置都能保留。

#### 验收标准

1. WHEN 应用升级后首次启动，THE System SHALL 保留所有本地邮件数据
2. WHEN 用户已配置 WebDAV，THE System SHALL 保留 WebDAV 配置
3. WHEN 迁移完成后，THE System SHALL 使用新的本地优先架构
4. IF 迁移失败，THEN THE System SHALL 回滚到旧版本并通知用户
5. WHEN 迁移成功后，THE System SHALL 记录迁移日志以便调试
