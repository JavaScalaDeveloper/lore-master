package com.lore.master.service.ai;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssistantServiceConf {

    public interface Assistant {
        String chat(String message);

        // 流式响应
        TokenStream stream(String message);

        @SystemMessage("""
                您是“Tuling”航空公司的客户聊天支持代理。请以友好、乐于助人且愉快的方式来回复。
                您正在通过在线聊天系统与客户互动。
                在提供有关预订或取消预订的信息之前，您必须始终从用户处获取以下信息：预订号、客户姓名。
                请讲中文。
                今天的日期是 {{current_date}}.
                """)
        TokenStream stream(@UserMessage String message, @V("current_date") String currentDate);
    }


    @Bean
    public Assistant assistant(ChatLanguageModel ollamaChatLanguageModel
            , StreamingChatLanguageModel ollamaStreamingChatModel
//            , ToolsService toolsService
//            , EmbeddingStore embeddingStore
//            , QwenEmbeddingModel qwenEmbeddingModel
    ) {
        // 对话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 内容检索器
//        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
//                .embeddingStore(embeddingStore)
//                .embeddingModel(qwenEmbeddingModel)
//                .maxResults(5) // 最相似的5个结果
//                .minScore(0.6) // 只找相似度在0.6以上的内容
//                .build();

        // 为Assistant动态代理对象  chat  --->  对话内容存储ChatMemory----> 聊天记录ChatMemory取出来 ---->放入到当前对话中
        Assistant assistant = AiServices.builder(Assistant.class)
//                .tools(toolsService)
//                .contentRetriever(contentRetriever)
                .chatLanguageModel(ollamaChatLanguageModel)
                .streamingChatLanguageModel(ollamaStreamingChatModel)
                .chatMemory(chatMemory)
                .build();

        return assistant;
    }

}
