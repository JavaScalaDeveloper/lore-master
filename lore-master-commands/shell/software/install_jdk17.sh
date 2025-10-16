#!/bin/bash

# --- 变量定义 ---
SOURCE_DIR="/opt/pkg"
TAR_FILE="OpenJDK17U-jdk_x64_linux_hotspot_17.0.16_8.tar.gz"
TARGET_DIR="/opt/software"
JDK_LINK_NAME="jdk-17"
ENV_PROFILE_FILE="/etc/profile.d/jdk17.sh"

SOURCE_FILE="$SOURCE_DIR/$TAR_FILE"
NEW_JDK_PATH="$TARGET_DIR/$JDK_LINK_NAME"

echo "--- JDK 17 自动化安装脚本 ---"

# 1. 权限检查
if [ "$EUID" -ne 0 ]; then
  echo "错误: 请使用 sudo 运行此脚本。"
  exit 1
fi

# 2. 检查安装包是否存在
if [ ! -f "$SOURCE_FILE" ]; then
  echo "错误: 找不到安装包 $SOURCE_FILE"
  echo "请确保文件存在于指定的路径。"
  exit 1
fi

# 3. 创建目标安装目录
if [ ! -d "$TARGET_DIR" ]; then
  echo "创建目标目录: $TARGET_DIR"
  mkdir -p "$TARGET_DIR"
fi

# 4. 解压安装包
echo "正在解压 JDK 17 到 $TARGET_DIR..."
# -C 选项指定解压目标目录
tar -xzf "$SOURCE_FILE" -C "$TARGET_DIR"

# 5. 查找并重命名解压后的目录
# 查找 TARGET_DIR 中以 "jdk-" 开头的目录，这通常是解压后的目录名
EXTRACTED_DIR=$(find "$TARGET_DIR" -maxdepth 1 -type d -name "jdk-*" -not -name "$JDK_LINK_NAME" | head -n 1)

if [ -z "$EXTRACTED_DIR" ]; then
  echo "错误: 解压后未找到 JDK 目录。安装失败。"
  exit 1
fi

# 确保重命名目标目录不存在，避免冲突
if [ -d "$NEW_JDK_PATH" ]; then
    echo "警告: 目标链接目录 $NEW_JDK_PATH 已存在，正在删除旧目录..."
    rm -rf "$NEW_JDK_PATH"
fi

echo "重命名目录: $(basename "$EXTRACTED_DIR") -> $JDK_LINK_NAME"
mv "$EXTRACTED_DIR" "$NEW_JDK_PATH"

echo "JDK 17 安装成功在: $NEW_JDK_PATH"

# 6. 配置环境变量
echo "正在配置系统环境变量文件: $ENV_PROFILE_FILE"

cat << EOF > "$ENV_PROFILE_FILE"
# JDK 17 Configuration
export JAVA_HOME=$NEW_JDK_PATH
export PATH=\$JAVA_HOME/bin:\$PATH
EOF

chmod +x "$ENV_PROFILE_FILE"

# 7. 验证安装
echo "--- 验证安装 ---"

# 临时加载环境变量进行验证 (仅在当前脚本子shell中有效)
source "$ENV_PROFILE_FILE"

if command -v java &> /dev/null; then
    echo "JAVA_HOME 已设置: $JAVA_HOME"
    echo "Java 版本信息:"
    java -version
else
    echo "警告: java 命令未在 PATH 中找到，请检查配置。"
fi

echo ""
echo "--- 安装完成 ---"
echo "要使环境变量在当前终端生效，请执行以下命令:"
echo "source $ENV_PROFILE_FILE"

