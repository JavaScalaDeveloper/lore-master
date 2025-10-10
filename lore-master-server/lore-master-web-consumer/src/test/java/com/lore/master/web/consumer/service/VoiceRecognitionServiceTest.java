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

    }
}