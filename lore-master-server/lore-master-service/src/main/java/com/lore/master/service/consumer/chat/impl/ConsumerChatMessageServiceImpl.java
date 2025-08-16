package com.lore.master.service.consumer.chat.impl;

import com.lore.master.data.dto.chat.ConsumerChatHistoryRequest;
import com.lore.master.data.entity.consumer.ConsumerChatMessage;
import com.lore.master.data.repository.consumer.ConsumerChatMessageRepository;
import com.lore.master.service.consumer.chat.ConsumerChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 用户聊天消息服务实现类
 */
@Slf4j
@Service
public class ConsumerChatMessageServiceImpl implements ConsumerChatMessageService {

    @Resource
    @Qualifier("consumerChatMessageRepository")
    private ConsumerChatMessageRepository consumerChatMessageRepository;

    /**
     * 保存用户消息
     */
    @Transactional
    public ConsumerChatMessage saveUserMessage(ConsumerChatHistoryRequest request) {
        String userId = request.getUserId();
        String content = request.getContent();
        String sessionId = "session_" + userId; // 简化：每个用户一个会话

        ConsumerChatMessage message = ConsumerChatMessage.builder()
                .messageId(generateMessageId())
                .userId(userId)
                .sessionId(sessionId)
                .role(ConsumerChatMessage.MessageRole.user)
                .content(content)
                .createTime(LocalDateTime.now())
                .build();

        ConsumerChatMessage saved = consumerChatMessageRepository.save(message);
        log.info("保存用户消息: userId={}, messageId={}", userId, saved.getMessageId());
        return saved;
    }

    /**
     * 保存AI响应消息
     */
    @Transactional
    public ConsumerChatMessage saveAssistantMessage(ConsumerChatHistoryRequest request, String content, String modelName) {
        String userId = request.getUserId();
        String sessionId = "session_" + userId; // 简化：每个用户一个会话

        ConsumerChatMessage message = ConsumerChatMessage.builder()
                .messageId(generateMessageId())
                .userId(userId)
                .sessionId(sessionId)
                .role(ConsumerChatMessage.MessageRole.assistant)
                .content(content)
                .modelName(modelName)
                .createTime(LocalDateTime.now())
                .build();

        ConsumerChatMessage saved = consumerChatMessageRepository.save(message);
        log.info("保存AI响应消息: userId={}, messageId={}, contentLength={}",
                userId, saved.getMessageId(), content.length());
        return saved;
    }

    /**
     * 获取用户的最近消息（用于ChatMemory）
     */
    public List<ConsumerChatMessage> getRecentMessages(ConsumerChatHistoryRequest request) {
        String userId = request.getUserId();
        int maxMessages = request.getMaxMessages() != null ? request.getMaxMessages() : 20; // 默认值
        String sessionId = "session_" + userId;
        Pageable pageable = PageRequest.of(0, maxMessages);
        List<ConsumerChatMessage> messages = consumerChatMessageRepository.findRecentMessages(userId, sessionId, pageable);

        // 按时间正序返回（最早的在前面，符合ChatMemory的顺序）
        Collections.reverse(messages);
        log.debug("获取用户最近消息: userId={}, messageCount={}", userId, messages.size());
        return messages;
    }

    /**
     * 获取用户的所有消息（分页）
     */
    public List<ConsumerChatMessage> getUserMessages(ConsumerChatHistoryRequest request) {
        String userId = request.getUserId();
        String sessionId = "session_" + userId;
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return consumerChatMessageRepository.findByUserIdOrderByCreateTimeAsc(userId, pageable);
    }

    /**
     * 获取用户消息总数
     */
    public long getUserMessageCount(ConsumerChatHistoryRequest request) {
        String userId = request.getUserId();
        String sessionId = "session_" + userId;
        return consumerChatMessageRepository.countByUserIdAndSessionId(userId, sessionId);
    }

    /**
     * 生成消息ID
     */
    public String generateMessageId() {
        return "msg_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
