package com.lore.master.data.entity.consumer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 聊天消息实体
 */
@Data
@Entity
@Table(name = "chat_message")
@EqualsAndHashCode(callSuper = false)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 消息ID（UUID）
     */
    @Column(name = "message_id", nullable = false, unique = true, length = 64)
    private String messageId;

    /**
     * 会话ID
     */
    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /**
     * 消息类型：user, assistant, system
     */
    @Column(name = "message_type", nullable = false, length = 20)
    private String messageType;

    /**
     * 消息内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 消息格式：text, image, audio, file, multimodal
     */
    @Column(name = "content_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'text'")
    private String contentType = "text";

    /**
     * 附件信息（JSON格式，存储图片、音频、文件等信息）
     */
    @Column(name = "attachments", columnDefinition = "JSON")
    private String attachments;

    /**
     * 模型名称
     */
    @Column(name = "model_name", length = 50)
    private String modelName;

    /**
     * Token使用量
     */
    @Column(name = "token_count")
    private Integer tokenCount;

    /**
     * 消息状态：pending, completed, failed
     */
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'completed'")
    private String status = "completed";

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 父消息ID（用于消息链）
     */
    @Column(name = "parent_message_id", length = 64)
    private String parentMessageId;

    /**
     * 消息元数据（JSON格式）
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
