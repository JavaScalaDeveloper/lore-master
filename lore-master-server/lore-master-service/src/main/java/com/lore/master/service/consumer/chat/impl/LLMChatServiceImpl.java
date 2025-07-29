package com.lore.master.service.consumer.chat.impl;

import com.lore.master.common.config.LangChain4jConfig;
import com.lore.master.service.consumer.chat.LLMChatService;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import jakarta.annotation.Resource;
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

    @Resource(name = "qwenChatLanguageModel")
    private ChatLanguageModel chatLanguageModel;
    @Resource(name = "qwenStreamingChatLanguageModel")
    private StreamingChatLanguageModel streamingChatLanguageModel;

    @Override
    public Flux<String> sendMessageStream(String message, String userId) {
        log.info("发送流式LLM请求: userId={}, message={}", userId, message);

        if (!isAvailable()) {
            return Flux.just("LLM服务暂时不可用，请检查API配置。");
        }


        try {
            Flux<String> flux = Flux.create(fluxSink -> streamingChatLanguageModel.chat(message,
                    new StreamingChatResponseHandler() {
                        @Override
                        public void onPartialResponse(String partialResponse) {
                            fluxSink.next(partialResponse);
                        }

                        @Override
                        public void onCompleteResponse(ChatResponse completeResponse) {
                            fluxSink.complete();
                        }

                        @Override
                        public void onError(Throwable error) {
                            fluxSink.error(error);
                        }
                    }));
            return flux;
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
            return chatLanguageModel.chat(message);

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
