# API配置说明

本目录包含小程序的API配置文件，用于统一管理后端API域名和环境配置。

## 文件说明

### `api.ts` - 主配置文件
- 定义了不同环境的API配置
- 提供统一的API端点
- 包含请求头生成、URL构建等工具函数

### `env.ts` - 环境配置文件
- 用于手动切换开发环境
- 配置开发环境的具体参数
- 支持本地和局域网地址切换

## 使用方法

### 1. 基本使用

```typescript
import { API_ENDPOINTS, buildApiUrl, getApiHeaders, apiLog } from '../config/api';

// 使用预定义的API端点
const response = await request({
  url: API_ENDPOINTS.USER_INFO,
  method: 'GET',
  header: getApiHeaders(token)
});

// 构建自定义API URL
const customUrl = buildApiUrl('/api/custom/endpoint');

// 使用日志函数（仅在开发环境输出）
apiLog('API调用', { url, params });
```

### 2. 环境切换

#### 开发环境切换
编辑 `env.ts` 文件：

```typescript
// 设置当前环境
export const MANUAL_ENV: Environment = 'development'; // 或 'test', 'production'

// 启用手动环境设置
export const USE_MANUAL_ENV = true;
```

#### 开发服务器地址配置
```typescript
export const DEV_CONFIG = {
  // 本地开发服务器地址
  LOCAL_API_URL: 'http://localhost:8082',
  
  // 局域网开发服务器地址（用于真机调试）
  NETWORK_API_URL: 'http://192.168.1.100:8082', // 替换为你的电脑IP
  
  // 是否使用局域网地址
  USE_NETWORK_URL: false, // 真机调试时设为 true
};
```

### 3. 真机调试配置

当需要在真机上调试时：

1. 查看你的电脑IP地址
2. 修改 `env.ts` 中的 `NETWORK_API_URL`
3. 设置 `USE_NETWORK_URL: true`
4. 确保手机和电脑在同一局域网

### 4. 生产环境配置

发布到生产环境前：

1. 修改 `api.ts` 中的生产环境域名：
```typescript
production: {
  baseUrl: 'https://your-production-api.com', // 替换为实际域名
  timeout: 15000,
  enableLog: false,
},
```

2. 设置环境为生产模式：
```typescript
export const MANUAL_ENV: Environment = 'production';
```

## 环境说明

| 环境 | 说明 | 默认域名 | 日志 |
|------|------|----------|------|
| development | 开发环境 | localhost:8082 | 启用 |
| test | 测试环境 | test-api.loremaster.com | 启用 |
| production | 生产环境 | api.loremaster.com | 禁用 |

## API端点

当前预定义的API端点：

- `USER_LOGIN` - 用户登录
- `USER_INFO` - 获取用户信息
- `USER_AVATAR_UPLOAD` - 头像上传
- `COURSES_LIST` - 课程列表
- `COURSE_DETAIL` - 课程详情
- `LEARNING_PROGRESS` - 学习进度
- `LEARNING_STATS` - 学习统计

## 工具函数

### `buildApiUrl(path: string)`
构建完整的API URL

### `getApiHeaders(token?: string)`
获取标准请求头

### `apiLog(message: string, data?: any)`
输出API日志（仅在启用日志的环境）

## 注意事项

1. **安全性**：生产环境请确保 `enableLog: false`
2. **域名配置**：请根据实际情况修改各环境的域名
3. **真机调试**：确保网络连通性
4. **版本控制**：`env.ts` 中的敏感配置可考虑加入 `.gitignore`

## 故障排除

### 常见问题

1. **网络请求失败**
   - 检查当前环境配置
   - 确认API服务器是否启动
   - 检查网络连接

2. **真机调试连接失败**
   - 确认手机和电脑在同一网络
   - 检查防火墙设置
   - 验证IP地址是否正确

3. **环境切换不生效**
   - 确认 `USE_MANUAL_ENV` 为 `true`
   - 重新编译小程序
   - 检查控制台环境信息输出
