package com.lore.master.data.dto.chat;

import lombok.Data;

/**
 * 聊天统计信息DTO
 */
@Data
public class ChatStatsDTO {
    
    /**
     * 总会话数
     */
    private Long totalSessions;
    
    /**
     * 总消息数
     */
    private Long totalMessages;
    
    /**
     * 总Token使用量
     */
    private Integer totalTokens;
    
    /**
     * 今日消息数
     */
    private Long todayMessages;
    
    /**
     * 今日Token使用量
     */
    private Integer todayTokens;
}
