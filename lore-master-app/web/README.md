# 管理端前端项目（React + Ant Design + TypeScript）

本项目基于 [Create React App](https://github.com/facebook/create-react-app) 脚手架，使用 React + TypeScript + Ant Design 技术栈，适用于管理后台系统。

---

## 目录结构

```
lore-master-app/web/
├── node_modules/           # 依赖包
├── public/                 # 静态资源
├── src/                    # 源码目录
│   └── pages/Login/        # 登录页（含RSA加密示例）
├── package.json            # 项目依赖配置
├── tsconfig.json           # TypeScript 配置
└── README.md               # 项目说明
```

---

## 启动与开发

1. 安装依赖（首次或依赖变动后）：
   ```shell
   npm install
   ```
2. 启动开发服务器：
   ```shell
   npm start
   ```
   启动后访问 [http://localhost:3000](http://localhost:3000)

---

## 主要依赖

- antd
- axios
- js-sha256
- lodash
- jsencrypt
- **react-router-dom**（页面跳转与路由）
- **@types/react-router-dom**（TypeScript 路由类型声明）

如需手动安装：
```shell
npm install antd axios@1 js-sha256 lodash jsencrypt react-router-dom@6 @types/react-router-dom --save
```

---

## 登录页密码加密与登录态说明

- 登录页已集成 RSA 加密，使用 [jsencrypt](https://github.com/travist/jsencrypt) 实现：
  - 前端用后端提供的公钥加密密码，后端用私钥解密。
  - 公钥请在 `src/pages/Login/index.tsx` 中替换为实际后端公钥。
- 登录成功后，前端会将后端返回的 token 信息保存到 localStorage，实现持久化登录态。
- 推荐全站使用 HTTPS，保障传输安全。

---

## 常见问题

- **依赖报错/缺包**：请严格按“依赖安装与修复”部分命令操作。
- **端口被占用**：修改 `package.json` 里的 `start` 脚本或关闭占用端口的进程。
- **登录接口地址**：如需调整，修改 `src/pages/Login/index.tsx` 里的 axios 请求地址。
- **RSA 公钥获取**：如需动态获取公钥，可在登录页组件中通过接口请求获取。

---

如有其他问题，请联系开发者或在 README 中补充说明。
