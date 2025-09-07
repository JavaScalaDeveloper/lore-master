#!/bin/bash

# 讯飞语音识别后端快速部署脚本
# 作者: lore-master
# 日期: 2024-09-07

echo "🚀 开始部署讯飞语音识别后端服务..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查环境变量
check_env_vars() {
    echo -e "${BLUE}📋 检查环境变量...${NC}"
    
    missing_vars=()
    
    if [ -z "$XFYUN_APP_ID" ]; then
        missing_vars+=("XFYUN_APP_ID")
    fi
    
    if [ -z "$XFYUN_API_KEY" ]; then
        missing_vars+=("XFYUN_API_KEY")
    fi
    
    if [ -z "$XFYUN_API_SECRET" ]; then
        missing_vars+=("XFYUN_API_SECRET")
    fi
    
    if [ ${#missing_vars[@]} -ne 0 ]; then
        echo -e "${RED}❌ 缺少以下环境变量:${NC}"
        for var in "${missing_vars[@]}"; do
            echo -e "${RED}   - $var${NC}"
        done
        echo -e "${YELLOW}💡 请先设置环境变量:${NC}"
        echo -e "${YELLOW}   export XFYUN_APP_ID=your_app_id${NC}"
        echo -e "${YELLOW}   export XFYUN_API_KEY=your_api_key${NC}"
        echo -e "${YELLOW}   export XFYUN_API_SECRET=your_api_secret${NC}"
        echo -e "${YELLOW}或者在IDE的Run Configuration中添加这些环境变量${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ 环境变量检查通过${NC}"
}

# 检查Java版本
check_java() {
    echo -e "${BLUE}☕ 检查Java版本...${NC}"
    
    if ! command -v java &> /dev/null; then
        echo -e "${RED}❌ 未找到Java，请安装JDK 17+${NC}"
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | head -1)
    echo -e "${GREEN}✅ Java版本: $java_version${NC}"
}

# 检查Maven
check_maven() {
    echo -e "${BLUE}🔧 检查Maven...${NC}"
    
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}❌ 未找到Maven，请安装Maven 3.6+${NC}"
        exit 1
    fi
    
    mvn_version=$(mvn -version | head -1)
    echo -e "${GREEN}✅ $mvn_version${NC}"
}

# 编译项目
build_project() {
    echo -e "${BLUE}🔨 编译项目...${NC}"
    
    # 进入项目根目录
    cd "$(dirname "$0")" || exit 1
    
    # 清理并编译
    echo -e "${YELLOW}📦 执行 mvn clean install...${NC}"
    mvn clean install -DskipTests=true
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 项目编译成功${NC}"
    else
        echo -e "${RED}❌ 项目编译失败${NC}"
        exit 1
    fi
}

# 启动服务
start_service() {
    echo -e "${BLUE}🚀 启动consumer服务...${NC}"
    
    cd lore-master-web-consumer || exit 1
    
    echo -e "${YELLOW}🌟 启动Spring Boot应用...${NC}"
    echo -e "${YELLOW}📝 日志将输出到 logs/lore-master-consumer.log${NC}"
    
    # 后台启动服务
    nohup mvn spring-boot:run > ../logs/startup.log 2>&1 &
    
    # 获取进程ID
    SERVICE_PID=$!
    echo $SERVICE_PID > ../service.pid
    
    echo -e "${GREEN}✅ 服务已启动，进程ID: $SERVICE_PID${NC}"
    
    # 等待服务启动
    echo -e "${YELLOW}⏳ 等待服务启动...${NC}"
    sleep 10
    
    # 检查服务状态
    check_service_health
}

# 检查服务健康状态
check_service_health() {
    echo -e "${BLUE}🏥 检查服务健康状态...${NC}"
    
    # 尝试访问健康检查端点
    for i in {1..30}; do
        if curl -f http://localhost:8082/api/voice/status > /dev/null 2>&1; then
            echo -e "${GREEN}✅ 服务启动成功！${NC}"
            echo -e "${GREEN}🌐 API地址: http://localhost:8082${NC}"
            echo -e "${GREEN}📊 服务状态: http://localhost:8082/api/voice/status${NC}"
            return 0
        fi
        
        echo -e "${YELLOW}⏳ 等待服务启动... ($i/30)${NC}"
        sleep 2
    done
    
    echo -e "${RED}❌ 服务启动超时，请检查日志${NC}"
    echo -e "${YELLOW}📝 查看启动日志: tail -f logs/startup.log${NC}"
    echo -e "${YELLOW}📝 查看应用日志: tail -f logs/lore-master-consumer.log${NC}"
    return 1
}

# 测试API
test_api() {
    echo -e "${BLUE}🧪 测试API功能...${NC}"
    
    # 测试服务状态API
    echo -e "${YELLOW}📡 测试服务状态API...${NC}"
    response=$(curl -s http://localhost:8082/api/voice/status)
    
    if echo "$response" | grep -q "success"; then
        echo -e "${GREEN}✅ 服务状态API测试通过${NC}"
        echo -e "${GREEN}📄 响应: $response${NC}"
    else
        echo -e "${RED}❌ 服务状态API测试失败${NC}"
        echo -e "${RED}📄 响应: $response${NC}"
    fi
}

# 显示使用说明
show_usage() {
    echo -e "${BLUE}📖 使用说明:${NC}"
    echo -e "${GREEN}1. 服务地址: http://localhost:8082${NC}"
    echo -e "${GREEN}2. 语音转文字API: POST /api/voice/transcribe${NC}"
    echo -e "${GREEN}3. 服务状态API: GET /api/voice/status${NC}"
    echo -e "${GREEN}4. 停止服务: kill \$(cat service.pid)${NC}"
    echo -e "${GREEN}5. 查看日志: tail -f logs/lore-master-consumer.log${NC}"
    echo ""
    echo -e "${YELLOW}📝 API测试示例:${NC}"
    echo -e "${YELLOW}curl -X POST http://localhost:8082/api/voice/transcribe \\${NC}"
    echo -e "${YELLOW}  -H 'Authorization: Bearer YOUR_TOKEN' \\${NC}"
    echo -e "${YELLOW}  -F 'audioFile=@test.mp3' \\${NC}"
    echo -e "${YELLOW}  -F 'userId=test123' \\${NC}"
    echo -e "${YELLOW}  -F 'format=mp3'${NC}"
}

# 创建日志目录
mkdir -p logs

# 主执行流程
main() {
    echo -e "${GREEN}🎤 讯飞语音识别后端部署脚本${NC}"
    echo -e "${GREEN}================================${NC}"
    
    check_env_vars
    check_java
    check_maven
    build_project
    start_service
    test_api
    show_usage
    
    echo -e "${GREEN}🎉 部署完成！${NC}"
}

# 信号处理
trap 'echo -e "${RED}❌ 部署中断${NC}"; exit 1' INT TERM

# 执行主函数
main "$@"