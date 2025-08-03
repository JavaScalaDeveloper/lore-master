# WebSocket流式响应使用指南

## 🎯 重要说明

**WebSocket是小程序中唯一能实现真正流式响应的方案！**

HTTP接口（包括/stream、/miniapp-stream等）在小程序中都无法实现真正的流式接收，只能等待完整响应。

## 🔗 WebSocket连接信息

### 连接地址
```
ws://localhost:8082/ws/chat
```

### 相关文件
- **配置文件**: `WebSocketConfig.java`
- **处理器**: `ChatWebSocketHandler.java`
- **测试控制器**: `WebSocketTestController.java`

## 📡 使用方法

### 1. 获取WebSocket信息
```bash
curl http://localhost:8082/api/chat/websocket-info
```

**响应示例**:
```json
{
  "success": true,
  "message": "WebSocket连接信息",
  "data": {
    "endpoint": "ws://localhost:8082/ws/chat",
    "protocol": "WebSocket",
    "messageFormat": "JSON",
    "features": [
      "真正的实时流式响应",
      "双向通信",
      "低延迟",
      "支持小程序"
    ],
    "example": {
      "message": "你好",
      "userId": "user123",
      "messageId": "msg456"
    }
  }
}
```

### 2. 小程序中使用WebSocket

```typescript
// 连接WebSocket
connectSocket({
  url: 'ws://localhost:8082/ws/chat',
  success: () => console.log('WebSocket连接成功')
})

// 监听连接打开
onSocketOpen(() => {
  console.log('WebSocket连接已建立')
})

// 监听消息
onSocketMessage((res) => {
  console.log('收到消息:', res.data)
  // 处理流式数据
  handleStreamMessage(res.data)
})

// 发送消息
sendSocketMessage({
  data: JSON.stringify({
    message: "用户输入的消息",
    userId: "user123",
    messageId: "msg456"
  })
})
```

### 3. 浏览器中测试WebSocket

```javascript
const ws = new WebSocket('ws://localhost:8082/ws/chat');

ws.onopen = function() {
    console.log('WebSocket连接成功');
    
    // 发送测试消息
    ws.send(JSON.stringify({
        message: "测试流式响应",
        userId: "test-user",
        messageId: "test-123"
    }));
};

ws.onmessage = function(event) {
    console.log('收到流式数据:', event.data);
    
    // 处理特殊标记
    if (event.data === '[STREAM_START]') {
        console.log('流式响应开始');
    } else if (event.data === '[STREAM_END]') {
        console.log('流式响应结束');
    } else if (event.data.startsWith('[STREAM_ERROR]')) {
        console.error('流式响应错误:', event.data);
    } else {
        // 处理实际内容
        console.log('内容块:', event.data);
    }
};
```

## 📋 消息协议

### 发送格式（客户端 → 服务器）
```json
{
  "message": "用户消息内容",
  "userId": "用户ID（可选）",
  "messageId": "消息ID（可选）"
}
```

### 接收格式（服务器 → 客户端）
```
[STREAM_START]        # 流式响应开始标记
实际内容块1           # AI生成的内容块
实际内容块2           # 继续的内容块
...                   # 更多内容块
[STREAM_END]          # 流式响应结束标记
[STREAM_ERROR]错误信息 # 错误标记（如果发生错误）
[ERROR]错误信息       # 一般错误标记
```

## 🧪 测试步骤

### 1. 检查WebSocket状态
```bash
curl http://localhost:8082/api/websocket/status
```

### 2. 发送广播测试
```bash
curl http://localhost:8082/api/websocket/broadcast
```

### 3. 在小程序中测试
1. 观察右上角状态：🟢 WS = 连接成功
2. 发送消息测试流式响应
3. 观察控制台日志确认数据接收

## 🔧 HTTP接口说明

虽然HTTP接口无法在小程序中实现真正的流式响应，但仍然保留用于Web前端和测试：

### 流式接口（仅Web有效）
```bash
curl -X POST "http://localhost:8082/api/chat/stream" \
  -d "message=测试消息&userId=test"
```

### 同步接口（小程序可用）
```bash
curl -X POST "http://localhost:8082/api/chat/send" \
  -d "message=测试消息&userId=test"
```

### 状态检查
```bash
curl http://localhost:8082/api/chat/status
```

## 🎯 最佳实践

### 小程序开发
1. **优先使用WebSocket**：真正的流式响应
2. **智能降级**：WebSocket失败时使用HTTP同步接口
3. **状态显示**：显示连接状态（🟢 WS / 🔴 HTTP）
4. **错误处理**：完善的重连和错误恢复机制

### 错误处理
```typescript
// WebSocket错误处理
onSocketError((err) => {
  console.error('WebSocket错误:', err)
  // 降级到HTTP接口
  fallbackToHttpAPI()
})

// 连接关闭处理
onSocketClose(() => {
  console.log('WebSocket连接关闭')
  // 尝试重连
  setTimeout(reconnectWebSocket, 3000)
})
```

## 📊 性能对比

| 方案 | 实时性 | 小程序支持 | 实现复杂度 | 推荐度 |
|------|--------|------------|------------|--------|
| WebSocket | ⭐⭐⭐⭐⭐ | ✅ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| HTTP Stream | ⭐⭐⭐⭐⭐ | ❌ | ⭐⭐⭐⭐ | ⭐⭐ |
| HTTP Sync | ⭐⭐ | ✅ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |

## 🚀 总结

- ✅ **WebSocket**: 小程序中唯一的真正流式方案
- ❌ **HTTP Stream**: 小程序无法接收，仅Web有效
- ✅ **HTTP Sync**: 备用方案，配合打字机效果

**推荐架构**: WebSocket主要方案 + HTTP同步备用方案 + 智能降级策略

现在你可以通过 `ws://localhost:8082/ws/chat` 享受真正的流式响应了！
