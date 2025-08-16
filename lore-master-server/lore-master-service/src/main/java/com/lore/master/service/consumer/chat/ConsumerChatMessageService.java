package com.lore.master.service.consumer.chat;

import com.lore.master.data.dto.chat.ConsumerChatHistoryRequest;
import com.lore.master.data.entity.consumer.ConsumerChatMessage;
import java.util.List;

/**
 * 用户聊天消息服务接口
 */
public interface ConsumerChatMessageService {

    /**
     * 保存用户消息
     */
    ConsumerChatMessage saveUserMessage(ConsumerChatHistoryRequest request);

    /**
     * 保存AI响应消息
     */
    ConsumerChatMessage saveAssistantMessage(ConsumerChatHistoryRequest request, String content, String modelName);

    /**
     * 获取用户的最近消息（用于ChatMemory）
     */
    List<ConsumerChatMessage> getRecentMessages(ConsumerChatHistoryRequest request);

    /**
     * 获取用户的所有消息（分页）
     */
    List<ConsumerChatMessage> getUserMessages(ConsumerChatHistoryRequest request);

    /**
     * 获取用户消息总数
     */
    long getUserMessageCount(ConsumerChatHistoryRequest request);

    /**
     * 生成消息ID
     */
    String generateMessageId();
}