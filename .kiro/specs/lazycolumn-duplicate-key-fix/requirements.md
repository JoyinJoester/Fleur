# Requirements Document

## Introduction

应用在滚动邮件列表时崩溃，错误信息显示LazyColumn中存在重复的key。这是一个严重的bug，导致应用无法正常使用。需要确保邮件列表中的每个邮件都有唯一的ID，并且LazyColumn正确使用这些唯一ID作为key。

## Glossary

- **LazyColumn**: Jetpack Compose的懒加载列表组件，要求每个item有唯一的key
- **Email ID**: 邮件的唯一标识符，用于区分不同的邮件
- **Duplicate Key**: 重复的key，当LazyColumn中多个item使用相同的key时会导致崩溃
- **EmailEntity**: 数据库中的邮件实体，包含邮件的所有信息
- **Test Data Generator**: 测试数据生成器，用于生成测试邮件

## Requirements

### Requirement 1

**User Story:** 作为用户，我希望应用在滚动邮件列表时不会崩溃，以便正常浏览邮件

#### Acceptance Criteria

1. WHEN 用户滚动邮件列表, THE EmailListView SHALL 不抛出IllegalArgumentException异常
2. THE EmailListView SHALL 确保LazyColumn中每个item的key都是唯一的
3. THE EmailListView SHALL 在邮件列表为空时正常显示空状态

### Requirement 2

**User Story:** 作为开发者，我需要确保数据库中的邮件ID是唯一的，以便避免数据重复

#### Acceptance Criteria

1. THE EmailEntity SHALL 使用唯一的ID作为主键
2. THE EmailDao SHALL 在插入邮件时检测并处理ID冲突
3. THE EmailDao SHALL 使用REPLACE策略处理重复的邮件ID
4. THE EmailRepository SHALL 在返回邮件列表前去除重复的邮件

### Requirement 3

**User Story:** 作为开发者，我需要测试数据生成器生成唯一的邮件ID，以便测试时不会出现重复数据

#### Acceptance Criteria

1. THE TestEmailGenerator SHALL 为每封测试邮件生成唯一的ID
2. THE TestEmailGenerator SHALL 使用时间戳和随机数组合确保ID唯一性
3. THE TestEmailGenerator SHALL 在生成多封邮件时避免ID冲突
4. THE TestDataInserter SHALL 在插入测试数据前清除旧的测试数据

### Requirement 4

**User Story:** 作为开发者，我需要在UI层添加防御性编程，以便即使数据有问题也不会崩溃

#### Acceptance Criteria

1. THE EmailListView SHALL 在渲染前对邮件列表进行去重处理
2. THE EmailListView SHALL 使用distinctBy确保每个邮件ID只出现一次
3. THE EmailListView SHALL 记录警告日志当检测到重复邮件时
4. THE InboxViewModel SHALL 在更新UI状态前对邮件列表进行去重

### Requirement 5

**User Story:** 作为开发者，我需要添加诊断日志，以便快速定位重复数据的来源

#### Acceptance Criteria

1. THE EmailListView SHALL 在渲染邮件列表时记录邮件ID和数量
2. THE InboxViewModel SHALL 在加载邮件时记录数据源信息
3. THE TestDataInserter SHALL 在插入测试数据时记录插入的邮件ID
4. THE System SHALL 在检测到重复ID时记录详细的错误信息
