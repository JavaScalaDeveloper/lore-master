package com.lore.master.data.dto.chat;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话DTO
 */
@Data
public class ChatSessionDTO {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 会话标题
     */
    @NotBlank(message = "会话标题不能为空")
    private String title;

    /**
     * 会话描述
     */
    private String description;

    /**
     * 会话类型：chat, assessment, learning
     */
    private String sessionType = "chat";

    /**
     * 使用的模型名称
     */
    private String modelName;

    /**
     * 会话状态：active, archived, deleted
     */
    private String status;

    /**
     * 消息总数
     */
    private Integer messageCount;

    /**
     * 总Token使用量
     */
    private Integer totalTokens;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 最后一条消息
     */
    private ChatMessageDTO lastMessage;

    /**
     * 消息列表（可选）
     */
    private List<ChatMessageDTO> messages;
}
