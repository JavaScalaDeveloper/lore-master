package com.lore.master.data.dto.chat;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息DTO
 */
@Data
public class ChatMessageDTO {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 会话ID
     */
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 消息类型：user, assistant, system
     */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 消息格式：text, image, audio, file, multimodal
     */
    private String contentType = "text";

    /**
     * 附件列表
     */
    private List<AttachmentDTO> attachments;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * Token使用量
     */
    private Integer tokenCount;

    /**
     * 消息状态
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 父消息ID
     */
    private String parentMessageId;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 附件DTO
     */
    @Data
    public static class AttachmentDTO {
        /**
         * 附件类型：image, audio, file
         */
        private String type;

        /**
         * 附件URL
         */
        private String url;

        /**
         * 附件名称
         */
        private String name;

        /**
         * 附件大小（字节）
         */
        private Long size;

        /**
         * MIME类型
         */
        private String mimeType;

        /**
         * 缩略图URL（图片/视频）
         */
        private String thumbnailUrl;

        /**
         * 时长（音频/视频，秒）
         */
        private Integer duration;

        /**
         * 宽度（图片/视频）
         */
        private Integer width;

        /**
         * 高度（图片/视频）
         */
        private Integer height;
    }
}
