package com.lore.master.data.entity.consumer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 聊天会话实体
 */
@Data
@Entity
@Table(name = "chat_session")
@EqualsAndHashCode(callSuper = false)
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会话ID（UUID）
     */
    @Column(name = "session_id", nullable = false, unique = true, length = 64)
    private String sessionId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /**
     * 会话标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 会话描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 会话类型：chat, assessment, learning
     */
    @Column(name = "session_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'chat'")
    private String sessionType = "chat";

    /**
     * 使用的模型名称
     */
    @Column(name = "model_name", length = 50)
    private String modelName;

    /**
     * 会话配置（JSON格式）
     */
    @Column(name = "config", columnDefinition = "JSON")
    private String config;

    /**
     * 会话状态：active, archived, deleted
     */
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'active'")
    private String status = "active";

    /**
     * 消息总数
     */
    @Column(name = "message_count", columnDefinition = "INT DEFAULT 0")
    private Integer messageCount = 0;

    /**
     * 总Token使用量
     */
    @Column(name = "total_tokens", columnDefinition = "INT DEFAULT 0")
    private Integer totalTokens = 0;

    /**
     * 最后活跃时间
     */
    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime;

    /**
     * 会话元数据（JSON格式）
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    /**
     * 是否删除
     */
    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}
