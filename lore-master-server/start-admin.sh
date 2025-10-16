#!/bin/bash

# 通学万卷 - 启动管理端后端服务
echo "🚀 启动管理端后端服务..."
echo "📍 端口: 8080"
echo "🌐 访问地址: http://localhost:8080"
echo ""

# 构建并启动应用
mvn -pl lore-master-web-admin spring-boot:run