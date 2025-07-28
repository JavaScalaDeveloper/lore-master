package com.lore.master.service.consumer.chat;

import java.util.List;

/**
 * RAG (Retrieval-Augmented Generation) 服务接口
 */
public interface RAGService {

    /**
     * 根据查询检索相关文档
     * @param query 查询内容
     * @param topK 返回的文档数量
     * @return 相关文档列表
     */
    List<DocumentChunk> retrieveDocuments(String query, int topK);

    /**
     * 添加文档到知识库
     * @param content 文档内容
     * @param metadata 文档元数据
     * @return 文档ID
     */
    String addDocument(String content, DocumentMetadata metadata);

    /**
     * 删除文档
     * @param documentId 文档ID
     * @return 是否成功
     */
    boolean deleteDocument(String documentId);

    /**
     * 生成增强的提示词
     * @param query 用户查询
     * @param documents 检索到的文档
     * @return 增强的提示词
     */
    String generateAugmentedPrompt(String query, List<DocumentChunk> documents);

    /**
     * 文档块类
     */
    class DocumentChunk {
        private String id;
        private String content;
        private DocumentMetadata metadata;
        private double similarity;

        public DocumentChunk(String id, String content, DocumentMetadata metadata, double similarity) {
            this.id = id;
            this.content = content;
            this.metadata = metadata;
            this.similarity = similarity;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public DocumentMetadata getMetadata() { return metadata; }
        public void setMetadata(DocumentMetadata metadata) { this.metadata = metadata; }
        
        public double getSimilarity() { return similarity; }
        public void setSimilarity(double similarity) { this.similarity = similarity; }
    }

    /**
     * 文档元数据类
     */
    class DocumentMetadata {
        private String title;
        private String source;
        private String category;
        private long timestamp;

        public DocumentMetadata(String title, String source, String category) {
            this.title = title;
            this.source = source;
            this.category = category;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
