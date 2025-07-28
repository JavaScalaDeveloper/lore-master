package com.lore.master.data.dto.chat;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequestDTO {

    /**
     * 会话ID（可选，如果为空则创建新会话）
     */
    private String sessionId;

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 消息类型：text, image, audio, file, multimodal
     */
    private String contentType = "text";

    /**
     * 附件列表
     */
    private List<ChatMessageDTO.AttachmentDTO> attachments;

    /**
     * 模型名称（可选，使用默认模型）
     */
    private String modelName;

    /**
     * 会话标题（新会话时使用）
     */
    private String sessionTitle;

    /**
     * 会话类型：chat, assessment, learning
     */
    private String sessionType = "chat";

    /**
     * 是否流式输出
     */
    private Boolean stream = true;

    /**
     * 系统提示词（可选）
     */
    private String systemPrompt;

    /**
     * 温度参数（0.0-2.0）
     */
    private Double temperature;

    /**
     * 最大Token数
     */
    private Integer maxTokens;

    /**
     * 上下文消息数量限制
     */
    private Integer contextLimit = 10;
}
