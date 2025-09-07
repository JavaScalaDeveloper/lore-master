package com.lore.master.service.consumer.impl;

import com.lore.master.data.dto.consumer.VoiceTranscribeRequest;
import com.lore.master.data.dto.consumer.VoiceTranscribeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 语音识别服务
 *
 * @author lore-master
 * @since 2024-09-07
 */
@Slf4j
@Service
public class VoiceRecognitionService {
    
    @Autowired
    private XfyunVoiceClient xfyunVoiceClient;
    
    /**
     * 语音转文字
     *
     * @param audioData 音频数据
     * @param request   请求参数
     * @param userId    用户ID（从 UserContext 获取）
     * @return 识别结果
     */
    public VoiceTranscribeResponse transcribeVoice(byte[] audioData, VoiceTranscribeRequest request, String userId) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始语音识别: userId={}, fileSize={}, format={}", 
                    userId, audioData.length, request.getFormat());
            
            // 验证音频数据
            if (audioData == null || audioData.length == 0) {
                throw new IllegalArgumentException("音频数据不能为空");
            }
            
            // 验证音频大小（限制10MB）
            if (audioData.length > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("音频文件过大，请控制在10MB以内");
            }
            
            // 验证音频格式
            String format = request.getFormat().toLowerCase();
            if (!isValidAudioFormat(format)) {
                throw new IllegalArgumentException("不支持的音频格式: " + format);
            }
            
            // 调用讯飞语音识别
            log.debug("准备调用讯飞语音识别服务: 音频大小={}字节, 格式={}", audioData.length, format);
            String recognizedText = xfyunVoiceClient.recognizeVoice(audioData, format);
            log.debug("讯飞语音识别原始结果: [{}]", recognizedText);
            
            // 处理识别结果
            if (!StringUtils.hasText(recognizedText)) {
                log.warn("语音识别结果为空: userId={}", userId);
                recognizedText = ""; // 返回空字符串而不是null
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // 构建响应
            VoiceTranscribeResponse response = VoiceTranscribeResponse.builder()
                    .text(recognizedText.trim())
                    .confidence(0.95) // 讯飞API通常有较高准确率
                    .duration(estimateAudioDuration(audioData, format))
                    .resultCode("SUCCESS")
                    .provider("xfyun")
                    .processingTime(processingTime)
                    .build();
            
            log.info("语音识别完成: userId={}, result={}, processingTime={}ms", 
                    userId, recognizedText, processingTime);
            
            return response;
            
        } catch (IllegalArgumentException e) {
            log.error("语音识别参数错误: userId={}, error={}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("语音识别失败: userId={}, error={}", userId, e.getMessage(), e);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // 返回错误响应
            return VoiceTranscribeResponse.builder()
                    .text("")
                    .confidence(0.0)
                    .duration(0.0)
                    .resultCode("ERROR")
                    .provider("xfyun")
                    .processingTime(processingTime)
                    .build();
        }
    }
    
    /**
     * 语音转文字（兼容旧版本）
     * 
     * @deprecated 请使用 {@link #transcribeVoice(byte[], VoiceTranscribeRequest, String)}
     */
    @Deprecated
    public VoiceTranscribeResponse transcribeVoice(byte[] audioData, VoiceTranscribeRequest request) {
        // 从 request 中获取 userId（如果有的话），否则使用默认值
        return transcribeVoice(audioData, request, "unknown");
    }
    
    /**
     * 验证音频格式
     */
    private boolean isValidAudioFormat(String format) {
        return "mp3".equals(format) || "wav".equals(format) || "pcm".equals(format);
    }
    
    /**
     * 估算音频时长
     */
    private double estimateAudioDuration(byte[] audioData, String format) {
        try {
            // 简单估算，实际项目中可以使用音频处理库获取精确时长
            if ("mp3".equals(format)) {
                // MP3压缩比约为1:10
                return audioData.length / (16000.0 * 2 * 0.1); // 16kHz, 16bit, 压缩比
            } else if ("wav".equals(format)) {
                // WAV无压缩
                return audioData.length / (16000.0 * 2); // 16kHz, 16bit
            } else {
                // PCM原始数据
                return audioData.length / (16000.0 * 2); // 16kHz, 16bit
            }
        } catch (Exception e) {
            log.warn("估算音频时长失败: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * 检查语音识别服务可用性
     */
    public boolean isServiceAvailable() {
        try {
            return xfyunVoiceClient.isServiceAvailable();
        } catch (Exception e) {
            log.error("检查语音识别服务可用性失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取服务状态信息
     */
    public String getServiceStatus() {
        if (isServiceAvailable()) {
            return "讯飞语音识别服务正常";
        } else {
            return "讯飞语音识别服务不可用";
        }
    }
}