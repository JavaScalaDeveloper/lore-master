package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.service.consumer.chat.LLMChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * AI聊天控制器（简化版本）
 * 提供基本的聊天功能演示
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final LLMChatService llmChatService;
    /**
     * 模拟流式聊天响应
     * 支持Server-Sent Events (SSE)流式输出
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> sendMessageStream(
            @RequestParam String message,
            @RequestParam(required = false) String userId) {

        log.info("接收流式聊天请求: userId={}, message={}", userId, message);

        // 模拟AI回复的分段内容
        String[] responses = {
            "您好！我是AI助手。",
            "我正在分析您的问题：" + message,
            "根据您的描述，我建议：",
            "1. 首先明确学习目标和当前水平",
            "2. 制定合理的学习计划和时间安排",
            "3. 选择适合的学习资源和方法",
            "希望这些建议对您有帮助！如果您还有其他问题，请随时告诉我。"
        };

        return Flux.fromArray(responses)
                .delayElements(Duration.ofMillis(500))
                .doOnNext(token -> log.debug("发送流式内容: {}", token))
                .doOnError(error -> log.error("流式聊天失败: userId={}, error={}", userId, error.getMessage(), error))
                .onErrorReturn("抱歉，处理您的请求时出现了错误。");
    }

    /**
     * 发送聊天消息（同步响应）
     */
    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(
            @RequestParam String message,
            @RequestParam(required = false) String userId) {

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

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("AI聊天服务正常运行！");
    }
}
