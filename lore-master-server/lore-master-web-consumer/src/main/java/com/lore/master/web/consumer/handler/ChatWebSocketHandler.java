package com.lore.master.web.consumer.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lore.master.service.consumer.chat.LLMChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final LLMChatService llmChatService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储活跃的WebSocket会话
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket连接建立: sessionId={}", sessionId);
        
        // 发送连接成功消息
        sendMessage(session, "连接成功！可以开始聊天了。");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();
        
        log.info("收到WebSocket消息: sessionId={}, message={}", sessionId, payload);
        
        try {
            // 解析消息
            JsonNode jsonNode = objectMapper.readTree(payload);
            String userMessage = jsonNode.get("message").asText();
            String userId = jsonNode.has("userId") ? jsonNode.get("userId").asText() : "anonymous";
            String messageId = jsonNode.has("messageId") ? jsonNode.get("messageId").asText() : null;
            
            log.info("解析消息: userId={}, message={}, messageId={}", userId, userMessage, messageId);
            
            // 发送开始标记
            sendMessage(session, "[STREAM_START]");
            
            // 调用流式服务并实时发送
            llmChatService.sendMessageStream(userMessage, userId)
                .doOnNext(chunk -> {
                    try {
                        log.debug("发送流式数据块: sessionId={}, chunk={}", sessionId, chunk);
                        sendMessage(session, chunk);
                    } catch (Exception e) {
                        log.error("发送流式数据失败: sessionId={}", sessionId, e);
                    }
                })
                .doOnComplete(() -> {
                    try {
                        log.info("流式响应完成: sessionId={}", sessionId);
                        sendMessage(session, "[STREAM_END]");
                    } catch (Exception e) {
                        log.error("发送完成标记失败: sessionId={}", sessionId, e);
                    }
                })
                .doOnError(error -> {
                    try {
                        log.error("流式响应错误: sessionId={}", sessionId, error);
                        sendMessage(session, "[STREAM_ERROR]" + error.getMessage());
                    } catch (Exception e) {
                        log.error("发送错误标记失败: sessionId={}", sessionId, e);
                    }
                })
                .subscribe();
                
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: sessionId={}", sessionId, e);
            sendMessage(session, "[ERROR]消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket传输错误: sessionId={}", sessionId, exception);
        sessions.remove(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        log.info("WebSocket连接关闭: sessionId={}, status={}", sessionId, closeStatus);
    }

    /**
     * 发送消息到WebSocket客户端
     */
    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            } else {
                log.warn("WebSocket会话已关闭，无法发送消息: sessionId={}", session.getId());
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 广播消息到所有连接的客户端
     */
    public void broadcastMessage(String message) {
        sessions.values().forEach(session -> sendMessage(session, message));
    }

    /**
     * 获取活跃连接数
     */
    public int getActiveConnectionCount() {
        return sessions.size();
    }
}
