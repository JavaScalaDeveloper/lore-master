# 通学万卷 - 用户系统数据库设计

## 📋 概述

本文档描述了通学万卷项目的用户系统数据库设计，支持多种登录方式、账号绑定、会话管理等功能。

## 🎯 设计目标

- **🔐 安全性**：用户ID不连续，防止数据被遍历爬取
- **🔗 多登录方式**：支持用户名、邮箱、手机、微信、QQ等多种登录方式
- **🔄 账号绑定**：同一用户可绑定多种登录方式，统一身份管理
- **📱 多设备支持**：支持用户在多个设备同时登录
- **📊 可追溯性**：完整的用户操作日志记录
- **🚀 可扩展性**：使用JSON字段支持未来功能扩展

## 📊 表结构设计

### 1. users - 用户基础信息表

用户系统的核心表，存储用户的基本信息。

**核心字段：**
- `user_id`：用户唯一ID（对外展示，类似QQ号）
- `nickname`：用户昵称
- `current_level`：当前等级
- `total_score`：总积分
- `status`：用户状态（1正常 0禁用 2待激活）

**特点：**
- 使用自定义用户ID，防止数据被遍历
- 包含学习相关字段（等级、积分、学习天数）
- 支持JSON扩展字段

### 2. user_auth_methods - 用户登录方式表

存储用户的各种登录方式，支持账号绑定。

**核心字段：**
- `auth_type`：认证类型（username、email、phone、wechat、qq等）
- `auth_key`：认证标识（用户名、邮箱、手机号、第三方openid等）
- `auth_secret`：认证密钥（密码hash、token等）
- `is_primary`：是否为主要登录方式

**特点：**
- 一个用户可以有多种登录方式
- 支持第三方登录（微信、QQ等）
- 独立的验证状态管理

### 3. user_sessions - 用户会话表

管理用户的登录会话，支持多设备登录。

**核心字段：**
- `session_id`：会话唯一标识
- `device_type`：设备类型（web、ios、android等）
- `login_ip`：登录IP地址
- `expire_time`：会话过期时间

**特点：**
- 支持多设备同时登录
- 记录详细的登录信息
- 自动会话过期管理

### 4. user_operation_logs - 用户操作日志表

记录用户的重要操作，用于安全审计和问题排查。

**核心字段：**
- `operation_type`：操作类型（login、logout、register等）
- `request_ip`：请求IP地址
- `result_status`：操作结果（1成功 0失败）

**特点：**
- 完整的操作记录
- 支持失败操作记录
- 便于安全审计

## 🔧 核心功能

### 用户ID生成策略

使用自定义函数 `generate_user_id()` 生成唯一用户ID：

```sql
-- 生成格式：年月日时分秒(10位) + 随机数(3位) + 校验位(2位)
-- 示例：25072610450012345
SELECT generate_user_id();
```

**优势：**
- 不连续，难以被遍历
- 包含时间信息，便于排序
- 15位长度，类似QQ号体验

### 多登录方式支持

支持的登录方式：
- `username`：用户名密码
- `email`：邮箱验证码/密码
- `phone`：手机号验证码
- `wechat`：微信登录
- `qq`：QQ登录
- `github`：GitHub登录

### 账号绑定流程

1. 用户通过任意方式登录（如微信）
2. 系统创建用户记录和对应的登录方式记录
3. 用户可以绑定其他登录方式（如邮箱）
4. 所有登录方式都关联到同一个用户ID

## 📝 使用示例

### 1. 用户注册（用户名密码）

```sql
-- 创建用户
INSERT INTO users (user_id, nickname, status) 
VALUES (generate_user_id(), '新用户', 1);

-- 添加登录方式
INSERT INTO user_auth_methods (user_id, auth_type, auth_key, auth_secret, is_primary, is_verified)
VALUES ('25072610450001', 'username', 'testuser', '$2a$10$...', 1, 1);
```

### 2. 微信登录后绑定邮箱

```sql
-- 微信登录创建用户
INSERT INTO users (user_id, nickname, avatar_url) 
VALUES (generate_user_id(), '微信用户', 'https://...');

-- 添加微信登录方式
INSERT INTO user_auth_methods (user_id, auth_type, auth_key, third_party_id, is_primary)
VALUES ('25072610450002', 'wechat', 'wx_openid_123', 'openid123', 1);

-- 后续绑定邮箱
INSERT INTO user_auth_methods (user_id, auth_type, auth_key, is_verified)
VALUES ('25072610450002', 'email', 'user@example.com', 1);
```

### 3. 用户登录会话管理

```sql
-- 创建登录会话
INSERT INTO user_sessions (user_id, session_id, auth_type, device_type, login_ip)
VALUES ('25072610450001', 'sess_123456', 'username', 'web', '192.168.1.100');

-- 记录操作日志
INSERT INTO user_operation_logs (user_id, operation_type, operation_desc, request_ip, result_status)
VALUES ('25072610450001', 'login', '用户登录', '192.168.1.100', 1);
```

## 🚀 扩展性设计

### JSON扩展字段

所有表都包含 `extra_info` JSON字段，支持未来功能扩展：

```sql
-- 用户扩展信息示例
UPDATE users SET extra_info = JSON_OBJECT(
    'preferences', JSON_OBJECT('theme', 'dark', 'language', 'zh-CN'),
    'social', JSON_OBJECT('wechat_verified', true, 'qq_verified', false),
    'learning', JSON_OBJECT('daily_goal', 30, 'study_streak', 7)
) WHERE user_id = '25072610450001';
```

### 索引优化

- 主要查询字段都建立了索引
- 支持高并发用户查询
- 优化了登录验证性能

## 📋 部署说明

1. **执行DDL脚本**：
   ```bash
   mysql -u root -p your_database < user_tables.sql
   ```

2. **验证表结构**：
   ```sql
   SHOW TABLES LIKE 'user%';
   SELECT generate_user_id();
   ```

3. **测试数据**：
   脚本会自动插入3个测试用户，可用于功能测试。

## 🔒 安全考虑

- 密码使用BCrypt加密存储
- 用户ID不连续，防止遍历攻击
- 完整的操作日志记录
- 支持会话过期管理
- IP地址记录，便于异常检测

## 📈 性能优化

- 合理的索引设计
- 分表设计，避免单表过大
- JSON字段用于灵活扩展
- 会话表支持定期清理过期数据

---

**版本**：v1.0  
**更新时间**：2025-07-26  
**维护者**：通学万卷开发团队
