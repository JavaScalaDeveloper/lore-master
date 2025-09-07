package com.lore.master.data.dto.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 语音转文字请求DTO
 *
 * @author lore-master
 * @since 2024-09-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceTranscribeRequest {
    
    /**
     * 音频文件Base64编码
     */
    @NotBlank(message = "音频文件不能为空")
    private String audioData;
    
    /**
     * 音频格式
     */
    @NotBlank(message = "音频格式不能为空")
    private String format;
    
    /**
     * 音频文件大小（字节）
     */
    @NotNull(message = "音频文件大小不能为空")
    private Long fileSize;
    
    /**
     * 音频时长（秒，可选）
     */
    private Double duration;
    
    /**
     * 语言类型（可选，默认中文）
     */
    private String language = "zh_cn";
    
    /**
     * 原始文件名（可选）
     */
    private String fileName;
}