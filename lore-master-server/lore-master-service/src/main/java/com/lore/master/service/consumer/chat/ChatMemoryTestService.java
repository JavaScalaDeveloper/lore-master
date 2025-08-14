package com.lore.master.service.consumer.chat;

import com.lore.master.data.entity.consumer.ConsumerChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * ChatMemory测试服务
 * 用于验证用户级ChatMemory功能是否正常工作
 */
@Slf4j
@Service
public class ChatMemoryTestService {

    @Resource
    private ConsumerChatMessageService chatMessageService;

    @Resource
    private UserChatMemoryService userChatMemoryService;

    /**
     * 测试用户ChatMemory功能
     */
    public String testUserChatMemory(String userId) {
        try {
            log.info("开始测试用户ChatMemory功能: userId={}", userId);

            // 1. 测试保存消息
            chatMessageService.saveUserMessage(userId, "你好，我是测试用户");
            chatMessageService.saveAssistantMessage(userId, "你好！我是AI助手，很高兴为您服务。", "test");

            // 2. 测试获取消息历史
            List<ConsumerChatMessage> messages = chatMessageService.getUserMessages(userId, 0, 10);
            log.info("获取到 {} 条历史消息", messages.size());

            // 3. 测试ChatMemory加载
            int memoryCount = userChatMemoryService.getUserMemoryMessageCount(userId);
            log.info("ChatMemory中有 {} 条消息", memoryCount);

            // 4. 测试消息总数
            long totalCount = chatMessageService.getUserMessageCount(userId);
            log.info("用户总消息数: {}", totalCount);

            return String.format("测试完成！历史消息: %d条, ChatMemory: %d条, 总消息: %d条", 
                    messages.size(), memoryCount, totalCount);

        } catch (Exception e) {
            log.error("测试ChatMemory功能失败: userId={}, error={}", userId, e.getMessage(), e);
            return "测试失败: " + e.getMessage();
        }
    }

    /**
     * 清理测试数据
     */
    public String cleanTestData(String userId) {
        try {
            userChatMemoryService.clearUserMemoryCache(userId);
            return "测试数据清理完成";
        } catch (Exception e) {
            log.error("清理测试数据失败: userId={}, error={}", userId, e.getMessage(), e);
            return "清理失败: " + e.getMessage();
        }
    }
}
