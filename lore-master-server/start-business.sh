#!/bin/bash

# 通学万卷 - 启动B端后端服务
echo "🚀 启动B端后端服务..."
echo "📍 端口: 8081"
echo "🌐 访问地址: http://localhost:8081"
echo ""

# 构建并启动应用
mvn -pl lore-master-web-business spring-boot:run