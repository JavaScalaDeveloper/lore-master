# API配置重构文档

## 📋 概述

本次重构将前端代码中所有硬编码的API域名统一管理，提高了代码的可维护性和部署灵活性。

## 🎯 重构目标

- ✅ 消除硬编码的API地址（如 `http://localhost:8082`）
- ✅ 统一管理所有API配置
- ✅ 支持多环境配置（开发、测试、生产）
- ✅ 提供统一的HTTP请求工具
- ✅ 支持请求拦截器和错误处理

## 📁 新增文件结构

```
src/
├── config/
│   └── api.ts              # API配置文件
├── utils/
│   └── request.ts          # HTTP请求工具类
├── .env.development        # 开发环境变量
└── .env.production         # 生产环境变量
```

## 🔧 核心配置文件

### 1. `src/config/api.ts`

**功能：**
- 统一管理所有API域名和路径
- 支持多环境配置
- 提供API地址构建函数
- 定义CORS和请求配置

**主要导出：**
```typescript
export const DOMAINS = {
  ADMIN_API: 'http://localhost:8080',
  BUSINESS_API: 'http://localhost:8081', 
  CONSUMER_API: 'http://localhost:8082',
  WEB_FRONTEND: 'http://localhost:3001'
};

export const API_PATHS = {
  ADMIN: { AUTH: { LOGIN: '/api/admin/auth/login' } },
  CONSUMER: { AUTH: { LOGIN: '/api/user/login' } },
  // ... 更多路径
};
```

### 2. `src/utils/request.ts`

**功能：**
- 提供统一的HTTP请求客户端
- 支持请求/响应拦截器
- 自动处理认证token
- 支持请求重试和超时
- 错误统一处理

**主要导出：**
```typescript
export const adminApi = new HttpClient();     // 管理端API
export const consumerApi = new HttpClient();  // C端用户API
export const businessApi = new HttpClient();  // 业务端API
```

## 🌍 环境配置

### 开发环境 (`.env.development`)
```env
REACT_APP_ADMIN_API=http://localhost:8080
REACT_APP_BUSINESS_API=http://localhost:8081
REACT_APP_CONSUMER_API=http://localhost:8082
REACT_APP_WEB_FRONTEND=http://localhost:3001
```

### 生产环境 (`.env.production`)
```env
REACT_APP_ADMIN_API=https://admin-api.loremaster.com
REACT_APP_BUSINESS_API=https://business-api.loremaster.com
REACT_APP_CONSUMER_API=https://consumer-api.loremaster.com
REACT_APP_WEB_FRONTEND=https://www.loremaster.com
```

## 📝 代码迁移示例

### 迁移前（硬编码）
```typescript
const response = await fetch('http://localhost:8082/api/user/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(data)
});
const result = await response.json();
```

### 迁移后（配置化）
```typescript
import { consumerApi } from '../../../utils/request';
import { API_PATHS } from '../../../config/api';

const result = await consumerApi.post(API_PATHS.CONSUMER.AUTH.LOGIN, data);
```

## 🔄 已迁移的组件

### C端用户相关
- ✅ `UserLoginModal.tsx` - 用户登录
- ✅ `UserRegisterModal.tsx` - 用户注册

### 管理端相关
- ✅ `Login/index.tsx` - 管理员登录
- ✅ `UserManage/index.tsx` - 用户管理
- ✅ `CareerTargetManage/index.tsx` - 职业目标管理
- ✅ `KnowledgeManage/index.tsx` - 知识管理

### 后端配置
- ✅ `WebConfig.java` - CORS配置更新

## 🚀 使用方法

### 1. 基本API调用
```typescript
import { consumerApi } from '../utils/request';
import { API_PATHS } from '../config/api';

// GET请求
const userData = await consumerApi.get(API_PATHS.CONSUMER.PROFILE.GET);

// POST请求
const result = await consumerApi.post(API_PATHS.CONSUMER.AUTH.LOGIN, {
  username: 'user',
  password: 'pass'
});
```

### 2. 自定义请求配置
```typescript
const result = await consumerApi.post('/api/custom', data, {
  timeout: 5000,
  retry: 2,
  headers: { 'Custom-Header': 'value' }
});
```

### 3. 添加请求拦截器
```typescript
consumerApi.addRequestInterceptor((config) => {
  config.headers = { ...config.headers, 'X-Custom': 'value' };
  return config;
});
```

## 🎨 特性优势

### 1. 环境适配
- 自动根据 `NODE_ENV` 选择对应的API域名
- 支持通过环境变量覆盖默认配置

### 2. 开发体验
- 统一的错误处理和日志输出
- 自动添加认证token
- 支持请求重试和超时控制

### 3. 维护性
- 所有API地址集中管理
- 类型安全的API路径定义
- 清晰的代码结构和注释

## 🔧 部署配置

### 1. 开发环境
无需额外配置，使用默认的localhost地址。

### 2. 生产环境
更新 `.env.production` 文件中的域名：
```env
REACT_APP_ADMIN_API=https://your-admin-api.com
REACT_APP_CONSUMER_API=https://your-consumer-api.com
```

### 3. 后端CORS配置
确保后端服务的CORS配置包含前端域名：
```java
.allowedOrigins(
    "http://localhost:3001",           // 开发环境
    "https://www.loremaster.com"       // 生产环境
)
```

## 📊 重构成果

- **✅ 消除硬编码**: 移除了所有硬编码的API地址
- **✅ 统一管理**: 所有API配置集中在配置文件中
- **✅ 环境支持**: 支持开发、测试、生产多环境
- **✅ 类型安全**: 提供完整的TypeScript类型定义
- **✅ 错误处理**: 统一的错误处理和重试机制
- **✅ 开发体验**: 更好的调试信息和开发工具

## 🔮 后续优化建议

1. **API Mock**: 添加开发环境的API Mock功能
2. **缓存策略**: 实现请求结果缓存机制
3. **监控上报**: 添加API请求监控和错误上报
4. **文档生成**: 自动生成API文档
5. **测试覆盖**: 增加HTTP客户端的单元测试

---

**重构完成时间**: 2025-07-26  
**重构范围**: 前端API配置统一管理  
**影响组件**: 8个组件文件 + 2个配置文件  
**向后兼容**: ✅ 完全兼容现有功能
