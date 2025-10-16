@echo off
chcp 65001 >nul

echo 🚀 启动C端后端服务...
echo 📍 端口: 8082
echo 🌐 访问地址: http://localhost:8082
echo.

REM 构建并启动应用
mvn -pl lore-master-web-consumer spring-boot:run

pause