#!/bin/bash

# --- 配置变量 ---
MAVEN_VERSION="3.9.11"
SOURCE_DIR="/opt/pkg"
TAR_FILE="apache-maven-3.9.11-bin.tar.gz"
SOURCE_PATH="$SOURCE_DIR/$TAR_FILE"

TARGET_DIR="/opt/software"
# 压缩包解压后，内部的根目录名称通常与版本号一致
EXTRACTED_DIR_NAME="apache-maven-$MAVEN_VERSION"
MAVEN_INSTALL_PATH="$TARGET_DIR/$EXTRACTED_DIR_NAME"
ENV_PROFILE_FILE="/etc/profile.d/maven.sh"

echo "--- Apache Maven $MAVEN_VERSION 本地安装脚本 ---"

# 1. 权限检查
if [ "$EUID" -ne 0 ]; then
  echo "错误: 请使用 sudo 运行此脚本。"
  exit 1
fi

# 2. 检查源文件是否存在
if [ ! -f "$SOURCE_PATH" ]; then
  echo "错误: 找不到 Maven 安装包: $SOURCE_PATH"
  echo "请确认文件路径是否正确。"
  exit 1
fi

# 3. 检查并清理旧安装 (如果目标目录已存在)
if [ -d "$MAVEN_INSTALL_PATH" ]; then
    echo "警告: 目标安装目录 $MAVEN_INSTALL_PATH 已存在，正在删除旧目录..."
    rm -rf "$MAVEN_INSTALL_PATH"
fi

# 4. 创建目标安装目录
if [ ! -d "$TARGET_DIR" ]; then
  echo "创建目标目录: $TARGET_DIR"
  mkdir -p "$TARGET_DIR"
fi

# 5. 解压安装
echo "正在解压 $TAR_FILE 到 $TARGET_DIR..."
# -C 选项确保文件解压到目标目录，并保留文件夹全名
if ! tar -xzf "$SOURCE_PATH" -C "$TARGET_DIR"; then
    echo "错误: 解压文件失败。"
    exit 1
fi

# 6. 验证解压是否成功
if [ ! -d "$MAVEN_INSTALL_PATH" ]; then
    echo "错误: 解压完成后，未找到预期的目录 $MAVEN_INSTALL_PATH。安装失败。"
    exit 1
fi

echo "Maven $MAVEN_VERSION 安装成功在: $MAVEN_INSTALL_PATH"

# 7. 配置环境变量
echo "正在配置系统环境变量文件: $ENV_PROFILE_FILE"

cat << EOF > "$ENV_PROFILE_FILE"
# Apache Maven Configuration
export MAVEN_HOME=$MAVEN_INSTALL_PATH
export PATH=\$MAVEN_HOME/bin:\$PATH
EOF

chmod +x "$ENV_PROFILE_FILE"

# 8. 验证安装
echo "--- 验证安装 ---"

# 临时加载环境变量进行验证
source "$ENV_PROFILE_FILE"

if command -v mvn &> /dev/null; then
    echo "MAVEN_HOME 已设置: $MAVEN_HOME"
    echo "Maven 版本信息:"
    mvn -v
else
    echo "警告: mvn 命令未在 PATH 中找到，请检查配置。"
fi

echo ""
echo "--- 安装完成 ---"
echo "要使环境变量在当前终端生效，请执行以下命令:"
echo "source $ENV_PROFILE_FILE"

