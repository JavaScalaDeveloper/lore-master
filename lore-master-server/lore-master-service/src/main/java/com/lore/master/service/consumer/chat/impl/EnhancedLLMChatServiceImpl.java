package com.lore.master.service.consumer.chat.impl;

import com.lore.master.service.consumer.chat.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 增强的LLM聊天服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedLLMChatServiceImpl implements EnhancedLLMChatService {

    private final FunctionCallService functionCallService;
    private final RAGService ragService;

    @Override
    public ChatResponse sendEnhancedMessage(String message, String userId, boolean enableFunctionCall, boolean enableRAG) {
        log.info("处理增强聊天请求: userId={}, enableFC={}, enableRAG={}", userId, enableFunctionCall, enableRAG);
        
        long startTime = System.currentTimeMillis();
        ChatResponse response = new ChatResponse();
        
        try {
            String processedMessage = message;
            Map<String, Object> functionResults = new HashMap<>();
            List<String> retrievedDocs = new ArrayList<>();

            // 1. Function Call处理
            if (enableFunctionCall) {
                Map<String, Object> fcResult = processFunctionCalls(message);
                if (!fcResult.isEmpty()) {
                    response.setUsedFunctionCall(true);
                    response.setFunctionResults(fcResult);
                    
                    // 将函数调用结果添加到消息中
                    processedMessage += "\n\n函数调用结果：" + fcResult.toString();
                }
            }

            // 2. RAG处理
            if (enableRAG) {
                List<RAGService.DocumentChunk> documents = ragService.retrieveDocuments(message, 3);
                if (!documents.isEmpty()) {
                    response.setUsedRAG(true);
                    
                    for (RAGService.DocumentChunk doc : documents) {
                        retrievedDocs.add(doc.getMetadata().getTitle() + ": " + doc.getContent().substring(0, Math.min(100, doc.getContent().length())) + "...");
                    }
                    response.setRetrievedDocuments(retrievedDocs);
                    
                    // 生成增强提示词
                    processedMessage = ragService.generateAugmentedPrompt(message, documents);
                }
            }

            // 3. 生成最终响应
            String finalResponse = generateResponse(processedMessage, response.isUsedFunctionCall(), response.isUsedRAG());
            response.setMessage(finalResponse);
            
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            
            return response;
            
        } catch (Exception e) {
            log.error("处理增强聊天请求失败: {}", e.getMessage(), e);
            response.setMessage("抱歉，处理您的请求时出现了错误：" + e.getMessage());
            response.setProcessingTime(System.currentTimeMillis() - startTime);
            return response;
        }
    }

    @Override
    public Flux<String> sendEnhancedMessageStream(String message, String userId, boolean enableFunctionCall, boolean enableRAG) {
        log.info("处理流式增强聊天请求: userId={}, enableFC={}, enableRAG={}", userId, enableFunctionCall, enableRAG);
        
        return Flux.defer(() -> {
            try {
                ChatResponse response = sendEnhancedMessage(message, userId, enableFunctionCall, enableRAG);
                
                // 将响应分割成流式输出
                List<String> segments = new ArrayList<>();
                
                // 添加处理信息
                if (response.isUsedFunctionCall()) {
                    segments.add("🔧 执行了函数调用...\n");
                }
                if (response.isUsedRAG()) {
                    segments.add("📚 检索了相关文档...\n");
                }
                
                // 分割主要响应内容
                String[] messageParts = splitMessage(response.getMessage());
                segments.addAll(Arrays.asList(messageParts));
                
                // 添加元信息
                if (response.isUsedFunctionCall() && response.getFunctionResults() != null) {
                    segments.add("\n\n📋 函数调用结果：" + response.getFunctionResults().toString());
                }
                
                if (response.isUsedRAG() && response.getRetrievedDocuments() != null) {
                    segments.add("\n\n📖 参考文档：" + String.join(", ", response.getRetrievedDocuments()));
                }
                
                segments.add("\n\n⏱️ 处理时间：" + response.getProcessingTime() + "ms");
                
                return Flux.fromIterable(segments)
                        .delayElements(Duration.ofMillis(200));
                        
            } catch (Exception e) {
                return Flux.just("抱歉，处理您的请求时出现了错误：" + e.getMessage());
            }
        });
    }

    @Override
    public String addKnowledgeDocument(String content, String title, String category) {
        RAGService.DocumentMetadata metadata = new RAGService.DocumentMetadata(title, "user_upload", category);
        return ragService.addDocument(content, metadata);
    }

    @Override
    public Map<String, Object> getAvailableFunctions() {
        Map<String, FunctionCallService.FunctionDefinition> functions = functionCallService.getAvailableFunctions();
        Map<String, Object> result = new HashMap<>();
        
        for (Map.Entry<String, FunctionCallService.FunctionDefinition> entry : functions.entrySet()) {
            Map<String, Object> funcInfo = new HashMap<>();
            funcInfo.put("name", entry.getValue().getName());
            funcInfo.put("description", entry.getValue().getDescription());
            funcInfo.put("parameters", entry.getValue().getParameters());
            result.put(entry.getKey(), funcInfo);
        }
        
        return result;
    }

    /**
     * 处理函数调用
     */
    private Map<String, Object> processFunctionCalls(String message) {
        Map<String, Object> results = new HashMap<>();
        
        // 检测是否需要调用函数
        if (needsFunctionCall(message)) {
            String functionName = detectFunctionName(message);
            Map<String, Object> parameters = extractParameters(message, functionName);
            
            if (functionName != null) {
                try {
                    Object result = functionCallService.executeFunction(functionName, parameters);
                    results.put(functionName, result);
                } catch (Exception e) {
                    results.put(functionName + "_error", e.getMessage());
                }
            }
        }
        
        return results;
    }

    /**
     * 检测是否需要函数调用
     */
    private boolean needsFunctionCall(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("时间") || lowerMessage.contains("天气") || 
               lowerMessage.contains("计算") || lowerMessage.contains("搜索") ||
               lowerMessage.contains("用户信息") || lowerMessage.contains("通知");
    }

    /**
     * 检测函数名称
     */
    private String detectFunctionName(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("时间") || lowerMessage.contains("现在几点")) {
            return "get_current_time";
        } else if (lowerMessage.contains("天气")) {
            return "get_weather";
        } else if (lowerMessage.contains("计算") || lowerMessage.contains("算")) {
            return "calculate";
        } else if (lowerMessage.contains("搜索") || lowerMessage.contains("查找")) {
            return "search_knowledge";
        } else if (lowerMessage.contains("用户信息")) {
            return "get_user_info";
        } else if (lowerMessage.contains("通知") || lowerMessage.contains("提醒")) {
            return "send_notification";
        }
        
        return null;
    }

    /**
     * 提取参数
     */
    private Map<String, Object> extractParameters(String message, String functionName) {
        Map<String, Object> parameters = new HashMap<>();
        
        switch (functionName) {
            case "get_weather":
                String city = extractCity(message);
                if (city != null) {
                    parameters.put("city", city);
                }
                break;
            case "calculate":
                String expression = extractExpression(message);
                if (expression != null) {
                    parameters.put("expression", expression);
                }
                break;
            case "search_knowledge":
                parameters.put("query", message);
                break;
            case "get_user_info":
                parameters.put("userId", "default_user");
                break;
            case "send_notification":
                parameters.put("message", message);
                parameters.put("userId", "default_user");
                break;
        }
        
        return parameters;
    }

    private String extractCity(String message) {
        // 简单的城市提取逻辑
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5]+市|[\\u4e00-\\u9fa5]+县|[\\u4e00-\\u9fa5]+区)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "北京"; // 默认城市
    }

    private String extractExpression(String message) {
        // 提取数学表达式
        Pattern pattern = Pattern.compile("([0-9+\\-*/\\s\\.]+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 生成最终响应
     */
    private String generateResponse(String processedMessage, boolean usedFC, boolean usedRAG) {
        // 模拟LLM响应生成
        StringBuilder response = new StringBuilder();
        
        if (usedFC && usedRAG) {
            response.append("我已经调用了相关函数并检索了知识库，");
        } else if (usedFC) {
            response.append("我已经调用了相关函数，");
        } else if (usedRAG) {
            response.append("我已经检索了相关文档，");
        }
        
        response.append("基于您的问题「").append(processedMessage.length() > 50 ? 
                processedMessage.substring(0, 50) + "..." : processedMessage).append("」，");
        
        response.append("我为您提供以下回答：这是一个增强的AI回复，");
        response.append("集成了Function Call和RAG功能。");
        
        if (usedFC) {
            response.append("通过函数调用获取了实时数据。");
        }
        
        if (usedRAG) {
            response.append("通过知识库检索获取了相关背景信息。");
        }
        
        return response.toString();
    }

    /**
     * 分割消息用于流式输出
     */
    private String[] splitMessage(String message) {
        if (message.length() <= 50) {
            return new String[]{message};
        }
        
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < message.length()) {
            int end = Math.min(start + 50, message.length());
            parts.add(message.substring(start, end));
            start = end;
        }
        
        return parts.toArray(new String[0]);
    }
}
