#!/bin/bash

# 通学万卷 - 启动B端前端
echo "🚀 启动B端前端服务..."
echo "📍 端口: 3002"
echo "🌐 访问地址: http://localhost:3002"
echo ""

cd web
npm run start:business
