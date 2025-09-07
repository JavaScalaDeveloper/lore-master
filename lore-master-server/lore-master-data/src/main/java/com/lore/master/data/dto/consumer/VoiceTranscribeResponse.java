package com.lore.master.data.dto.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 语音转文字响应DTO
 *
 * @author lore-master
 * @since 2024-09-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceTranscribeResponse {
    
    /**
     * 识别出的文字内容
     */
    private String text;
    
    /**
     * 识别置信度 (0-1)
     */
    private Double confidence;
    
    /**
     * 音频时长（秒）
     */
    private Double duration;
    
    /**
     * 识别结果状态码
     */
    private String resultCode;
    
    /**
     * 识别引擎提供商
     */
    private String provider;
    
    /**
     * 处理时间（毫秒）
     */
    private Long processingTime;
}