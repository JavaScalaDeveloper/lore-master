package com.lore.master.service.consumer.chat;

import com.lore.master.data.entity.consumer.ConsumerChatMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户ChatMemory管理服务
 */
@Slf4j
@Service
public class UserChatMemoryService {

    @Resource
    private ConsumerChatMessageService chatMessageService;

    // 缓存用户的ChatMemory，避免频繁从数据库加载
    private final Map<String, ChatMemory> userMemoryCache = new ConcurrentHashMap<>();

    // 默认最大记忆消息数
    private static final int DEFAULT_MAX_MESSAGES = 20;

    /**
     * 获取用户的ChatMemory
     */
    public ChatMemory getUserChatMemory(String userId) {
        return userMemoryCache.computeIfAbsent(userId, this::loadChatMemoryFromDatabase);
    }

    /**
     * 从数据库加载ChatMemory
     */
    private ChatMemory loadChatMemoryFromDatabase(String userId) {
        log.info("为用户 {} 从数据库加载ChatMemory", userId);
        
        // 创建ChatMemory，限制最大消息数
        ChatMemory memory = MessageWindowChatMemory.withMaxMessages(DEFAULT_MAX_MESSAGES);

        // 获取用户的最近消息
        List<ConsumerChatMessage> recentMessages = chatMessageService.getRecentMessages(userId, DEFAULT_MAX_MESSAGES);

        // 将消息添加到ChatMemory
        for (ConsumerChatMessage msg : recentMessages) {
            switch (msg.getRole()) {
                case user:
                    memory.add(UserMessage.from(msg.getContent()));
                    break;
                case assistant:
                    memory.add(AiMessage.from(msg.getContent()));
                    break;
                case system:
                    memory.add(SystemMessage.from(msg.getContent()));
                    break;
            }
        }

        log.info("为用户 {} 加载了 {} 条历史消息到ChatMemory", userId, recentMessages.size());
        return memory;
    }

    /**
     * 添加用户消息到ChatMemory
     */
    public void addUserMessage(String userId, String content) {
        ChatMemory memory = getUserChatMemory(userId);
        memory.add(UserMessage.from(content));
        log.debug("添加用户消息到ChatMemory: userId={}", userId);
    }

    /**
     * 添加AI响应到ChatMemory
     */
    public void addAssistantMessage(String userId, String content) {
        ChatMemory memory = getUserChatMemory(userId);
        memory.add(AiMessage.from(content));
        log.debug("添加AI响应到ChatMemory: userId={}", userId);
    }

    /**
     * 清除用户ChatMemory缓存（当需要重新加载时）
     */
    public void clearUserMemoryCache(String userId) {
        userMemoryCache.remove(userId);
        log.info("清除用户ChatMemory缓存: userId={}", userId);
    }

    /**
     * 清除所有ChatMemory缓存
     */
    public void clearAllMemoryCache() {
        userMemoryCache.clear();
        log.info("清除所有ChatMemory缓存");
    }

    /**
     * 获取用户ChatMemory的消息数量
     */
    public int getUserMemoryMessageCount(String userId) {
        ChatMemory memory = getUserChatMemory(userId);
        return memory.messages().size();
    }
}
