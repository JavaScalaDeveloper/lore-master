# 通学万卷 - 微信小程序

## 📱 项目简介

通学万卷微信小程序是一个基于React的学习平台，提供完整的用户学习体验，包括课程浏览、学习统计、用户管理等功能。

## 🎯 主要功能

- **🏠 主页**: 轮播图、功能导航、推荐课程、学习动态
- **📚 学习**: 课程分类、搜索功能、学习进度、课程管理
- **👤 个人中心**: 微信登录、用户信息、学习统计、功能菜单

## 🛠 技术栈

- **前端框架**: React 18 + TypeScript
- **UI样式**: CSS3 + 响应式设计
- **HTTP客户端**: 自定义封装的请求工具
- **状态管理**: React Hooks
- **构建工具**: Create React App

## 📋 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0
- 后端API服务 (端口8082)

## 🚀 快速启动

### 1. 启动后端服务

首先需要启动C端后端API服务：

```bash
# 进入后端项目目录
cd ../lore-master-server

# 编译项目
mvn clean compile -DskipTests

# 启动C端API服务
cd lore-master-web-consumer
mvn spring-boot:run
```

**确认后端启动成功**：
- 看到 `Started WebConsumerApplication` 日志
- 服务运行在端口 `8082`

### 2. 安装前端依赖

```bash
# 进入小程序目录
cd lore-master-app/miniapp

# 安装依赖
npm install
```

### 3. 启动前端服务

```bash
# 启动开发服务器
npm start
```

**启动过程**：
1. 如果3000端口被占用，选择 `Y` 使用其他端口
2. 等待编译完成
3. 自动打开浏览器访问应用

### 4. 访问应用

- **本地访问**: http://localhost:3004
- **网络访问**: http://[你的IP]:3004

## 🎮 使用指南

### 基本操作

1. **浏览主页**
   - 查看轮播图和推荐课程
   - 点击功能导航快速访问

2. **学习功能**
   - 点击底部"学习"Tab
   - 选择课程分类
   - 查看学习进度

3. **用户登录**
   - 点击底部"个人中心"Tab
   - 点击"立即登录"
   - 选择"微信登录"或"手机登录"

### 微信登录测试

1. 进入个人中心
2. 点击"立即登录"
3. 选择"微信登录"
4. 等待模拟授权完成
5. 查看用户信息和学习统计

## 🔧 开发配置

### 环境变量

项目支持多环境配置：

**开发环境** (`.env.development`):
```env
REACT_APP_CONSUMER_API=http://localhost:8082
REACT_APP_DEBUG=true
```

**生产环境** (`.env.production`):
```env
REACT_APP_CONSUMER_API=https://consumer-api.loremaster.com
REACT_APP_DEBUG=false
```

### API配置

API配置文件位于 `src/config/api.ts`：

```typescript
export const DOMAINS = {
  CONSUMER_API: process.env.REACT_APP_CONSUMER_API || 'http://localhost:8082',
  // 其他配置...
};
```

## 🐛 常见问题

### 1. 端口冲突

**问题**: 3000端口被占用
**解决**: 选择Y使用其他端口，或手动指定端口：
```bash
PORT=3002 npm start
```

### 2. API请求失败

**问题**: 登录时提示网络错误
**解决步骤**:
1. 确认后端服务已启动 (端口8082)
2. 检查浏览器控制台错误信息
3. 确认CORS配置正确

### 3. 编译错误

**问题**: TypeScript编译错误
**解决**:
```bash
# 清除缓存重新安装
rm -rf node_modules package-lock.json
npm install
```

## 📊 项目结构

```
src/
├── components/          # 公共组件
│   └── TabBar.tsx      # 底部导航栏
├── pages/              # 页面组件
│   ├── Home.tsx        # 主页
│   ├── Study.tsx       # 学习页面
│   └── Profile.tsx     # 个人中心
├── config/             # 配置文件
│   └── api.ts          # API配置
├── utils/              # 工具函数
│   └── request.ts      # HTTP请求工具
└── styles/             # 样式文件
    ├── Home.css
    ├── Study.css
    └── Profile.css
```

## 🔍 调试技巧

### 1. 查看API请求

打开浏览器开发者工具 (F12)，在Console中查看：
```
🚀 准备发送API请求: {...}
🌐 API请求成功: {...}
```

### 2. 网络请求监控

在Network标签页中监控：
- 请求URL是否正确
- 响应状态码
- 请求和响应数据

### 3. 本地存储检查

在Application标签页中查看：
- `userInfo`: 用户信息
- `userToken`: 访问令牌
- `tokenType`: 令牌类型

## 🚀 部署说明

### 开发环境部署

1. 确保后端服务运行在8082端口
2. 前端自动使用localhost:8082作为API地址

### 生产环境部署

1. 更新 `.env.production` 中的API地址
2. 构建生产版本：
```bash
npm run build
```
3. 部署build目录到Web服务器

## 📞 技术支持

如果遇到问题，请检查：

1. **后端服务状态**: 确认8082端口服务正常
2. **网络连接**: 确认前后端网络通信正常
3. **浏览器控制台**: 查看详细错误信息
4. **API响应**: 确认后端返回正确的数据格式

---

## 🎉 开始使用

现在你可以开始使用通学万卷小程序了！

1. 启动后端服务 (端口8082)
2. 启动前端服务 (端口3001)
3. 打开浏览器访问应用
4. 体验完整的学习功能

祝你使用愉快！ 🚀
