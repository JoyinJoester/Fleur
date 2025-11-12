# 实现计划

- [x] 1. 扩展 EmailDao 添加标签更新方法





  - 添加 `updateEmailLabels(emailId: String, labels: String)` 方法用于更新单个邮件的标签
  - 添加 `updateEmailLabels(updates: Map<String, String>)` 方法用于批量更新邮件标签，使用 @Transaction 注解确保原子性
  - _需求: 1.1, 2.1_

- [x] 2. 重写 EmailRepository.archiveEmail 方法


  - 使用 `emailDao.getEmailById()` 获取邮件实体
  - 解析当前标签字符串，移除 "inbox" 标签
  - 添加 "archive" 标签（如果不存在）
  - 创建更新后的邮件实体，设置 `isRead = true`
  - 调用 `emailDao.updateEmail()` 更新数据库
  - 尝试调用 `webdavClient.updateEmailLabels()` 同步到远程服务器，捕获异常并记录日志
  - 返回 `Result.success(Unit)` 或 `Result.failure()`
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 3.2, 3.3_

- [x] 3. 优化 EmailRepository.archiveEmails 批量归档方法



  - 遍历邮件ID列表，获取每个邮件实体
  - 为每个邮件更新标签（移除 "inbox"，添加 "archive"）
  - 收集所有更新到 Map<String, String>
  - 使用 `emailDao.updateEmailLabels(updates)` 批量更新标签
  - 使用 `emailDao.markEmailsAsRead()` 批量标记为已读
  - 尝试同步到远程服务器，记录失败的邮件ID
  - 记录成功和失败的数量
  - _需求: 2.1, 2.2, 2.3, 2.4, 3.1, 3.2_

- [ ] 4. 验证归档功能的完整性
  - 在测试环境中归档一封邮件
  - 验证邮件从收件箱消失
  - 导航到归档文件夹，验证邮件出现在归档列表中
  - 刷新收件箱，验证邮件不会重新出现
  - 测试批量归档多封邮件
  - 验证归档后的邮件详情可以正常查看
  - _需求: 1.2, 1.3, 1.4, 2.2, 4.1, 4.2_

- [ ]* 5. 添加数据迁移脚本（可选）
  - 创建数据库迁移类，为现有邮件添加默认 "inbox" 标签
  - 在 FleurDatabase 中注册迁移
  - _需求: 向后兼容性_
