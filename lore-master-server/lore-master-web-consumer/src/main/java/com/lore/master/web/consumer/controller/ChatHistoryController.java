package com.lore.master.web.consumer.controller;

import com.lore.master.common.annotation.RequireLogin;
import com.lore.master.common.context.UserContext;
import com.lore.master.common.result.Result;
import com.lore.master.data.dto.chat.ConsumerChatHistoryRequest;
import com.lore.master.data.dto.chat.UserIdRequest;
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
    @PostMapping("/history")
    @RequireLogin
    public Result<List<ConsumerChatMessage>> getChatHistory(
            @RequestBody ConsumerChatHistoryRequest request) {
        
        String userId = UserContext.getCurrentUserId();
        log.info("获取用户聊天历史: userId={}, page={}, size={}", userId, request.getPage(), request.getSize());
        request.setUserId(userId);
        try {
            List<ConsumerChatMessage> messages = chatMessageService.getUserMessages(request);
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
    @PostMapping("/count")
    @RequireLogin
    public Result<Long> getMessageCount(@RequestBody UserIdRequest request) {
        String userId = UserContext.getCurrentUserId();
        log.info("获取用户消息总数: userId={}", userId);
        request.setUserId(userId);
        try {
            ConsumerChatHistoryRequest consumerChatHistoryRequest = new ConsumerChatHistoryRequest();
            consumerChatHistoryRequest.setUserId(userId);
            long count = chatMessageService.getUserMessageCount(consumerChatHistoryRequest);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取消息总数失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("获取消息总数失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户ChatMemory中的消息数量
     */
    @PostMapping("/memory/count")
    @RequireLogin
    public Result<Integer> getMemoryMessageCount(@RequestBody UserIdRequest request) {
        String userId = UserContext.getCurrentUserId();
        log.info("获取用户ChatMemory消息数量: userId={}", userId);
        ConsumerChatHistoryRequest consumerChatHistoryRequest = new ConsumerChatHistoryRequest();
        consumerChatHistoryRequest.setUserId(userId);
        try {
            int count = userChatMemoryService.getMemoryMessageCount(consumerChatHistoryRequest);
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
    @RequireLogin
    public Result<String> clearMemoryCache(@RequestBody UserIdRequest request) {
        String userId = UserContext.getCurrentUserId();
        log.info("清除用户ChatMemory缓存: userId={}", userId);
        request.setUserId(userId);
        ConsumerChatHistoryRequest consumerChatHistoryRequest = new ConsumerChatHistoryRequest();
        consumerChatHistoryRequest.setUserId(userId);
        try {
            userChatMemoryService.clearUserMemoryCache(consumerChatHistoryRequest);
            return Result.success("ChatMemory缓存已清除");
        } catch (Exception e) {
            log.error("清除ChatMemory缓存失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("清除ChatMemory缓存失败: " + e.getMessage());
        }
    }
}
