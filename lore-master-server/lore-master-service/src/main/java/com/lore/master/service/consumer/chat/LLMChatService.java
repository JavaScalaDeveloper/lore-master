package com.lore.master.service.consumer.chat;

import reactor.core.publisher.Flux;

/**
 * LLM聊天服务接口
 */
public interface LLMChatService {

    /**
     * 发送消息并获取流式响应
     * @param message 用户消息
     * @param userId 用户ID（可选，用于上下文管理）
     * @return 流式响应
     */
    Flux<String> sendMessageStream(String message, String userId);

    /**
     * 发送消息并获取完整响应
     * @param message 用户消息
     * @param userId 用户ID（可选）
     * @return 完整响应
     */
    String sendMessage(String message, String userId);

    /**
     * 检查服务是否可用
     * @return 是否可用
     */
    boolean isAvailable();
}
