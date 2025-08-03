# WebSocket启动指南

## 🔧 已添加的依赖

### Maven依赖 (pom.xml)
```xml
<!-- Spring Boot Starter WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- Spring WebFlux (for Reactor support) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

## 📁 已创建的文件

### 1. WebSocket配置
- `WebSocketConfig.java` - WebSocket配置类
- 端点：`/ws/chat`
- 允许所有来源连接

### 2. WebSocket处理器
- `ChatWebSocketHandler.java` - 消息处理器
- 支持JSON消息格式
- 实时流式数据传输
- 完整的错误处理

### 3. 测试控制器
- `WebSocketTestController.java` - 测试接口
- `/api/websocket/status` - 获取连接状态
- `/api/websocket/broadcast` - 广播测试消息

## 🚀 启动步骤

### 1. 重新加载Maven依赖
```bash
# 在IDEA中
1. 右键点击 pom.xml
2. 选择 "Maven" -> "Reload project"
3. 等待依赖下载完成
```

### 2. 启动应用
```bash
# 启动Spring Boot应用
mvn spring-boot:run

# 或者在IDEA中运行主类
```

### 3. 验证WebSocket服务
```bash
# 检查WebSocket状态
curl http://localhost:8082/api/websocket/status

# 期望响应
{
  "success": true,
  "message": "WebSocket状态获取成功",
  "data": {
    "endpoint": "/ws/chat",
    "activeConnections": 0,
    "status": "running",
    "protocol": "ws://localhost:8082/ws/chat"
  }
}
```

## 🧪 测试WebSocket连接

### 1. 使用浏览器测试
```javascript
// 在浏览器控制台中运行
const ws = new WebSocket('ws://localhost:8082/ws/chat');

ws.onopen = function() {
    console.log('WebSocket连接成功');
    
    // 发送测试消息
    ws.send(JSON.stringify({
        message: "测试消息",
        userId: "test-user",
        messageId: "test-123"
    }));
};

ws.onmessage = function(event) {
    console.log('收到消息:', event.data);
};

ws.onclose = function() {
    console.log('WebSocket连接关闭');
};

ws.onerror = function(error) {
    console.error('WebSocket错误:', error);
};
```

### 2. 使用curl测试（HTTP升级）
```bash
curl -i -N \
  -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  -H "Sec-WebSocket-Key: test" \
  -H "Sec-WebSocket-Version: 13" \
  http://localhost:8082/ws/chat
```

### 3. 广播测试
```bash
# 发送广播消息
curl http://localhost:8082/api/websocket/broadcast

# 如果有WebSocket连接，会收到广播消息
```

## 🔍 故障排除

### 问题1: 依赖下载失败
**解决方案**:
```bash
# 清理并重新下载
mvn clean install -U

# 或者删除本地仓库中的相关依赖
rm -rf ~/.m2/repository/org/springframework/boot/spring-boot-starter-websocket
```

### 问题2: 启动时找不到Bean
**检查**:
1. 确保`@EnableWebSocket`注解存在
2. 确保`ChatWebSocketHandler`有`@Component`注解
3. 确保包扫描路径正确

### 问题3: WebSocket连接失败
**检查**:
1. 端口8082是否被占用
2. 防火墙是否阻止连接
3. 应用是否正常启动

### 问题4: 消息处理异常
**检查后端日志**:
```bash
# 查看详细日志
tail -f logs/application.log

# 或者在IDEA控制台查看
```

## 📊 预期日志输出

### 启动成功日志
```
INFO  - WebSocket配置已加载
INFO  - ChatWebSocketHandler已注册
INFO  - WebSocket端点: /ws/chat
INFO  - 应用启动完成，端口: 8082
```

### 连接成功日志
```
INFO  - WebSocket连接建立: sessionId=xxx
INFO  - 收到WebSocket消息: sessionId=xxx, message={"message":"测试","userId":"test"}
DEBUG - 发送流式数据块: sessionId=xxx, chunk=这是
DEBUG - 发送流式数据块: sessionId=xxx, chunk=AI的
DEBUG - 发送流式数据块: sessionId=xxx, chunk=回复
INFO  - 流式响应完成: sessionId=xxx
```

## 🎯 成功标准

### 最低要求
- ✅ 应用正常启动
- ✅ WebSocket端点可访问
- ✅ 状态接口返回正常

### 完整功能
- ✅ WebSocket连接成功
- ✅ 消息发送接收正常
- ✅ 流式数据传输工作
- ✅ 错误处理正常

## 🔧 配置说明

### WebSocket端点
- **URL**: `ws://localhost:8082/ws/chat`
- **协议**: WebSocket
- **消息格式**: JSON

### 消息格式
```json
// 发送格式
{
  "message": "用户消息内容",
  "userId": "用户ID",
  "messageId": "消息ID"
}

// 接收格式
"[STREAM_START]"     // 流式开始
"实际内容块"          // 流式内容
"[STREAM_END]"       // 流式结束
"[STREAM_ERROR]错误" // 错误信息
```

## 🚀 下一步

启动成功后：
1. 在小程序中测试WebSocket连接
2. 观察右上角状态指示器
3. 发送消息测试流式响应
4. 检查控制台日志确认功能正常

如果遇到问题，请提供：
1. 启动日志
2. 错误信息
3. 网络状态
4. 浏览器控制台输出
