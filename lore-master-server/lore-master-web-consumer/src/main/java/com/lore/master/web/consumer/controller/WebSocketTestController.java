package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.web.consumer.handler.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
public class WebSocketTestController {

    private final ChatWebSocketHandler chatWebSocketHandler;

    /**
     * 获取WebSocket状态
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getWebSocketStatus() {
        log.info("获取WebSocket状态");
        
        Map<String, Object> status = new HashMap<>();
        status.put("endpoint", "/ws/chat");
        status.put("activeConnections", chatWebSocketHandler.getActiveConnectionCount());
        status.put("status", "running");
        status.put("protocol", "ws://localhost:8082/ws/chat");
        
        return Result.success("WebSocket状态获取成功", status);
    }

    /**
     * 广播测试消息
     */
    @GetMapping("/broadcast")
    public Result<String> broadcastTestMessage() {
        log.info("广播测试消息");
        
        try {
            String testMessage = "这是一条测试广播消息，时间：" + System.currentTimeMillis();
            chatWebSocketHandler.broadcastMessage(testMessage);
            
            return Result.success("广播消息发送成功", testMessage);
        } catch (Exception e) {
            log.error("广播消息失败", e);
            return Result.error("广播消息失败: " + e.getMessage());
        }
    }
}
