package com.lore.master.service.consumer.chat;

import reactor.core.publisher.Flux;
import java.util.Map;

/**
 * 增强的LLM聊天服务接口
 * 支持Function Call和RAG功能
 */
public interface EnhancedLLMChatService {

    /**
     * 发送消息并获取增强响应（支持Function Call和RAG）
     * @param message 用户消息
     * @param userId 用户ID
     * @param enableFunctionCall 是否启用Function Call
     * @param enableRAG 是否启用RAG
     * @return 响应结果
     */
    ChatResponse sendEnhancedMessage(String message, String userId, boolean enableFunctionCall, boolean enableRAG);

    /**
     * 发送消息并获取流式增强响应
     * @param message 用户消息
     * @param userId 用户ID
     * @param enableFunctionCall 是否启用Function Call
     * @param enableRAG 是否启用RAG
     * @return 流式响应
     */
    Flux<String> sendEnhancedMessageStream(String message, String userId, boolean enableFunctionCall, boolean enableRAG);

    /**
     * 添加文档到RAG知识库
     * @param content 文档内容
     * @param title 文档标题
     * @param category 文档分类
     * @return 文档ID
     */
    String addKnowledgeDocument(String content, String title, String category);

    /**
     * 获取可用的函数列表
     * @return 函数列表
     */
    Map<String, Object> getAvailableFunctions();

    /**
     * 聊天响应类
     */
    class ChatResponse {
        private String message;
        private boolean usedFunctionCall;
        private boolean usedRAG;
        private Map<String, Object> functionResults;
        private java.util.List<String> retrievedDocuments;
        private long processingTime;

        public ChatResponse() {}

        public ChatResponse(String message) {
            this.message = message;
        }

        // Getters and Setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public boolean isUsedFunctionCall() { return usedFunctionCall; }
        public void setUsedFunctionCall(boolean usedFunctionCall) { this.usedFunctionCall = usedFunctionCall; }

        public boolean isUsedRAG() { return usedRAG; }
        public void setUsedRAG(boolean usedRAG) { this.usedRAG = usedRAG; }

        public Map<String, Object> getFunctionResults() { return functionResults; }
        public void setFunctionResults(Map<String, Object> functionResults) { this.functionResults = functionResults; }

        public java.util.List<String> getRetrievedDocuments() { return retrievedDocuments; }
        public void setRetrievedDocuments(java.util.List<String> retrievedDocuments) { this.retrievedDocuments = retrievedDocuments; }

        public long getProcessingTime() { return processingTime; }
        public void setProcessingTime(long processingTime) { this.processingTime = processingTime; }
    }
}
