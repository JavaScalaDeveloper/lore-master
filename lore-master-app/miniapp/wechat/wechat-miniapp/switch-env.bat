@echo off
chcp 65001 >nul
echo.
echo ==========================================
echo 🔧 小程序环境切换工具
echo ==========================================
echo.
echo 请选择要切换的环境:
echo.
echo   1. 本地环境 (localhost:8082)
echo   2. 远程环境 (ly112978940c.vicp.fun)
echo   3. 查看当前状态
echo   4. 退出
echo.
set /p choice=请输入选项 (1-4): 

if "%choice%"=="1" (
    echo.
    echo 🔄 正在切换到本地环境...
    node switch-env.js local
) else if "%choice%"=="2" (
    echo.
    echo 🔄 正在切换到远程环境...
    node switch-env.js remote
) else if "%choice%"=="3" (
    echo.
    node switch-env.js status
) else if "%choice%"=="4" (
    echo.
    echo 👋 再见！
    goto :end
) else (
    echo.
    echo ❌ 无效的选项，请重新选择
    pause
    goto :start
)

echo.
echo ⚠️  重要提醒：环境切换后，请执行以下命令重新构建：
echo    npm run build:weapp
echo.
pause

:end