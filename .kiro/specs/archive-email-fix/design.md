# 设计文档

## 概述

本设计文档描述了如何修复邮件归档功能，使其能够正确地将邮件从收件箱移动到归档文件夹。当前实现的问题在于 `EmailRepositoryImpl.archiveEmail` 方法只调用了 `markAsRead`，没有更新邮件的标签（labels）字段。

修复方案将通过更新邮件实体的 `labels` 字段来实现文件夹移动，确保归档的邮件能够在归档文件夹中显示，并且不会在收件箱中重新出现。

## 架构

### 当前架构问题

```
用户滑动归档
    ↓
InboxViewModel.archiveEmail()
    ↓
ArchiveEmailUseCase.invoke()
    ↓
EmailRepository.archiveEmail()
    ↓
只调用 markAsRead() ❌ 问题所在
    ↓
邮件仍然保留 "inbox" 标签
    ↓
刷新后重新出现在收件箱
```

### 修复后的架构

```
用户滑动归档
    ↓
InboxViewModel.archiveEmail()
    ↓
ArchiveEmailUseCase.invoke()
    ↓
EmailRepository.archiveEmail()
    ↓
1. 获取邮件实体
2. 更新标签：移除 "inbox"，添加 "archive"
3. 标记为已读
4. 更新数据库
5. 同步到远程服务器（如果可用）
    ↓
邮件从收件箱消失，出现在归档文件夹
```

## 组件和接口

### 1. EmailDao 扩展

需要添加新的方法来更新邮件标签：

```kotlin
/**
 * 更新邮件标签
 * @param emailId 邮件ID
 * @param labels 新的标签字符串（逗号分隔）
 */
@Query("UPDATE emails SET labels = :labels WHERE id = :emailId")
suspend fun updateEmailLabels(emailId: String, labels: String)

/**
 * 批量更新邮件标签
 * @param updates 邮件ID到标签的映射
 */
@Transaction
suspend fun updateEmailLabels(updates: Map<String, String>) {
    updates.forEach { (emailId, labels) ->
        updateEmailLabels(emailId, labels)
    }
}
```

### 2. EmailRepository 接口更新

`archiveEmail` 方法的实现需要完全重写：

```kotlin
/**
 * 归档邮件
 * 将邮件从收件箱移动到归档文件夹
 * 
 * @param emailId 邮件ID
 * @return 操作结果
 */
override suspend fun archiveEmail(emailId: String): Result<Unit> {
    return try {
        // 1. 获取邮件实体
        val emailEntity = emailDao.getEmailById(emailId).first()
        
        if (emailEntity != null) {
            // 2. 更新标签
            val currentLabels = emailEntity.labels?.split(",")?.toMutableList() ?: mutableListOf()
            currentLabels.remove("inbox")
            if (!currentLabels.contains("archive")) {
                currentLabels.add("archive")
            }
            
            // 3. 更新邮件实体
            val updatedEntity = emailEntity.copy(
                labels = currentLabels.joinToString(","),
                isRead = true  // 归档时标记为已读
            )
            
            // 4. 更新本地数据库
            emailDao.updateEmail(updatedEntity)
            
            // 5. 尝试同步到远程服务器
            try {
                webdavClient.updateEmailLabels(emailId, currentLabels)
            } catch (e: IllegalArgumentException) {
                // WebDAV 未连接，仅在本地更新
                Log.w(TAG, "WebDAV 未连接，仅在本地归档邮件")
            }
            
            Result.success(Unit)
        } else {
            Result.failure(FleurError.NotFoundError("邮件不存在"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "归档邮件失败: ${e.message}", e)
        Result.failure(FleurError.DatabaseError(e.message ?: "归档失败"))
    }
}
```

### 3. 批量归档优化

`archiveEmails` 方法也需要优化，避免逐个调用：

```kotlin
/**
 * 批量归档邮件
 * 
 * @param emailIds 邮件ID列表
 * @return 操作结果，包含成功和失败的数量
 */
override suspend fun archiveEmails(emailIds: List<String>): Result<Unit> {
    return try {
        val updates = mutableMapOf<String, String>()
        val failedIds = mutableListOf<String>()
        
        // 批量处理标签更新
        emailIds.forEach { emailId ->
            try {
                val emailEntity = emailDao.getEmailById(emailId).first()
                if (emailEntity != null) {
                    val currentLabels = emailEntity.labels?.split(",")?.toMutableList() ?: mutableListOf()
                    currentLabels.remove("inbox")
                    if (!currentLabels.contains("archive")) {
                        currentLabels.add("archive")
                    }
                    updates[emailId] = currentLabels.joinToString(",")
                } else {
                    failedIds.add(emailId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理邮件 $emailId 失败: ${e.message}")
                failedIds.add(emailId)
            }
        }
        
        // 批量更新数据库
        if (updates.isNotEmpty()) {
            emailDao.updateEmailLabels(updates)
            
            // 批量标记为已读
            emailDao.markEmailsAsRead(updates.keys.toList(), true)
        }
        
        // 尝试同步到远程服务器
        try {
            updates.forEach { (emailId, labels) ->
                webdavClient.updateEmailLabels(emailId, labels.split(","))
            }
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "WebDAV 未连接，仅在本地批量归档邮件")
        }
        
        if (failedIds.isEmpty()) {
            Result.success(Unit)
        } else {
            Log.w(TAG, "批量归档完成，失败数量: ${failedIds.size}")
            Result.success(Unit)  // 部分成功也返回成功
        }
    } catch (e: Exception) {
        Log.e(TAG, "批量归档邮件失败: ${e.message}", e)
        Result.failure(FleurError.DatabaseError(e.message ?: "批量归档失败"))
    }
}
```

## 数据模型

### 标签管理规则

邮件的 `labels` 字段是一个逗号分隔的字符串，包含以下可能的值：

- `inbox`: 收件箱
- `sent`: 已发送
- `drafts`: 草稿
- `archive`: 归档
- `trash`: 垃圾箱
- `spam`: 垃圾邮件

**规则：**
1. 一封邮件可以有多个标签（例如：`inbox,important`）
2. `inbox`、`archive`、`trash` 是互斥的（一封邮件只能在一个主文件夹中）
3. 归档操作：移除 `inbox`，添加 `archive`
4. 删除操作：移除所有标签，添加 `trash`
5. 恢复操作：移除 `trash`，添加 `inbox`

### 标签更新示例

```kotlin
// 原始标签
labels = "inbox,important"

// 归档后
labels = "archive,important"

// 删除后
labels = "trash"

// 从垃圾箱恢复
labels = "inbox"
```

## 错误处理

### 1. 邮件不存在

```kotlin
if (emailEntity == null) {
    return Result.failure(FleurError.NotFoundError("邮件不存在"))
}
```

### 2. 数据库更新失败

```kotlin
try {
    emailDao.updateEmail(updatedEntity)
} catch (e: Exception) {
    Log.e(TAG, "更新数据库失败: ${e.message}", e)
    return Result.failure(FleurError.DatabaseError("更新失败"))
}
```

### 3. 远程同步失败

```kotlin
try {
    webdavClient.updateEmailLabels(emailId, currentLabels)
} catch (e: IllegalArgumentException) {
    // WebDAV 未连接，仅在本地更新
    Log.w(TAG, "WebDAV 未连接，仅在本地归档邮件")
} catch (e: Exception) {
    // 其他错误，记录但不影响本地操作
    Log.e(TAG, "远程同步失败: ${e.message}", e)
}
```

### 4. 批量操作部分失败

批量操作时，即使部分邮件失败，也应该继续处理其他邮件，并记录失败的邮件ID：

```kotlin
val failedIds = mutableListOf<String>()
emailIds.forEach { emailId ->
    try {
        // 处理邮件
    } catch (e: Exception) {
        failedIds.add(emailId)
    }
}

if (failedIds.isNotEmpty()) {
    Log.w(TAG, "部分邮件归档失败: $failedIds")
}
```

## 测试策略

### 1. 单元测试

测试 `EmailRepositoryImpl.archiveEmail` 方法：

- 测试归档单封邮件
- 测试归档不存在的邮件
- 测试标签更新逻辑
- 测试数据库更新失败的情况
- 测试远程同步失败的情况

### 2. 集成测试

测试完整的归档流程：

- 测试从收件箱归档邮件
- 测试归档后邮件在归档文件夹中显示
- 测试归档后邮件不在收件箱中显示
- 测试批量归档多封邮件
- 测试归档后刷新收件箱

### 3. UI 测试

测试用户交互：

- 测试滑动归档手势
- 测试归档动画
- 测试归档后的 UI 更新
- 测试归档失败时的错误提示

## 性能考虑

### 1. 批量操作优化

批量归档时，使用事务来减少数据库操作次数：

```kotlin
@Transaction
suspend fun updateEmailLabels(updates: Map<String, String>) {
    updates.forEach { (emailId, labels) ->
        updateEmailLabels(emailId, labels)
    }
}
```

### 2. 异步处理

归档操作应该在后台线程执行，避免阻塞 UI：

```kotlin
viewModelScope.launch {
    withContext(Dispatchers.IO) {
        archiveEmailUseCase(emailId)
    }
}
```

### 3. 缓存更新

归档后立即更新 ViewModel 的状态，避免等待数据库查询：

```kotlin
fun archiveEmail(emailId: String) {
    viewModelScope.launch {
        // 立即从 UI 列表中移除
        _uiState.update { currentState ->
            currentState.copy(
                emails = currentState.emails.filter { it.id != emailId }
            )
        }
        
        // 后台执行归档操作
        val result = archiveEmailUseCase(emailId)
        if (result.isFailure) {
            // 如果失败，恢复邮件到列表
            loadEmails()
        }
    }
}
```

## 向后兼容性

### 1. 数据迁移

对于现有的邮件数据，可能需要添加默认标签：

```kotlin
// 数据库迁移脚本
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 为没有标签的邮件添加默认 inbox 标签
        database.execSQL("""
            UPDATE emails 
            SET labels = 'inbox' 
            WHERE labels IS NULL OR labels = ''
        """)
    }
}
```

### 2. 标签格式兼容

确保标签字符串的格式一致：

```kotlin
// 标准化标签格式
fun normalizeLabels(labels: String?): String {
    return labels
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.distinct()
        ?.joinToString(",")
        ?: ""
}
```

## 日志和监控

### 1. 操作日志

记录所有归档操作：

```kotlin
Log.d(TAG, "归档邮件: emailId=$emailId")
Log.d(TAG, "标签更新: $oldLabels -> $newLabels")
Log.d(TAG, "归档成功: emailId=$emailId")
```

### 2. 错误日志

记录所有错误：

```kotlin
Log.e(TAG, "归档邮件失败: emailId=$emailId, error=${e.message}", e)
```

### 3. 性能监控

记录批量操作的性能：

```kotlin
val startTime = System.currentTimeMillis()
// 执行批量归档
val duration = System.currentTimeMillis() - startTime
Log.d(TAG, "批量归档 ${emailIds.size} 封邮件，耗时: ${duration}ms")
```
