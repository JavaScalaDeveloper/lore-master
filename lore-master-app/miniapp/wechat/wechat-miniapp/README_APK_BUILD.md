# Android APK 构建指南

本文档介绍了如何构建Android APK文件的步骤和注意事项。

## 构建APK

### 1. 准备工作

确保您已经安装了以下工具：
- Node.js (推荐版本: 18.x 或更高)
- JDK (推荐版本: 11 或更高)
- Android Studio (包含Android SDK)

### 2. 安装依赖

在项目根目录下运行以下命令安装依赖：

```bash
npm install
```

### 3. 构建APK

使用以下脚本构建APK：

```bash
# 构建Debug版本APK
./scripts/build-apk-no-autolink.sh
```

构建完成后，APK文件将位于以下路径：
```
android/app/build/outputs/apk/debug/app-debug.apk
```

### 4. 安装APK到设备

您可以使用以下命令将APK安装到连接的Android设备：

```bash
adb install android/app/build/outputs/apk/debug/app-debug.apk
```

## 构建脚本说明

### build-apk-no-autolink.sh

该脚本用于构建Android APK，禁用了React Native的自动链接功能以避免构建错误。

脚本路径：`scripts/build-apk-no-autolink.sh`

主要步骤：
1. 清理之前的构建缓存
2. 构建Debug版本APK
3. 检查构建结果并输出APK文件位置

## 常见问题

### 1. 构建失败

如果构建失败，请检查以下几点：
- 确保所有依赖已正确安装
- 检查Android SDK路径是否正确配置
- 确认Java和Gradle版本兼容

### 2. APK安装失败

如果APK安装失败，请检查：
- 设备是否开启开发者选项和USB调试
- 设备Android版本是否兼容
- 是否已卸载旧版本应用

## 注意事项

1. 当前构建的APK为Debug版本，仅用于测试目的
2. 如需发布到应用商店，请构建Release版本并进行签名
3. APK文件较大（约148MB），请确保设备有足够的存储空间