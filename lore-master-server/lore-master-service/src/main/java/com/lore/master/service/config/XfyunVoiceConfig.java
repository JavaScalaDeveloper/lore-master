package com.lore.master.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 讯飞语音识别配置
 *
 * @author lore-master
 * @since 2024-09-07
 */
@Data
@Component
@ConfigurationProperties(prefix = "xfyun.voice")
public class XfyunVoiceConfig {
    
    /**
     * 应用ID
     */
    private String appId;
    
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * API Secret
     */
    private String apiSecret;
    
    /**
     * 语音识别URL
     */
    private String url = "wss://iat-api.xfyun.cn/v2/iat";
    
    /**
     * 音频采样率
     */
    private Integer sampleRate = 16000;
    
    /**
     * 音频编码格式
     */
    private String encoding = "raw";
    
    /**
     * 语言类型
     */
    private String language = "zh_cn";
    
    /**
     * 领域
     */
    private String domain = "iat";
    
    /**
     * 连接超时时间（毫秒）
     */
    private Long connectTimeout = 5000L;
    
    /**
     * 读取超时时间（毫秒）
     */
    private Long readTimeout = 30000L;
}