package com.lore.master.common.config;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;


import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    @Value("${llm.ollama.url}")
    private String ollamaUrl;
    @Value("${llm.ollama.model:qwen3:0.6b}")
    private String ollamaModelName;

    @Value("${langchain4j.openai.temperature:0.7}")
    private Float temperature;

    @Value("${langchain4j.openai.max-tokens:2048}")
    private Integer maxTokens;

    @Value("${langchain4j.openai.timeout:60}")
    private Integer timeoutSeconds;

    /**
     * 同步聊天模型
     */
    @Bean(name = "qwenChatLanguageModel")
    public ChatLanguageModel chatLanguageModel() {
//        return OpenAiChatModel.builder()
        return QwenChatModel.builder()
                .apiKey(openaiApiKey)
//                .baseUrl(openaiBaseUrl)
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
    @Bean(name = "qwenStreamingChatLanguageModel")
    public QwenStreamingChatModel streamingChatLanguageModel() {
//        return OpenAiStreamingChatModel.builder()
        return QwenStreamingChatModel.builder()
                .apiKey(openaiApiKey)
//                .baseUrl(openaiBaseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
//                .timeout(Duration.ofSeconds(timeoutSeconds))
//                .logRequests(true)
//                .logResponses(true)
                .build();
    }


    /**
     * 同步聊天模型
     */
    @Bean(name = "ollamaChatLanguageModel")
    public ChatLanguageModel ollamaChatLanguageModel() {
        return  OllamaChatModel
                .builder()
                .baseUrl(ollamaUrl)
                .modelName(ollamaModelName)
                .build();
    }

    /**
     * 流式聊天模型
     */
    @Bean(name = "ollamaStreamingChatModel")
    public OllamaStreamingChatModel ollamaStreamingChatModel() {
        return OllamaStreamingChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(ollamaModelName)
                .temperature(Double.valueOf(temperature))
                .build();
    }


    public static void main(String[] args) {
//        qwenChatTest();
        ollamaChatTest();

    }

    private static void qwenChatTest() {
        ChatLanguageModel model = QwenChatModel
                .builder()
                .apiKey(System.getenv("LLM_API_KEY"))
                .modelName("qwen-max")
                .build();

        String answer = model.chat("你好，你是谁？");

        System.out.println(answer);
    }

    private static void ollamaChatTest() {
        ChatLanguageModel model = OllamaChatModel
                .builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen3:8b")
                .build();

        String answer = model.chat("你好，你是谁？");

        System.out.println(answer);
    }
}
