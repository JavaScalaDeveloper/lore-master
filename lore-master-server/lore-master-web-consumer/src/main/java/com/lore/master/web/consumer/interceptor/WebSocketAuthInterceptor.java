package com.lore.master.web.consumer.interceptor;

import com.lore.master.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket认证拦截器
 * 在WebSocket握手阶段验证token并提取用户信息
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        log.info("WebSocket握手开始: {}", request.getURI());
        
        try {
            // 从URL参数中获取token
            URI uri = request.getURI();
            String query = uri.getQuery();
            String token = null;
            String userId = null;
            
            if (query != null) {
                Map<String, String> queryParams = UriComponentsBuilder.fromUriString("?" + query)
                        .build()
                        .getQueryParams()
                        .toSingleValueMap();
                
                token = queryParams.get("token");
                userId = queryParams.get("userId");
                
                log.info("从URL参数获取: token={}, userId={}", 
                    token != null ? "***" : "null", userId);
            }
            
            // 验证token
            if (token != null && !token.isEmpty()) {
                boolean isValid = JwtUtil.validateToken(token);
                log.info("Token验证结果: {}", isValid);
                
                if (isValid) {
                    // 从token中解析用户ID
                    String tokenUserId = JwtUtil.getUserIdFromToken(token);
                    log.info("从Token解析出用户ID: {}", tokenUserId);
                    
                    // 将认证信息存储到WebSocket会话属性中
                    attributes.put("authenticated", true);
                    attributes.put("token", token);
                    attributes.put("userId", tokenUserId != null ? tokenUserId : userId);
                    
                    log.info("WebSocket认证成功: userId={}", tokenUserId != null ? tokenUserId : userId);
                    return true;
                } else {
                    log.warn("WebSocket认证失败: token无效");
                }
            } else {
                log.warn("WebSocket认证失败: 未提供token");
            }
            
            // 认证失败，但允许连接（在消息处理时再次验证）
            attributes.put("authenticated", false);
            attributes.put("userId", userId != null ? userId : "anonymous");
            log.info("WebSocket连接允许但未认证: userId={}", userId != null ? userId : "anonymous");
            
            return true;
            
        } catch (Exception e) {
            log.error("WebSocket握手认证异常", e);
            // 发生异常时也允许连接，但标记为未认证
            attributes.put("authenticated", false);
            attributes.put("userId", "anonymous");
            return true;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket握手完成但有异常", exception);
        } else {
            log.info("WebSocket握手成功完成");
        }
    }
}
