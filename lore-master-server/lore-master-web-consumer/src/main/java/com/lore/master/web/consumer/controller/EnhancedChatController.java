package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.dto.chat.ConsumerChatHistoryRequest;
import com.lore.master.service.consumer.chat.EnhancedLLMChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 增强AI聊天控制器
 * 支持Function Call和RAG功能
 */
@Slf4j
@RestController
@RequestMapping("/api/enhanced-chat")
@RequiredArgsConstructor
public class EnhancedChatController {

    private final EnhancedLLMChatService enhancedLLMChatService;

    /**
     * 增强聊天接口（同步）
     * 支持Function Call和RAG
     */
    @PostMapping("/send")
    public Result<EnhancedLLMChatService.ChatResponse> sendEnhancedMessage(
            @RequestBody ConsumerChatHistoryRequest request,
            @RequestParam(required = false, defaultValue = "true") boolean enableFunctionCall,
            @RequestParam(required = false, defaultValue = "true") boolean enableRAG) {
        
        String userId = request.getUserId();
        String message = request.getContent();
        log.info("接收增强聊天请求: userId={}, message={}, FC={}, RAG={}", 
                userId, message, enableFunctionCall, enableRAG);
        
        try {
            EnhancedLLMChatService.ChatResponse response = enhancedLLMChatService.sendEnhancedMessage(
                    message, userId, enableFunctionCall, enableRAG);
            
            return Result.success("增强聊天响应成功", response);
        } catch (Exception e) {
            log.error("增强聊天失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("增强聊天失败: " + e.getMessage());
        }
    }

    /**
     * 增强聊天接口（流式）
     * 支持Function Call和RAG的流式输出
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> sendEnhancedMessageStream(
            @RequestBody ConsumerChatHistoryRequest request,
            @RequestParam(required = false, defaultValue = "true") boolean enableFunctionCall,
            @RequestParam(required = false, defaultValue = "true") boolean enableRAG) {
        
        String userId = request.getUserId();
        String message = request.getContent();
        log.info("接收流式增强聊天请求: userId={}, message={}, FC={}, RAG={}", 
                userId, message, enableFunctionCall, enableRAG);
        
        return enhancedLLMChatService.sendEnhancedMessageStream(message, userId, enableFunctionCall, enableRAG)
                .doOnNext(token -> log.debug("流式输出: {}", token))
                .doOnError(error -> log.error("流式增强聊天失败: userId={}, error={}", userId, error.getMessage(), error))
                .onErrorReturn("抱歉，处理您的请求时出现了错误。");
    }

    /**
     * 仅Function Call演示
     */
    @PostMapping("/function-call")
    public Result<EnhancedLLMChatService.ChatResponse> functionCallDemo(
            @RequestBody ConsumerChatHistoryRequest request) {
        
        String userId = request.getUserId();
        String message = request.getContent();
        log.info("Function Call演示: userId={}, message={}", userId, message);
        
        try {
            EnhancedLLMChatService.ChatResponse response = enhancedLLMChatService.sendEnhancedMessage(
                    message, userId, true, false);
            
            return Result.success("Function Call演示成功", response);
        } catch (Exception e) {
            log.error("Function Call演示失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("Function Call演示失败: " + e.getMessage());
        }
    }

    /**
     * 仅RAG演示
     */
    @PostMapping("/rag")
    public Result<EnhancedLLMChatService.ChatResponse> ragDemo(
            @RequestBody ConsumerChatHistoryRequest request) {
        
        String userId = request.getUserId();
        String message = request.getContent();
        log.info("RAG演示: userId={}, message={}", userId, message);
        
        try {
            EnhancedLLMChatService.ChatResponse response = enhancedLLMChatService.sendEnhancedMessage(
                    message, userId, false, true);
            
            return Result.success("RAG演示成功", response);
        } catch (Exception e) {
            log.error("RAG演示失败: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("RAG演示失败: " + e.getMessage());
        }
    }

    /**
     * 添加知识文档到RAG
     */
    @PostMapping("/knowledge")
    public Result<Map<String, Object>> addKnowledgeDocument(
            @RequestParam String content,
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "general") String category) {
        
        log.info("添加知识文档: title={}, category={}", title, category);
        
        try {
            String documentId = enhancedLLMChatService.addKnowledgeDocument(content, title, category);
            
            Map<String, Object> result = new HashMap<>();
            result.put("documentId", documentId);
            result.put("title", title);
            result.put("category", category);
            result.put("contentLength", content.length());
            
            return Result.success("知识文档添加成功", result);
        } catch (Exception e) {
            log.error("添加知识文档失败: title={}, error={}", title, e.getMessage(), e);
            return Result.error("添加知识文档失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用的函数列表
     */
    @GetMapping("/functions")
    public Result<Map<String, Object>> getAvailableFunctions() {
        log.info("获取可用函数列表");
        
        try {
            Map<String, Object> functions = enhancedLLMChatService.getAvailableFunctions();
            return Result.success("获取函数列表成功", functions);
        } catch (Exception e) {
            log.error("获取函数列表失败: error={}", e.getMessage(), e);
            return Result.error("获取函数列表失败: " + e.getMessage());
        }
    }

    /**
     * 功能演示和测试接口
     */
    @GetMapping("/demo")
    public Result<Map<String, Object>> getDemo() {
        Map<String, Object> demo = new HashMap<>();
        
        // Function Call演示
        Map<String, String> functionCallExamples = new HashMap<>();
        functionCallExamples.put("时间查询", "现在几点了？");
        functionCallExamples.put("天气查询", "北京的天气怎么样？");
        functionCallExamples.put("数学计算", "帮我计算 15 + 25");
        functionCallExamples.put("用户信息", "查看我的用户信息");
        functionCallExamples.put("发送通知", "给我发送一个提醒");
        demo.put("functionCallExamples", functionCallExamples);
        
        // RAG演示
        Map<String, String> ragExamples = new HashMap<>();
        ragExamples.put("Java相关", "什么是Java编程语言？");
        ragExamples.put("Spring Boot", "Spring Boot有什么特点？");
        ragExamples.put("机器学习", "机器学习的基本概念是什么？");
        ragExamples.put("数据库", "关系型数据库有哪些？");
        ragExamples.put("API设计", "什么是RESTful API？");
        demo.put("ragExamples", ragExamples);
        
        // 组合演示
        Map<String, String> combinedExamples = new HashMap<>();
        combinedExamples.put("时间+知识", "现在几点了？顺便告诉我Java的特点");
        combinedExamples.put("计算+搜索", "计算 10*5，然后搜索Spring Boot相关信息");
        demo.put("combinedExamples", combinedExamples);
        
        // API使用说明
        Map<String, String> apiUsage = new HashMap<>();
        apiUsage.put("同步接口", "POST /api/enhanced-chat/send");
        apiUsage.put("流式接口", "POST /api/enhanced-chat/stream");
        apiUsage.put("Function Call", "POST /api/enhanced-chat/function-call");
        apiUsage.put("RAG", "POST /api/enhanced-chat/rag");
        apiUsage.put("添加知识", "POST /api/enhanced-chat/knowledge");
        apiUsage.put("函数列表", "GET /api/enhanced-chat/functions");
        demo.put("apiUsage", apiUsage);
        
        return Result.success("增强聊天功能演示", demo);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Enhanced LLM Chat Service");
        health.put("features", new String[]{"Function Call", "RAG", "Streaming"});
        health.put("timestamp", System.currentTimeMillis());
        
        return Result.success("服务健康", health);
    }
}
