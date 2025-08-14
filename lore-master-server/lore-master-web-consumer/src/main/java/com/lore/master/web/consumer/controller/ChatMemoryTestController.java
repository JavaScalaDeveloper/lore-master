package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.service.consumer.chat.ChatMemoryTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * ChatMemory测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/test/chat-memory")
public class ChatMemoryTestController {

    @Resource
    private ChatMemoryTestService chatMemoryTestService;

    /**
     * 测试用户ChatMemory功能
     */
    @PostMapping("/test")
    public Result<String> testChatMemory(@RequestParam String userId) {
        log.info("测试用户ChatMemory: userId={}", userId);
        
        try {
            String result = chatMemoryTestService.testUserChatMemory(userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试ChatMemory失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 清理测试数据
     */
    @PostMapping("/clean")
    public Result<String> cleanTestData(@RequestParam String userId) {
        log.info("清理测试数据: userId={}", userId);
        
        try {
            String result = chatMemoryTestService.cleanTestData(userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("清理测试数据失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("清理失败: " + e.getMessage());
        }
    }
}
