package com.lore.master.web.consumer.controller;

import com.lore.master.common.annotation.RequireLogin;
import com.lore.master.common.context.UserContext;
import com.lore.master.common.result.Result;
import com.lore.master.data.dto.consumer.VoiceTranscribeRequest;
import com.lore.master.data.dto.consumer.VoiceTranscribeResponse;
import com.lore.master.service.consumer.impl.VoiceRecognitionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 语音识别控制器
 *
 * @author lore-master
 * @since 2024-09-07
 */
@Slf4j
@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = {
    "http://localhost:3000",
    "http://localhost:3001", 
    "http://localhost:3002",
    "http://localhost:3003",
    "http://localhost:3004",
    "https://www.loremaster.com",
    "https://test.loremaster.com",
    "https://ly112978940c.vicp.fun"
}, allowCredentials = "true")
public class VoiceController {
    
    @Autowired
    private VoiceRecognitionService voiceRecognitionService;
    
    /**
     * 语音转文字
     *
     * @param request 语音识别请求
     * @param httpRequest HTTP请求
     * @return 识别结果
     */
    @PostMapping("/transcribe")
    @RequireLogin
    public Result<VoiceTranscribeResponse> transcribeVoice(
            @Valid @RequestBody VoiceTranscribeRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // 从 UserContext 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 记录请求信息
            String clientIP = getClientIP(httpRequest);
            log.info("收到语音转文字请求: userId={}, fileName={}, fileSize={}, format={}, clientIP={}", 
                    userId, request.getFileName(), request.getFileSize(), request.getFormat(), clientIP);
            
            // 验证基本参数
            if (!StringUtils.hasText(request.getAudioData())) {
                return Result.error("音频数据不能为空");
            }
            
            if (!StringUtils.hasText(request.getFormat())) {
                return Result.error("音频格式不能为空");
            }
            
            // 验证文件大小（限制10MB）
            if (request.getFileSize() > 10 * 1024 * 1024) {
                return Result.error("音频文件过大，请控制在10MB以内");
            }
            
            // 验证音频格式
            if (!isValidAudioFormat(request.getFormat())) {
                return Result.error("不支持的音频文件格式，仅支持MP3、WAV格式");
            }
            
            // 解码Base64音频数据
            byte[] audioBytes;
            try {
                audioBytes = Base64.decodeBase64(request.getAudioData());
            } catch (Exception e) {
                log.error("解码音频数据失败: {}", e.getMessage());
                return Result.error("音频数据格式错误");
            }
            
            // 验证解码后的数据大小
            if (audioBytes.length == 0) {
                return Result.error("音频数据为空");
            }
            
            if (audioBytes.length > 10 * 1024 * 1024) {
                return Result.error("音频文件过大，请控制在10MB以内");
            }
            
            // 构建服务请求对象（修改userId为从 UserContext 获取）
            VoiceTranscribeRequest serviceRequest = VoiceTranscribeRequest.builder()
                    .audioData(request.getAudioData())
                    .format(request.getFormat().toLowerCase())
                    .fileSize((long) audioBytes.length) // 使用实际解码后的大小
                    .duration(request.getDuration())
                    .language(request.getLanguage() != null ? request.getLanguage() : "zh_cn")
                    .fileName(request.getFileName())
                    .build();
            
            // 调用语音识别服务
            VoiceTranscribeResponse response = voiceRecognitionService.transcribeVoice(
                    audioBytes, serviceRequest, userId);
            
            // 检查识别结果
            if ("ERROR".equals(response.getResultCode())) {
                log.error("语音识别失败: userId={}, provider={}", userId, response.getProvider());
                return Result.error("语音识别失败，请稍后重试");
            }
            
            // 记录成功日志
            log.info("语音识别成功: userId={}, textLength={}, processingTime={}ms", 
                    userId, response.getText().length(), response.getProcessingTime());
            
            return Result.success(response);
            
        } catch (IllegalArgumentException e) {
            String userId = UserContext.getCurrentUserId();
            log.warn("语音识别参数错误: userId={}, error={}", userId, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            String userId = UserContext.getCurrentUserId();
            log.error("语音识别异常: userId={}, error={}", userId, e.getMessage(), e);
            return Result.error("语音识别服务异常，请稍后重试");
        }
    }
    
    /**
     * 获取语音识别服务状态
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getServiceStatus() {
        try {
            boolean isAvailable = voiceRecognitionService.isServiceAvailable();
            String status = voiceRecognitionService.getServiceStatus();
            
            Map<String, Object> result = new HashMap<>();
            result.put("available", isAvailable);
            result.put("status", status);
            result.put("provider", "xfyun");
            result.put("timestamp", System.currentTimeMillis());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取语音识别服务状态失败: {}", e.getMessage(), e);
            return Result.error("获取服务状态失败");
        }
    }
    
    /**
     * 芃发震音识别调试信息
     */
    @PostMapping("/debug")
    @RequireLogin
    public Result<Map<String, Object>> debugVoiceRecognition(
            @Valid @RequestBody VoiceTranscribeRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("userId", userId);
            debugInfo.put("timestamp", System.currentTimeMillis());
            
            // 检查音频数据
            if (!StringUtils.hasText(request.getAudioData())) {
                debugInfo.put("error", "音频数据为空");
                return Result.success(debugInfo);
            }
            
            byte[] audioBytes = Base64.decodeBase64(request.getAudioData());
            debugInfo.put("audioDataSize", audioBytes.length);
            debugInfo.put("format", request.getFormat());
            debugInfo.put("originalFileSize", request.getFileSize());
            
            // 检查服务可用性
            boolean serviceAvailable = voiceRecognitionService.isServiceAvailable();
            debugInfo.put("serviceAvailable", serviceAvailable);
            debugInfo.put("serviceStatus", voiceRecognitionService.getServiceStatus());
            
            // 检查音频数据特征
            if (audioBytes.length > 0) {
                debugInfo.put("firstByte", String.format("0x%02X", audioBytes[0]));
                debugInfo.put("lastByte", String.format("0x%02X", audioBytes[audioBytes.length - 1]));
                
                // 检查MP3文件头
                if (audioBytes.length >= 3) {
                    boolean isMP3 = (audioBytes[0] & 0xFF) == 0xFF && 
                                   (audioBytes[1] & 0xE0) == 0xE0;
                    debugInfo.put("hasMP3Header", isMP3);
                }
            }
            
            log.info("语音识别调试信息: {}", debugInfo);
            
            return Result.success(debugInfo);
            
        } catch (Exception e) {
            log.error("语音识别调试失败: {}", e.getMessage(), e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("timestamp", System.currentTimeMillis());
            return Result.success(errorInfo);
        }
    }
    
    /**
     * 验证音频格式
     */
    private boolean isValidAudioFormat(String format) {
        if (format == null) {
            return false;
        }
        String lowerFormat = format.toLowerCase();
        return "mp3".equals(lowerFormat) || "wav".equals(lowerFormat) || "pcm".equals(lowerFormat);
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}