#!/bin/bash

# 构建React Native Android APK脚本（禁用自动链接）

echo "开始构建APK（禁用自动链接）..."

# 进入项目目录
cd /Users/huang/Documents/Workspaces/lore-master/lore-master-app/miniapp/wechat/wechat-miniapp

# 进入Android目录
cd android

# 清理之前的构建
echo "清理之前的构建..."
./gradlew clean

# 构建Debug APK
echo "构建Debug APK..."
./gradlew assembleDebug

# 检查构建是否成功
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "APK构建成功！"
    echo "APK文件位置: app/build/outputs/apk/debug/app-debug.apk"
else
    echo "APK构建失败，请检查错误信息。"
fi