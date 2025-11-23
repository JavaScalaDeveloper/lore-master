@echo off
chcp 65001 >nul

echo 🚀 启动管理端前端服务...
echo 📍 端口: 3001
echo 🌐 访问地址: http://localhost:3001
echo.

cd web
npm run start:admin

pause
