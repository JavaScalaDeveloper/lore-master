#!/bin/bash

# CentOS Stream 10 一键安装 Docker 和 Docker Compose 脚本
# 使用前请确保以root用户执行，或有sudo权限

set -e  # 遇到任何错误立即退出脚本

echo "========================================"
echo "开始安装 Docker 和 Docker Compose"
echo "适用于 CentOS Stream 10"
echo "========================================"

# 1. 安装依赖包
echo "步骤1: 安装系统依赖包..."
yum install -y yum-utils device-mapper-persistent-data lvm2

# 2. 添加 Docker 官方仓库
echo "步骤2: 添加 Docker CE 官方仓库..."
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 3. 安装 Docker CE
echo "步骤3: 安装 Docker CE (社区版)..."
yum install -y docker-ce docker-ce-cli containerd.io

# 4. 启动 Docker 服务并设置开机自启
echo "步骤4: 启动 Docker 服务..."
systemctl start docker
systemctl enable docker

# 5. 安装 Docker Compose
echo "步骤5: 安装 Docker Compose..."
# 下载最新的 Docker Compose 二进制文件
COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
echo "正在下载 Docker Compose 版本: $COMPOSE_VERSION"

curl -L "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# 赋予执行权限
chmod +x /usr/local/bin/docker-compose

# 6. 创建符号链接到 PATH 路径 (可选，确保在任何目录下都能执行)
if [ ! -f /usr/bin/docker-compose ]; then
    ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
fi

# 7. 验证安装
echo "步骤6: 验证安装..."

# 检查 Docker 版本
docker --version

# 检查 Docker Compose 版本
docker-compose --version

echo "========================================"
echo "安装完成!"
echo "----------------------------------------"
echo "Docker 版本: $(docker --version | cut -d' ' -f3 | cut -d',' -f1)"
echo "Docker Compose 版本: $(docker-compose --version | cut -d' ' -f3 | cut -d',' -f1)"
echo "========================================"
echo "常用命令:"
echo " - 启动 Docker: systemctl start docker"
echo " - 停止 Docker: systemctl stop docker"
echo " - 重启 Docker: systemctl restart docker"
echo " - 查看 Docker 状态: systemctl status docker"
echo " - 使用 Docker Compose: docker-compose up -d"
echo "========================================"