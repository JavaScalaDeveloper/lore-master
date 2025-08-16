package com.lore.master.data.dto.chat;

import lombok.Data;

/**
 * 聊天历史请求参数
 */
@Data
public class ConsumerChatHistoryRequest {
    private String userId;
    private String content;
    private Integer maxMessages;
    private int page = 0;
    private int size = 50;
}