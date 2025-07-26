# 通学万卷 - 后端服务

后端父模块，统一管理所有后端子模块的依赖和版本。

## 📁 模块结构

### 🏗️ 基础模块
- **lore-master-common**: 公共工具与基础设施
- **lore-master-data**: 数据访问与持久化层
- **lore-master-service**: 业务逻辑实现层
- **lore-master-integration**: 外部平台与第三方系统集成

### 🌐 Web服务模块
- **lore-master-web-admin**: 管理端Web服务 (端口: 8080)
- **lore-master-web-business**: 业务端Web服务 (端口: 8081)
- **lore-master-web-consumer**: C端用户Web服务 (端口: 8082)

## 🚀 服务端口分配

| 服务模块 | 端口 | 用途 | 数据库 |
|---------|------|------|--------|
| lore-master-web-admin | 8080 | 管理员后台 | lore_admin |
| lore-master-web-business | 8081 | 业务管理端 | lore_business |
| lore-master-web-consumer | 8082 | C端用户服务 | lore_business |

## 🎯 模块职责

### lore-master-web-consumer (C端用户服务)
**专门面向C端用户的服务模块**

**功能特性:**
- ✅ 用户注册 (用户名/邮箱/手机号)
- ✅ 用户登录与认证
- ✅ 个人信息管理
- ✅ 学习进度跟踪
- ✅ 积分等级系统
- 🔄 学习内容推荐 (开发中)
- 🔄 社交互动功能 (开发中)

**技术特点:**
- 使用策略模式支持多种注册方式
- 统一的密码加密传输机制
- Redis缓存优化用户体验
- JWT令牌认证机制
- 完善的CORS跨域配置

### lore-master-web-business (业务管理端)
**面向内部业务人员的管理服务**

**功能特性:**
- 📊 业务数据统计
- 👥 用户管理
- 📝 内容管理
- 🎯 运营活动管理

### lore-master-web-admin (系统管理端)
**面向系统管理员的后台服务**

**功能特性:**
- 🔐 管理员认证
- ⚙️ 系统配置管理
- 📈 系统监控
- 🗄️ 数据库管理

## 🛠️ 快速启动

### 启动C端用户服务
```bash
mvn spring-boot:run -pl lore-master-web-consumer
```

### 启动业务管理端
```bash
mvn spring-boot:run -pl lore-master-web-business
```

### 启动系统管理端
```bash
mvn spring-boot:run -pl lore-master-web-admin
```

## 📋 API文档

### C端用户服务 API (8082端口)
```
POST /api/user/register              # 用户注册
GET  /api/user/register/check        # 检查注册标识可用性
POST /api/user/register/send-code    # 发送验证码
POST /api/user/login                 # 用户登录
GET  /api/user/profile               # 获取用户信息
PUT  /api/user/profile               # 更新用户信息
```

### 业务管理端 API (8081端口)
```
POST /api/user/register              # 用户注册 (业务端)
GET  /api/business/stats             # 业务统计
GET  /api/business/users             # 用户管理
```

### 系统管理端 API (8080端口)
```
POST /api/admin/auth/login           # 管理员登录
GET  /api/admin/users                # 用户管理
GET  /api/admin/system/config        # 系统配置
```

## 🔧 开发环境

- **Java**: 17+
- **Spring Boot**: 3.2.6
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Maven**: 3.8+

## 📝 注意事项

1. **端口冲突**: 确保8080、8081、8082端口未被占用
2. **数据库配置**: 需要创建对应的数据库 (lore_admin, lore_business)
3. **Redis配置**: 不同服务使用不同的Redis数据库编号
4. **跨域配置**: 前端默认运行在3000端口，已配置CORS支持