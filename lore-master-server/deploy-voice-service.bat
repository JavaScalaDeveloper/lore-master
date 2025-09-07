@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 讯飞语音识别后端快速部署脚本
:: 作者: lore-master
:: 日期: 2024-09-07

echo 🚀 开始部署讯飞语音识别后端服务...
echo.

:: 检查环境变量
:check_env_vars
echo 📋 检查环境变量...

set missing_vars=
if "%XFYUN_APP_ID%"=="" set missing_vars=!missing_vars! XFYUN_APP_ID
if "%XFYUN_API_KEY%"=="" set missing_vars=!missing_vars! XFYUN_API_KEY
if "%XFYUN_API_SECRET%"=="" set missing_vars=!missing_vars! XFYUN_API_SECRET

if not "!missing_vars!"=="" (
    echo ❌ 缺少以下环境变量:
    for %%i in (!missing_vars!) do echo    - %%i
    echo.
    echo 💡 请先设置环境变量:
    echo    set XFYUN_APP_ID=your_app_id
    echo    set XFYUN_API_KEY=your_api_key
    echo    set XFYUN_API_SECRET=your_api_secret
    echo.
    echo 或者在IDE的Run Configuration中添加这些环境变量
    pause
    exit /b 1
)

echo ✅ 环境变量检查通过
echo.

:: 检查Java
:check_java
echo ☕ 检查Java版本...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未找到Java，请安装JDK 17+
    pause
    exit /b 1
)

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set java_version=%%g
    set java_version=!java_version:"=!
)
echo ✅ Java版本: !java_version!
echo.

:: 检查Maven
:check_maven
echo 🔧 检查Maven...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未找到Maven，请安装Maven 3.6+
    pause
    exit /b 1
)

echo ✅ Maven检查通过
echo.

:: 编译项目
:build_project
echo 🔨 编译项目...
echo 📦 执行 mvn clean install...

mvn clean install -DskipTests=true
if errorlevel 1 (
    echo ❌ 项目编译失败
    pause
    exit /b 1
)

echo ✅ 项目编译成功
echo.

:: 创建日志目录
if not exist logs mkdir logs

:: 启动服务
:start_service
echo 🚀 启动consumer服务...
cd lore-master-web-consumer

echo 🌟 启动Spring Boot应用...
echo 📝 日志将输出到 logs/lore-master-consumer.log

:: 启动服务
start "lore-master-consumer" cmd /c "mvn spring-boot:run > ../logs/startup.log 2>&1"

echo ✅ 服务启动命令已执行
echo.

:: 等待服务启动
echo ⏳ 等待服务启动...
timeout /t 15 /nobreak >nul

:: 检查服务状态
:check_service
echo 🏥 检查服务健康状态...

:: 等待服务启动
for /l %%i in (1,1,10) do (
    curl -f http://localhost:8082/api/voice/status >nul 2>&1
    if not errorlevel 1 (
        echo ✅ 服务启动成功！
        echo 🌐 API地址: http://localhost:8082
        echo 📊 服务状态: http://localhost:8082/api/voice/status
        goto test_api
    )
    echo ⏳ 等待服务启动... (%%i/10)
    timeout /t 3 /nobreak >nul
)

echo ❌ 服务启动超时，请检查日志
echo 📝 查看启动日志: type logs\startup.log
echo 📝 查看应用日志: type logs\lore-master-consumer.log
goto show_usage

:: 测试API
:test_api
echo 🧪 测试API功能...
echo 📡 测试服务状态API...

curl -s http://localhost:8082/api/voice/status > temp_response.txt
findstr "success" temp_response.txt >nul
if not errorlevel 1 (
    echo ✅ 服务状态API测试通过
    echo 📄 响应内容:
    type temp_response.txt
) else (
    echo ❌ 服务状态API测试失败
    echo 📄 响应内容:
    type temp_response.txt
)
del temp_response.txt >nul 2>&1
echo.

:: 显示使用说明
:show_usage
echo 📖 使用说明:
echo 1. 服务地址: http://localhost:8082
echo 2. 语音转文字API: POST /api/voice/transcribe
echo 3. 服务状态API: GET /api/voice/status
echo 4. 停止服务: 关闭启动的命令行窗口
echo 5. 查看日志: type logs\lore-master-consumer.log
echo.
echo 📝 API测试示例:
echo curl -X POST http://localhost:8082/api/voice/transcribe ^
echo   -H "Authorization: Bearer YOUR_TOKEN" ^
echo   -F "audioFile=@test.mp3" ^
echo   -F "userId=test123" ^
echo   -F "format=mp3"
echo.

echo 🎉 部署完成！
echo.
pause