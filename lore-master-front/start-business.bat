@echo off
chcp 65001 >nul

echo 🚀 启动B端前端服务...
echo 📍 端口: 3002
echo 🌐 访问地址: http://localhost:3002
echo.

cd web
npm run start:business

pause
