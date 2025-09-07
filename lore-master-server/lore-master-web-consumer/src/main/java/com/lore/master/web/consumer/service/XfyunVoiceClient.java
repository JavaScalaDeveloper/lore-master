package com.lore.master.web.consumer.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.lore.master.web.consumer.config.XfyunVoiceConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 讯飞语音识别客户端
 * 使用讯飞开放平台的免费语音识别服务
 * 免费额度：每天500次调用
 *
 * @author lore-master
 * @since 2024-09-07
 */
@Slf4j
@Component
public class XfyunVoiceClient {
    
    @Autowired
    private XfyunVoiceConfig config;
    
    private final OkHttpClient httpClient;
    
    public XfyunVoiceClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 语音识别 - HTTP API方式
     *
     * @param audioData 音频数据
     * @param format    音频格式 (mp3, wav, pcm)
     * @return 识别结果
     */
    public String recognizeVoice(byte[] audioData, String format) {
        try {
            log.info("开始调用讯飞语音识别，音频大小: {} bytes, 格式: {}", audioData.length, format);
            
            // 构建请求URL和Header
            String url = buildAuthUrl();
            String audioBase64 = Base64.encodeBase64String(audioData);
            
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            
            // 公共参数
            JSONObject common = new JSONObject();
            common.put("app_id", config.getAppId());
            requestBody.put("common", common);
            
            // 业务参数
            JSONObject business = new JSONObject();
            business.put("language", config.getLanguage());
            business.put("domain", config.getDomain());
            business.put("accent", "mandarin");
            business.put("vinfo", 1);
            business.put("vad_eos", 10000);
            
            // 根据音频格式设置参数
            if ("mp3".equalsIgnoreCase(format)) {
                business.put("aue", "lame");
                business.put("sfl", 1);
            } else if ("wav".equalsIgnoreCase(format)) {
                business.put("aue", "raw");
                business.put("sfl", 1);
            } else {
                business.put("aue", "raw");
                business.put("sfl", 1);
            }
            
            requestBody.put("business", business);
            
            // 数据参数
            JSONObject data = new JSONObject();
            data.put("status", 2); // 一次性识别
            data.put("format", "audio/L16;rate=16000");
            data.put("audio", audioBase64);
            data.put("encoding", "lame");
            requestBody.put("data", data);
            
            // 发送HTTP请求
            RequestBody body = RequestBody.create(
                    requestBody.toJSONString(), 
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(getHttpApiUrl())
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", buildAuthHeader())
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("讯飞API调用失败，状态码: " + response.code());
                }
                
                String responseBody = response.body().string();
                log.debug("讯飞API响应: {}", responseBody);
                
                return parseRecognitionResult(responseBody);
            }
            
        } catch (Exception e) {
            log.error("讯飞语音识别失败: {}", e.getMessage(), e);
            throw new RuntimeException("语音识别服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 解析识别结果
     */
    private String parseRecognitionResult(String responseBody) {
        try {
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            
            // 检查返回码
            Integer code = jsonResponse.getInteger("code");
            if (code == null || code != 0) {
                String message = jsonResponse.getString("message");
                throw new RuntimeException("讯飞语音识别失败: " + message);
            }
            
            // 解析识别结果
            JSONObject data = jsonResponse.getJSONObject("data");
            if (data == null) {
                throw new RuntimeException("讯飞API返回数据为空");
            }
            
            JSONArray result = data.getJSONArray("result");
            if (result == null || result.isEmpty()) {
                log.warn("讯飞语音识别结果为空");
                return "";
            }
            
            StringBuilder textBuilder = new StringBuilder();
            for (int i = 0; i < result.size(); i++) {
                JSONObject item = result.getJSONObject(i);
                JSONArray ws = item.getJSONArray("ws");
                
                if (ws != null) {
                    for (int j = 0; j < ws.size(); j++) {
                        JSONObject w = ws.getJSONObject(j);
                        JSONArray cw = w.getJSONArray("cw");
                        
                        if (cw != null) {
                            for (int k = 0; k < cw.size(); k++) {
                                JSONObject word = cw.getJSONObject(k);
                                String w_text = word.getString("w");
                                textBuilder.append(w_text);
                            }
                        }
                    }
                }
            }
            
            String recognizedText = textBuilder.toString();
            log.info("讯飞语音识别成功，结果: {}", recognizedText);
            
            return recognizedText;
            
        } catch (Exception e) {
            log.error("解析讯飞语音识别结果失败: {}", e.getMessage(), e);
            throw new RuntimeException("解析语音识别结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建认证URL
     */
    private String buildAuthUrl() {
        try {
            URL url = new URL(getHttpApiUrl());
            
            // RFC1123格式的时间戳
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());
            
            // 构建签名字符串
            String signatureOrigin = "host: " + url.getHost() + "\n";
            signatureOrigin += "date: " + date + "\n";
            signatureOrigin += "POST " + url.getPath() + " HTTP/1.1";
            
            // 进行hmac-sha256签名
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(config.getApiSecret().getBytes(StandardCharsets.UTF_8), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
            String sha = Base64.encodeBase64String(hexDigits);
            
            // 构建authorization字符串
            String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                    config.getApiKey(), "hmac-sha256", "host date request-line", sha);
            
            return authorization;
            
        } catch (Exception e) {
            log.error("构建讯飞认证信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("构建认证信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建认证头
     */
    private String buildAuthHeader() {
        return buildAuthUrl();
    }
    
    /**
     * 获取HTTP API地址
     */
    private String getHttpApiUrl() {
        return "https://iat-api.xfyun.cn/v2/iat";
    }
    
    /**
     * 检查服务可用性
     */
    public boolean isServiceAvailable() {
        try {
            // 发送一个测试请求来检查服务状态
            JSONObject testRequest = new JSONObject();
            JSONObject common = new JSONObject();
            common.put("app_id", config.getAppId());
            testRequest.put("common", common);
            
            RequestBody body = RequestBody.create(
                    testRequest.toJSONString(),
                    MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(getHttpApiUrl())
                    .post(body)
                    .addHeader("Authorization", buildAuthHeader())
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.code() != 401; // 401表示认证失败，其他错误可能是参数问题
            }
        } catch (Exception e) {
            log.warn("检查讯飞服务可用性失败: {}", e.getMessage());
            return false;
        }
    }
}