package com.lore.master.web.consumer.service;

import com.lore.master.service.consumer.impl.VoiceRecognitionService;
import com.lore.master.data.dto.consumer.VoiceTranscribeRequest;
import com.lore.master.data.dto.consumer.VoiceTranscribeResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;

/**
 * 语音识别服务测试
 *
 * @author lore-master
 * @since 2024-09-07
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class VoiceRecognitionServiceTest {
    
    @Autowired
    private VoiceRecognitionService voiceRecognitionService;
    
    @Test
    public void testServiceAvailability() {
        boolean isAvailable = voiceRecognitionService.isServiceAvailable();
        log.info("语音识别服务可用性: {}", isAvailable);
        
        String status = voiceRecognitionService.getServiceStatus();
        log.info("服务状态: {}", status);
    }
    
    @Test
    public void testVoiceTranscribe() {
        try {
            // 注意：这里需要一个测试音频文件
            // 可以从classpath加载测试音频
            InputStream audioStream = getClass().getResourceAsStream("/test-audio.mp3");
            if (audioStream == null) {
                log.warn("测试音频文件不存在，跳过语音识别测试");
                return;
            }
            
            byte[] audioData = audioStream.readAllBytes();
            
            VoiceTranscribeRequest request = VoiceTranscribeRequest.builder()
                    .userId("test-user")
                    .format("mp3")
                    .fileSize((long) audioData.length)
                    .language("zh_cn")
                    .build();
            
            VoiceTranscribeResponse response = voiceRecognitionService.transcribeVoice(audioData, request);
            
            log.info("语音识别结果: {}", response);
            log.info("识别文字: {}", response.getText());
            log.info("置信度: {}", response.getConfidence());
            log.info("处理时间: {}ms", response.getProcessingTime());
            
        } catch (IOException e) {
            log.error("读取测试音频文件失败: {}", e.getMessage());
        } catch (Exception e) {
            log.error("语音识别测试失败: {}", e.getMessage(), e);
        }
    }
}