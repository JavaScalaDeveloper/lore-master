package com.lore.master.service.consumer.chat;

import com.lore.master.data.dto.chat.ConsumerChatHistoryRequest;
import dev.langchain4j.memory.ChatMemory;

/**
 * 用户ChatMemory管理服务接口
 */
public interface UserChatMemoryService {

    /**
     * 获取用户的ChatMemory
     */
    ChatMemory getUserChatMemory(String userId);

    /**
     * 添加用户消息到ChatMemory
     */
    void addUserMessage(String userId, String content);

    /**
     * 添加AI响应到ChatMemory
     */
    void addAssistantMessage(String userId, String content);

    /**
     * 清除用户ChatMemory缓存（当需要重新加载时）
     */
    void clearUserMemoryCache(ConsumerChatHistoryRequest request);

    /**
     * 清除所有ChatMemory缓存
     */
    void clearAllMemoryCache();

    /**
     * 获取用户记忆消息数
     */
    int getMemoryMessageCount(ConsumerChatHistoryRequest request);
}