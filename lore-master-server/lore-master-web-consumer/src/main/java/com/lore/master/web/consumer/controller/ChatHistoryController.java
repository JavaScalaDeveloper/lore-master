package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.entity.consumer.ConsumerChatMessage;
import com.lore.master.service.consumer.chat.ConsumerChatMessageService;
import com.lore.master.service.consumer.chat.UserChatMemoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 聊天历史控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

    @Resource
    private ConsumerChatMessageService chatMessageService;

    @Resource
    private UserChatMemoryService userChatMemoryService;

    /**
     * 获取用户的聊天历史
     */
    @GetMapping("/history")
    public Result<List<ConsumerChatMessage>> getChatHistory(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("获取用户聊天历史: userId={}, page={}, size={}", userId, page, size);
        
        try {
            List<ConsumerChatMessage> messages = chatMessageService.getUserMessages(userId, page, size);
            log.info("获取到 {} 条聊天记录", messages.size());
            return Result.success(messages);
        } catch (Exception e) {
            log.error("获取聊天历史失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("获取聊天历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户消息总数
     */
    @GetMapping("/count")
    public Result<Long> getMessageCount(@RequestParam String userId) {
        log.info("获取用户消息总数: userId={}", userId);
        
        try {
            long count = chatMessageService.getUserMessageCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取消息总数失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("获取消息总数失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户ChatMemory中的消息数量
     */
    @GetMapping("/memory/count")
    public Result<Integer> getMemoryMessageCount(@RequestParam String userId) {
        log.info("获取用户ChatMemory消息数量: userId={}", userId);
        
        try {
            int count = userChatMemoryService.getUserMemoryMessageCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取ChatMemory消息数量失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("获取ChatMemory消息数量失败: " + e.getMessage());
        }
    }

    /**
     * 清除用户ChatMemory缓存
     */
    @PostMapping("/memory/clear")
    public Result<String> clearMemoryCache(@RequestParam String userId) {
        log.info("清除用户ChatMemory缓存: userId={}", userId);
        
        try {
            userChatMemoryService.clearUserMemoryCache(userId);
            return Result.success("ChatMemory缓存已清除");
        } catch (Exception e) {
            log.error("清除ChatMemory缓存失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("清除ChatMemory缓存失败: " + e.getMessage());
        }
    }
}
