package com.lore.master.service.consumer.chat.impl;

import com.lore.master.data.dto.chat.ConsumerChatHistoryRequest;
import com.lore.master.service.ai.AssistantServiceConf;
import com.lore.master.service.consumer.chat.LLMChatService;
import com.lore.master.service.consumer.chat.ConsumerChatMessageService;
import com.lore.master.service.consumer.chat.UserChatMemoryService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * LLM聊天服务实现类
 * 支持OpenAI API和兼容的API服务
 */
@Slf4j
@Service
public class LLMChatServiceImpl implements LLMChatService {

    @Resource
    private AssistantServiceConf.Assistant assistant;

    @Resource
    private ConsumerChatMessageService chatMessageService;

    @Resource
    private UserChatMemoryService userChatMemoryService;

    @Resource(name = "ollamaChatLanguageModel")
    private ChatLanguageModel ollamaChatLanguageModel;

    @Resource(name = "ollamaStreamingChatModel")
    private StreamingChatLanguageModel ollamaStreamingChatModel;

    @Override
    public Flux<String> sendMessageStream(String message, String userId) {
        log.info("发送流式LLM请求: userId={}, message={}", userId, message);

        try {

            // 1. 保存用户消息到数据库
            ConsumerChatHistoryRequest userMsgRequest = new ConsumerChatHistoryRequest();
            userMsgRequest.setUserId(userId);
            userMsgRequest.setContent(message);
            chatMessageService.saveUserMessage(userMsgRequest);

            // 2. 获取用户的ChatMemory
            ChatMemory chatMemory = userChatMemoryService.getUserChatMemory(userId);

            // 3. 添加当前消息到ChatMemory
            userChatMemoryService.addUserMessage(userId, message);

            // 4. 创建带有ChatMemory的Assistant
            AssistantServiceConf.Assistant assistantWithMemory = AiServices.builder(AssistantServiceConf.Assistant.class)
                    .chatLanguageModel(ollamaChatLanguageModel)
                    .streamingChatLanguageModel(ollamaStreamingChatModel)
                    .chatMemory(chatMemory)
                    .build();

            // 5. 准备保存AI响应
            StringBuilder responseBuilder = new StringBuilder();

            return Flux.create(sink -> {
                try {
                    TokenStream stream = assistantWithMemory.stream(message);
                    stream.onPartialResponse(partialResponse -> {
                        log.debug("接收到部分响应: {}", partialResponse);
                        responseBuilder.append(partialResponse);
                        sink.next(partialResponse);
                    })
                    .onCompleteResponse(completeResponse -> {
                        log.debug("流式响应完成");

                        // 保存完整的AI响应到数据库
                        String fullResponse = responseBuilder.toString();
                        ConsumerChatHistoryRequest assistantMsgRequest = new ConsumerChatHistoryRequest();
                        assistantMsgRequest.setUserId(userId);
                        chatMessageService.saveAssistantMessage(assistantMsgRequest, fullResponse, "ollama");

                        // 添加AI响应到ChatMemory
                        userChatMemoryService.addAssistantMessage(userId, fullResponse);

                        log.info("AI响应完成并已保存: userId={}, responseLength={}", userId, fullResponse.length());
                        sink.complete();
                    })
                    .onError(error -> {
                        log.error("流式响应出错: userId={}, error={}", userId, error.getMessage(), error);
                        sink.error(error);
                    })
                    .start();
                } catch (Exception e) {
                    log.error("启动流式响应失败: userId={}, error={}", userId, e.getMessage(), e);
                    sink.error(e);
                }
            });

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
        return true;
    }

    /**
     * 调用LLM API
     */
    private String callLLMAPI(String message) {
        try {
//            return chatLanguageModel.chat(message);
            return assistant.chat(message);
        } catch (Exception e) {
            log.error("调用LLM API失败: {}", e.getMessage(), e);
            throw new RuntimeException("LLM API调用失败: " + e.getMessage(), e);
        }
    }


}
