package com.lore.master.common.config;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import dev.langchain4j.model.chat.ChatLanguageModel;
import java.time.Duration;

/**
 * LangChain4j配置类
 */
@Configuration
public class LangChain4jConfig {

    @Value("${llm.api.key:}")
    private String openaiApiKey;

    @Value("${llm.api.url:https://api.openai.com/v1}")
    private String openaiBaseUrl;

    @Value("${llm.model:gpt-3.5-turbo}")
    private String modelName;

    @Value("${langchain4j.openai.temperature:0.7}")
    private Float temperature;

    @Value("${langchain4j.openai.max-tokens:2048}")
    private Integer maxTokens;

    @Value("${langchain4j.openai.timeout:60}")
    private Integer timeoutSeconds;

    /**
     * 同步聊天模型
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
//        return OpenAiChatModel.builder()
        return QwenChatModel.builder()
                .apiKey(openaiApiKey)
                .baseUrl(openaiBaseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
//                .timeout(Duration.ofSeconds(timeoutSeconds))
//                .logRequests(true)
//                .logResponses(true)
                .build();
    }

    /**
     * 流式聊天模型
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
//        return OpenAiStreamingChatModel.builder()
        return QwenStreamingChatModel.builder()
                .apiKey(openaiApiKey)
                .baseUrl(openaiBaseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
//                .timeout(Duration.ofSeconds(timeoutSeconds))
//                .logRequests(true)
//                .logResponses(true)
                .build();
    }
}
