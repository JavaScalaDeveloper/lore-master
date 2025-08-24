# downloadFile流式方案测试指南

## 🎯 测试目标

验证小程序能否通过`downloadFile` API接收后端Flux流式数据

## 🔧 当前配置

### 后端接口
- **URL**: `GET /api/chat/miniapp-stream`
- **参数**: `message`, `userId`
- **响应**: `Flux<String>` 流式数据
- **Headers**: 
  - `Transfer-Encoding: chunked`
  - `Content-Type: text/plain; charset=utf-8`
  - `Cache-Control: no-cache`

### 前端实现
- **方法**: `downloadFile` API
- **流式监听**: `onProgressUpdate`
- **部分读取**: `tryReadPartialContent`
- **完整读取**: 下载完成后读取文件

## 🧪 测试步骤

### 1. 启动后端服务
```bash
# 确保后端运行在 localhost:8082
curl -X GET "http://localhost:8082/api/chat/miniapp-stream?message=你好&userId=test"
```

### 2. 在小程序中测试
1. 打开小程序开发工具
2. 进入聊天页面
3. 发送测试消息："测试流式响应"
4. 观察控制台日志

### 3. 关键日志检查

**期望看到的日志**：
```
=== 开始测试downloadFile流式方案 ===
发送消息到后端: {message: "测试流式响应", userId: "anonymous", wsConnected: false}
开始流式下载: {message: "测试流式响应", userId: "anonymous", tempFilePath: "stream_xxx.txt"}
downloadFile URL: http://localhost:8082/api/chat/miniapp-stream?message=...
下载进度: 10 1024 5120
读取部分内容: 100 字符
下载进度: 50 2048 5120
读取部分内容: 200 字符
下载完成: {statusCode: 200, tempFilePath: "stream_xxx.txt"}
读取到流式内容长度: 500
流式内容预览: 这是AI的回复内容...
=== downloadFile方案成功！ ===
```

**如果失败，可能看到**：
```
下载失败: {errMsg: "downloadFile:fail"}
=== downloadFile方案失败，使用备用同步接口 ===
```

## 🔍 调试检查点

### 1. 网络请求检查
- 打开开发工具 Network 面板
- 查看是否有对 `/api/chat/miniapp-stream` 的请求
- 检查请求状态码是否为 200
- 查看响应头是否包含 `Transfer-Encoding: chunked`

### 2. 文件系统检查
- 检查临时文件是否创建成功
- 验证文件读取权限
- 确认文件内容不为空

### 3. 流式数据检查
- 观察 `onProgressUpdate` 是否被调用
- 检查 `totalBytesWritten` 是否递增
- 验证部分内容读取是否成功

## 🚨 常见问题及解决方案

### 问题1: 下载失败
**现象**: `downloadFile:fail`
**原因**: 
- 后端服务未启动
- URL格式错误
- 网络连接问题

**解决**:
```bash
# 检查后端服务
curl -X GET "http://localhost:8082/api/chat/test"

# 检查流式接口
curl -X GET "http://localhost:8082/api/chat/miniapp-stream?message=test&userId=test"
```

### 问题2: 文件读取失败
**现象**: `读取文件失败`
**原因**:
- 文件权限问题
- 文件路径错误
- 文件为空

**解决**:
```typescript
// 检查文件是否存在
fs.access({
  path: tempFilePath,
  success: () => console.log('文件存在'),
  fail: () => console.log('文件不存在')
})
```

### 问题3: 内容为空
**现象**: `内容为空，使用备用方案`
**原因**:
- 后端流式响应为空
- 编码问题
- 响应格式错误

**解决**:
```bash
# 直接测试后端接口
curl -X GET "http://localhost:8082/api/chat/miniapp-stream?message=你好&userId=test" -v
```

### 问题4: 流式效果不明显
**现象**: 看起来像一次性显示
**原因**:
- 响应太快
- 延迟设置太小
- 部分读取失败

**解决**:
```java
// 后端增加延迟
.delayElements(Duration.ofMillis(500))
```

## 🎯 成功标准

### 最低标准（基本成功）
- ✅ downloadFile 成功下载
- ✅ 文件读取成功
- ✅ 内容不为空
- ✅ 显示AI回复

### 理想标准（流式效果）
- ✅ `onProgressUpdate` 被多次调用
- ✅ 部分内容读取成功
- ✅ 看到内容逐步增加
- ✅ 真正的流式显示效果

## 🔧 调试技巧

### 1. 增加详细日志
```typescript
console.log('=== downloadFile详细调试 ===')
console.log('URL:', url)
console.log('Headers:', headers)
console.log('FilePath:', tempFilePath)
```

### 2. 监控下载进度
```typescript
downloadTask.onProgressUpdate((res) => {
  console.log(`下载进度: ${res.progress}% (${res.totalBytesWritten}/${res.totalBytesExpectedToWrite})`)
  
  // 每次进度更新都尝试读取
  if (res.totalBytesWritten > lastReadBytes) {
    tryReadPartialContent(tempFilePath, aiMessageId)
    lastReadBytes = res.totalBytesWritten
  }
})
```

### 3. 文件内容检查
```typescript
// 读取文件的十六进制内容
fs.readFile({
  filePath: tempFilePath,
  success: (res) => {
    console.log('文件大小:', res.data.length)
    console.log('文件开头:', res.data.substring(0, 50))
    console.log('文件结尾:', res.data.substring(res.data.length - 50))
  }
})
```

## 🚀 下一步优化

如果基本功能工作：
1. **优化流式显示**: 改进部分读取逻辑
2. **添加错误恢复**: 处理网络中断
3. **性能优化**: 减少文件读取频率
4. **用户体验**: 添加更好的加载指示器

如果基本功能不工作：
1. **检查网络**: 确认后端接口可访问
2. **简化测试**: 先用简单的文本响应测试
3. **备用方案**: 使用WebSocket或轮询
4. **问题反馈**: 提供详细的错误日志

## 📊 测试结果记录

请记录测试结果：

- [ ] 后端接口可访问
- [ ] downloadFile 开始下载
- [ ] onProgressUpdate 被调用
- [ ] 文件创建成功
- [ ] 文件读取成功
- [ ] 内容不为空
- [ ] 看到流式效果
- [ ] 最终显示完整内容

**测试时间**: ___________
**测试结果**: ___________
**遇到的问题**: ___________
**解决方案**: ___________
