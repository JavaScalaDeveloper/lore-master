@echo off
chcp 65001 >nul

echo 🚀 启动C端前端服务...
echo 📍 端口: 3003
echo 🌐 访问地址: http://localhost:3003
echo.

cd web
npm run start:consumer

pause
