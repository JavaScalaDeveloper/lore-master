# 通学万卷 - 前端项目集合

## 📱 项目概述

通学万卷前端项目包含多个独立的应用模块，每个模块都有专用的端口号，可以独立启动和开发。

## 🎯 项目架构

### 后端服务 (API)
| 服务名称 | 端口 | 访问地址 | 说明 |
|---------|------|----------|------|
| 管理端API | 8080 | http://localhost:8080 | 管理员后台API服务 |
| B端API | 8081 | http://localhost:8081 | 商家业务API服务 |
| C端API | 8082 | http://localhost:8082 | 用户端API服务 |

### 前端应用
| 应用名称 | 端口 | 访问地址 | 说明 | 目录 |
|---------|------|----------|------|------|
| Web管理端 | 3001 | http://localhost:3001 | 管理员后台界面 | `/web` |
| Web B端 | 3002 | http://localhost:3002 | 商家业务界面 | `/web` |
| Web C端 | 3003 | http://localhost:3003 | 用户端界面 | `/web` |
| 小程序 | 3004 | http://localhost:3004 | 微信小程序界面 | `/miniapp` |

## 🚀 快速启动

### 方式一：使用启动脚本 (推荐)

#### Windows系统
```bash
# 启动管理端
start-admin.bat

# 启动B端
start-business.bat

# 启动C端
start-consumer.bat

# 启动小程序
start-miniapp.bat
```

#### Linux/Mac系统
```bash
# 启动管理端
./start-admin.sh

# 启动B端
./start-business.sh

# 启动C端
./start-consumer.sh

# 启动小程序
./start-miniapp.sh
```

### 方式二：手动启动

#### 启动Web管理端 (端口3001)
```bash
cd web
npm run start:admin
```

#### 启动Web B端 (端口3002)
```bash
cd web
npm run start:business
```

#### 启动Web C端 (端口3003)
```bash
cd web
npm run start:consumer
```

#### 启动小程序 (端口3004)
```bash
cd miniapp
npm start
```

## 🔧 开发配置

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0
- 对应的后端API服务

### 依赖安装
```bash
# 安装Web项目依赖
cd web && npm install

# 安装小程序依赖
cd miniapp && npm install
```

### 环境变量配置

每个项目都支持环境变量配置，可以通过 `.env.development` 和 `.env.production` 文件进行配置。

## 📊 项目结构

```
lore-master-app/
├── web/                    # Web前端项目 (管理端/B端/C端)
│   ├── src/
│   ├── package.json
│   └── README.md
├── miniapp/                # 小程序项目
│   ├── src/
│   ├── package.json
│   └── README.md
├── start-admin.bat         # Windows启动脚本 - 管理端
├── start-business.bat      # Windows启动脚本 - B端
├── start-consumer.bat      # Windows启动脚本 - C端
├── start-miniapp.bat       # Windows启动脚本 - 小程序
├── start-admin.sh          # Linux/Mac启动脚本 - 管理端
├── start-business.sh       # Linux/Mac启动脚本 - B端
├── start-consumer.sh       # Linux/Mac启动脚本 - C端
├── start-miniapp.sh        # Linux/Mac启动脚本 - 小程序
└── README.md              # 本文件
```

## 🌐 访问地址

### 开发环境
- **管理端**: http://localhost:3001
- **B端**: http://localhost:3002
- **C端**: http://localhost:3003
- **小程序**: http://localhost:3004

### 网络访问
- **管理端**: http://[你的IP]:3001
- **B端**: http://[你的IP]:3002
- **C端**: http://[你的IP]:3003
- **小程序**: http://[你的IP]:3004

## 🐛 常见问题

### 1. 端口冲突
如果遇到端口被占用的问题：
```bash
# 查看端口占用情况
netstat -ano | findstr :3001

# 杀掉占用端口的进程
taskkill /PID [进程ID] /F
```

### 2. 依赖安装失败
```bash
# 清除缓存重新安装
rm -rf node_modules package-lock.json
npm install
```

### 3. API请求失败
确保对应的后端服务已启动：
- 管理端需要启动8080端口的API服务
- B端需要启动8081端口的API服务
- C端和小程序需要启动8082端口的API服务

## 📞 技术支持

如果遇到问题，请检查：
1. Node.js和npm版本是否符合要求
2. 对应的后端API服务是否正常运行
3. 端口是否被其他程序占用
4. 网络连接是否正常

---

## 🎉 开始开发

选择你需要开发的模块，使用对应的启动脚本即可开始开发！

祝你开发愉快！ 🚀
