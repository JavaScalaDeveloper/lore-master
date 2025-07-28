package com.lore.master.service.consumer.chat.impl;

import com.lore.master.service.consumer.chat.RAGService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RAG服务实现类
 */
@Slf4j
@Service
public class RAGServiceImpl implements RAGService {

    // 模拟的文档存储
    private final Map<String, DocumentChunk> documentStore = new ConcurrentHashMap<>();
    
    public RAGServiceImpl() {
        initializeKnowledgeBase();
    }

    @Override
    public List<DocumentChunk> retrieveDocuments(String query, int topK) {
        log.info("检索文档: query={}, topK={}", query, topK);

        List<DocumentChunk> allDocuments = new ArrayList<>(documentStore.values());
        
        // 计算相似度（简单的关键词匹配）
        for (DocumentChunk doc : allDocuments) {
            double similarity = calculateSimilarity(query, doc.getContent());
            doc.setSimilarity(similarity);
        }

        // 按相似度排序并返回前topK个
        return allDocuments.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .limit(topK)
                .filter(doc -> doc.getSimilarity() > 0.1) // 过滤相似度太低的文档
                .toList();
    }

    @Override
    public String addDocument(String content, DocumentMetadata metadata) {
        String documentId = UUID.randomUUID().toString();
        DocumentChunk chunk = new DocumentChunk(documentId, content, metadata, 0.0);
        documentStore.put(documentId, chunk);
        log.info("添加文档: id={}, title={}", documentId, metadata.getTitle());
        return documentId;
    }

    @Override
    public boolean deleteDocument(String documentId) {
        boolean removed = documentStore.remove(documentId) != null;
        log.info("删除文档: id={}, success={}", documentId, removed);
        return removed;
    }

    @Override
    public String generateAugmentedPrompt(String query, List<DocumentChunk> documents) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("基于以下相关文档回答用户问题：\n\n");
        
        // 添加检索到的文档
        for (int i = 0; i < documents.size(); i++) {
            DocumentChunk doc = documents.get(i);
            prompt.append("文档").append(i + 1).append("：\n");
            prompt.append("标题：").append(doc.getMetadata().getTitle()).append("\n");
            prompt.append("内容：").append(doc.getContent()).append("\n");
            prompt.append("相似度：").append(String.format("%.2f", doc.getSimilarity())).append("\n\n");
        }
        
        prompt.append("用户问题：").append(query).append("\n\n");
        prompt.append("请基于上述文档内容回答用户问题，如果文档中没有相关信息，请明确说明。");
        
        return prompt.toString();
    }

    /**
     * 初始化知识库
     */
    private void initializeKnowledgeBase() {
        // 添加一些示例文档
        addDocument(
                "Java是一种面向对象的编程语言，具有跨平台、安全性高、性能优良等特点。Java程序可以在任何支持Java虚拟机的平台上运行。",
                new DocumentMetadata("Java编程语言介绍", "编程教程", "技术文档")
        );

        addDocument(
                "Spring Boot是一个基于Spring框架的快速开发框架，它简化了Spring应用的配置和部署。Spring Boot提供了自动配置、起步依赖、Actuator监控等功能。",
                new DocumentMetadata("Spring Boot框架", "编程教程", "技术文档")
        );

        addDocument(
                "机器学习是人工智能的一个分支，它使计算机能够在没有明确编程的情况下学习。常见的机器学习算法包括线性回归、决策树、神经网络等。",
                new DocumentMetadata("机器学习基础", "AI教程", "技术文档")
        );

        addDocument(
                "数据库是存储和管理数据的系统。关系型数据库使用SQL语言进行查询，常见的关系型数据库包括MySQL、PostgreSQL、Oracle等。",
                new DocumentMetadata("数据库基础知识", "数据库教程", "技术文档")
        );

        addDocument(
                "RESTful API是一种软件架构风格，用于设计网络应用程序的接口。REST API使用HTTP方法（GET、POST、PUT、DELETE）来操作资源。",
                new DocumentMetadata("RESTful API设计", "API设计", "技术文档")
        );

        addDocument(
                "Docker是一个开源的容器化平台，它允许开发者将应用程序及其依赖项打包到轻量级、可移植的容器中。Docker简化了应用的部署和管理。",
                new DocumentMetadata("Docker容器技术", "DevOps", "技术文档")
        );

        addDocument(
                "微服务架构是一种将单一应用程序开发为一套小服务的方法，每个服务运行在自己的进程中，并使用轻量级机制（通常是HTTP API）进行通信。",
                new DocumentMetadata("微服务架构", "系统架构", "技术文档")
        );

        log.info("知识库初始化完成，共加载 {} 个文档", documentStore.size());
    }

    /**
     * 计算查询与文档内容的相似度
     */
    private double calculateSimilarity(String query, String content) {
        // 简单的关键词匹配算法
        String[] queryWords = query.toLowerCase().split("\\s+");
        String contentLower = content.toLowerCase();
        
        int matchCount = 0;
        for (String word : queryWords) {
            if (contentLower.contains(word)) {
                matchCount++;
            }
        }
        
        // 计算匹配度
        double similarity = (double) matchCount / queryWords.length;
        
        // 考虑文档长度的影响
        double lengthFactor = Math.min(1.0, content.length() / 100.0);
        
        return similarity * lengthFactor;
    }
}
