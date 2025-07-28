package com.lore.master.service.consumer.chat.impl;

import com.lore.master.service.consumer.chat.LLMChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM聊天服务实现类
 * 支持OpenAI API和兼容的API服务
 */
@Slf4j
@Service
public class LLMChatServiceImpl implements LLMChatService {

    @Value("${llm.api.key:}")
    private String apiKey;

    @Value("${llm.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${llm.model:gpt-3.5-turbo}")
    private String model;

    @Value("${llm.max-tokens:1000}")
    private int maxTokens;

    @Value("${llm.temperature:0.7}")
    private double temperature;

    private final RestTemplate restTemplate;

    public LLMChatServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Flux<String> sendMessageStream(String message, String userId) {
        log.info("发送流式LLM请求: userId={}, message={}", userId, message);

        if (!isAvailable()) {
            return Flux.just("LLM服务暂时不可用，请检查API配置。");
        }

        try {
            // 调用LLM API获取响应
            String response = callLLMAPI(message);
            
            // 将响应分割成多个片段进行流式输出
            String[] segments = splitResponse(response);
            
            return Flux.fromArray(segments)
                    .delayElements(Duration.ofMillis(100))
                    .doOnNext(segment -> log.debug("流式输出片段: {}", segment))
                    .doOnError(error -> log.error("流式输出错误: {}", error.getMessage(), error));
                    
        } catch (Exception e) {
            log.error("LLM流式请求失败: userId={}, error={}", userId, e.getMessage(), e);
            return Flux.just("抱歉，处理您的请求时出现了错误：" + e.getMessage());
        }
    }

    @Override
    public String sendMessage(String message, String userId) {
        log.info("发送LLM请求: userId={}, message={}", userId, message);

        if (!isAvailable()) {
            return "LLM服务暂时不可用，请检查API配置。";
        }

        try {
            return callLLMAPI(message);
        } catch (Exception e) {
            log.error("LLM请求失败: userId={}, error={}", userId, e.getMessage(), e);
            return "抱歉，处理您的请求时出现了错误：" + e.getMessage();
        }
    }

    @Override
    public boolean isAvailable() {
        return StringUtils.hasText(apiKey) && StringUtils.hasText(apiUrl);
    }

    /**
     * 调用LLM API
     */
    private String callLLMAPI(String message) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            
            // 构建消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            requestBody.put("messages", List.of(userMessage));

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 发送请求
            Map<String, Object> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            ).getBody();

            // 解析响应
            return parseResponse(response);

        } catch (Exception e) {
            log.error("调用LLM API失败: {}", e.getMessage(), e);
            throw new RuntimeException("LLM API调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析LLM API响应
     */
    @SuppressWarnings("unchecked")
    private String parseResponse(Map<String, Object> response) {
        try {
            if (response == null) {
                return "API返回空响应";
            }

            // 检查错误
            if (response.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                String errorMessage = (String) error.get("message");
                throw new RuntimeException("API错误: " + errorMessage);
            }

            // 解析正常响应
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "API返回空的选择列表";
            }

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            String content = (String) message.get("content");

            return content != null ? content.trim() : "API返回空内容";

        } catch (Exception e) {
            log.error("解析LLM响应失败: {}", e.getMessage(), e);
            return "解析响应失败: " + e.getMessage();
        }
    }

    /**
     * 将响应分割成片段用于流式输出
     */
    private String[] splitResponse(String response) {
        if (response == null || response.isEmpty()) {
            return new String[]{"无响应内容"};
        }

        // 按句子分割
        String[] sentences = response.split("(?<=[。！？.!?])\\s*");
        if (sentences.length <= 1) {
            // 如果没有句子分隔符，按长度分割
            return splitByLength(response, 20);
        }

        return sentences;
    }

    /**
     * 按长度分割字符串
     */
    private String[] splitByLength(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return new String[]{text};
        }

        int segments = (int) Math.ceil((double) text.length() / maxLength);
        String[] result = new String[segments];

        for (int i = 0; i < segments; i++) {
            int start = i * maxLength;
            int end = Math.min(start + maxLength, text.length());
            result[i] = text.substring(start, end);
        }

        return result;
    }
}
