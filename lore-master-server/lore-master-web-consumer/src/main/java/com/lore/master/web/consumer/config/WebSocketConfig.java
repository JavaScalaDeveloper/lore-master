package com.lore.master.web.consumer.config;

import com.lore.master.web.consumer.handler.ChatWebSocketHandler;
import com.lore.master.web.consumer.interceptor.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(webSocketAuthInterceptor, new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*") // 允许所有来源，生产环境应该限制
                .setHandshakeTimeout(5000) // 设置握手超时时间为5秒
                .setAllowedOrigins("*"); // 允许所有来源，生产环境应该限制
    }
}