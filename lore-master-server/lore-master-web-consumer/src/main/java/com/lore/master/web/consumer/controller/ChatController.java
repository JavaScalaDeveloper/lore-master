package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.dto.chat.ConsumerChatHistoryRequest;
import com.lore.master.service.consumer.chat.LLMChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.HashMap;
import java.util.Map;

/**
 * AI聊天控制器
 *
 * 🔥 重要说明：真正的流式响应请使用WebSocket！
 *
 * WebSocket端点：ws://localhost:8082/ws/chat
 * 配置文件：WebSocketConfig.java
 * 处理器：ChatWebSocketHandler.java
 *
 * WebSocket支持真正的实时流式数据传输，HTTP接口无法实现真正的流式响应
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final LLMChatService llmChatService;



    /**
     * HTTP流式聊天响应（注意：小程序无法真正接收流式数据）
     *
     * ⚠️  重要提示：
     * 小程序的request API无法接收真正的流式数据，会等待完整响应后才返回
     * 如需真正的流式响应，请使用WebSocket: ws://localhost:8082/ws/chat
     *
     * 此接口主要用于Web前端或测试目的
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> sendMessageStream(@RequestBody ConsumerChatHistoryRequest request) {

        String userId = request.getUserId();
        String message = request.getContent();
        log.info("接收HTTP流式聊天请求: userId={}, message={}", userId, message);
        log.warn("注意：小程序无法接收真正的HTTP流式数据，建议使用WebSocket");

        return llmChatService.sendMessageStream(message, userId);
    }

    /**
     * 发送聊天消息（同步响应）
     */
    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(@RequestBody ConsumerChatHistoryRequest request) {

        String userId = request.getUserId();
        String message = request.getContent();
        log.info("接收同步聊天请求: userId={}, message={}", userId, message);

        try {
            // 调用LLM服务
            String aiResponse = llmChatService.sendMessage(message, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", aiResponse);
            response.put("timestamp", System.currentTimeMillis());
            response.put("userId", userId);
            response.put("available", llmChatService.isAvailable());

            return Result.success("消息发送成功", response);
        } catch (Exception e) {
            log.error("同步聊天失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("消息发送失败: " + e.getMessage());
        }
    }

    /**
     * 检查LLM服务状态
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("available", llmChatService.isAvailable());
        status.put("timestamp", System.currentTimeMillis());
        status.put("service", "LLM Chat Service");

        if (llmChatService.isAvailable()) {
            return Result.success("LLM服务可用", status);
        } else {
            return Result.error("LLM服务不可用，请检查API配置");
        }
    }

}
