# 需求文档

## 简介

修复邮件归档功能，确保归档操作能够正确地将邮件从收件箱移动到归档文件夹，并在数据库中持久化。当前实现只是标记邮件为已读，导致归档后的邮件在刷新时重新出现在收件箱中。

## 术语表

- **Email System**: Fleur 邮件应用系统
- **Archive Folder**: 归档文件夹，用于存储已归档的邮件
- **Inbox Folder**: 收件箱文件夹，用于存储新收到的邮件
- **Email Entity**: 数据库中的邮件实体对象
- **Label**: 邮件标签，用于标识邮件所属的文件夹（如 inbox、archive、trash 等）

## 需求

### 需求 1

**用户故事：** 作为邮件应用用户，我希望滑动归档邮件后，邮件能够从收件箱消失并进入归档文件夹，这样我可以保持收件箱整洁。

#### 验收标准

1. WHEN 用户在收件箱中滑动归档一封邮件，THE Email System SHALL 将该邮件的标签从 "inbox" 更新为 "archive"
2. WHEN 用户在收件箱中滑动归档一封邮件，THE Email System SHALL 从收件箱列表中移除该邮件
3. WHEN 用户归档邮件后导航到归档文件夹，THE Email System SHALL 在归档文件夹中显示该邮件
4. WHEN 用户归档邮件后刷新收件箱，THE Email System SHALL 确保该邮件不会重新出现在收件箱中
5. WHEN 归档操作失败，THE Email System SHALL 保持邮件在收件箱中并显示错误提示

### 需求 2

**用户故事：** 作为邮件应用用户，我希望批量归档多封邮件时，所有选中的邮件都能正确进入归档文件夹，这样我可以高效地管理邮件。

#### 验收标准

1. WHEN 用户批量选择多封邮件并执行归档操作，THE Email System SHALL 将所有选中邮件的标签从 "inbox" 更新为 "archive"
2. WHEN 批量归档操作完成，THE Email System SHALL 从收件箱列表中移除所有已归档的邮件
3. WHEN 批量归档过程中某封邮件归档失败，THE Email System SHALL 继续处理其他邮件并记录失败的邮件ID
4. WHEN 批量归档操作完成，THE Email System SHALL 显示归档成功的邮件数量和失败的邮件数量

### 需求 3

**用户故事：** 作为邮件应用用户，我希望归档操作能够同步到远程服务器，这样我在其他设备上也能看到归档的邮件。

#### 验收标准

1. WHEN 用户归档邮件且网络连接可用，THE Email System SHALL 将归档操作同步到 WebDAV 服务器
2. WHEN 用户归档邮件但网络连接不可用，THE Email System SHALL 在本地数据库中记录归档操作并在网络恢复后同步
3. WHEN 远程同步失败，THE Email System SHALL 保持本地归档状态并记录同步失败日志
4. WHEN 远程同步成功，THE Email System SHALL 更新本地数据库中的同步状态

### 需求 4

**用户故事：** 作为邮件应用用户，我希望能够从归档文件夹中查看和管理已归档的邮件，这样我可以在需要时找回重要邮件。

#### 验收标准

1. WHEN 用户导航到归档文件夹，THE Email System SHALL 显示所有标签为 "archive" 的邮件
2. WHEN 用户在归档文件夹中点击邮件，THE Email System SHALL 显示邮件详情
3. WHEN 用户在归档文件夹中搜索邮件，THE Email System SHALL 仅在归档邮件中搜索
4. WHEN 归档文件夹为空，THE Email System SHALL 显示空状态提示
