package com.lore.master.service.consumer.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.lore.master.service.config.XfyunVoiceConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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
     * 语音识别 - WebSocket API方式
     *
     * @param audioData 音频数据
     * @param format    音频格式 (mp3, wav, pcm)
     * @return 识别结果
     */
    public String recognizeVoice(byte[] audioData, String format) {
        try {
            log.info("开始调用讯飞语音识别，音频大小: {} bytes, 格式: {}", audioData.length, format);
            
            // 验证配置
            if (config.getAppId() == null || config.getApiKey() == null || config.getApiSecret() == null) {
                throw new RuntimeException("讯飞语音识别配置不完整，请检查APPID、APIKey和APISecret");
            }
            
            // 构建认证URL
            String authUrl = buildAuthUrl();
            log.debug("讯飞认证URL: {}", authUrl);
            
            // 使用CompletableFuture和CountDownLatch处理异步WebSocket响应
            CompletableFuture<String> resultFuture = new CompletableFuture<>();
            CountDownLatch latch = new CountDownLatch(1);
            StringBuilder recognitionResult = new StringBuilder();
            
            // 创建WebSocket客户端
            Request request = new Request.Builder()
                    .url(authUrl)
                    .build();
            
            WebSocketListener listener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    log.debug("讯飞WebSocket连接成功");
                    
                    try {
                        // 发送音频数据
                        sendAudioData(webSocket, audioData, format);
                    } catch (Exception e) {
                        log.error("发送音频数据失败: {}", e.getMessage(), e);
                        resultFuture.completeExceptionally(e);
                        latch.countDown();
                    }
                }
                
                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    log.debug("收到讯飞响应: {}", text);
                    
                    try {
                        JSONObject response = JSON.parseObject(text);
                        Integer code = response.getInteger("code");
                        
                        if (code != null && code == 0) {
                            // 解析识别结果
                            JSONObject data = response.getJSONObject("data");
                            if (data != null) {
                                JSONObject result = data.getJSONObject("result");
                                if (result != null) {
                                    JSONArray ws = result.getJSONArray("ws");
                                    if (ws != null && ws.size() > 0) {
                                        for (int i = 0; i < ws.size(); i++) {
                                            JSONObject w = ws.getJSONObject(i);
                                            JSONArray cw = w.getJSONArray("cw");
                                            if (cw != null && cw.size() > 0) {
                                                for (int j = 0; j < cw.size(); j++) {
                                                    JSONObject word = cw.getJSONObject(j);
                                                    String wText = word.getString("w");
                                                    if (wText != null && !wText.trim().isEmpty()) {
                                                        recognitionResult.append(wText);
                                                        log.debug("识别结果片段: [{}]", wText);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // 检查是否结束
                                Integer status = data.getInteger("status");
                                if (status != null && status == 2) {
                                    // 识别结束
                                    String finalResult = recognitionResult.toString().trim();
                                    log.info("语音识别结束，最终结果: [{}]", finalResult);
                                    
                                    if (finalResult.isEmpty()) {
                                        log.warn("语音识别结果为空，可能原因: 1.音频数据无有效语音 2.音频格式不匹配 3.音频质量太低");
                                        // 即使结果为空，也返回完成信号
                                        resultFuture.complete("");
                                    } else {
                                        resultFuture.complete(finalResult);
                                    }
                                    latch.countDown();
                                }
                            }
                        } else {
                            // 错误响应
                            String message = response.getString("message");
                            String errorMsg = String.format("讯飞语音识别失败: code=%s, message=%s", code, message);
                            log.error(errorMsg);
                            resultFuture.completeExceptionally(new RuntimeException(errorMsg));
                            latch.countDown();
                        }
                    } catch (Exception e) {
                        log.error("解析讯飞响应失败: {}", e.getMessage(), e);
                        resultFuture.completeExceptionally(e);
                        latch.countDown();
                    }
                }
                
                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    log.error("讯飞WebSocket连接失败: {}", t.getMessage(), t);
                    resultFuture.completeExceptionally(new RuntimeException("讯飞WebSocket连接失败: " + t.getMessage()));
                    latch.countDown();
                }
                
                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    log.debug("讯飞WebSocket连接关闭: code={}, reason={}", code, reason);
                    if (!resultFuture.isDone()) {
                        resultFuture.complete(recognitionResult.toString());
                    }
                    latch.countDown();
                }
            };
            
            // 开始WebSocket连接
            WebSocket webSocket = httpClient.newWebSocket(request, listener);
            
            // 等待结果，最多等待30秒
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                webSocket.close(1000, "超时");
                throw new RuntimeException("讯飞语音识别超时");
            }
            
            // 获取结果
            String result = resultFuture.get(5, TimeUnit.SECONDS);
            log.info("讯飞语音识别成功，结果: {}", result);
            
            return result;
            
        } catch (Exception e) {
            log.error("讯飞语音识别失败: {}", e.getMessage(), e);
            throw new RuntimeException("语音识别服务异常: " + e.getMessage());
        }
    }
    
    /**
     * 发送音频数据到WebSocket
     */
    private void sendAudioData(WebSocket webSocket, byte[] audioData, String format) {
        try {
            log.debug("开始发送音频数据: 格式={}, 大小={}字节", format, audioData.length);
            
            // 根据音频格式确定编码参数
            String aueFormat;
            String audioFormat;
            String encoding;
            
            if ("mp3".equalsIgnoreCase(format)) {
                // MP3格式 - 讯飞支持lame编码的mp3
                aueFormat = "lame";
                audioFormat = "audio/mpeg";
                encoding = "lame";
                log.debug("使用MP3格式: lame编码");
            } else if ("wav".equalsIgnoreCase(format)) {
                // WAV格式，通常是PCM编码
                aueFormat = "raw";
                audioFormat = "audio/L16;rate=16000";
                encoding = "raw";
                log.debug("使用WAV格式: PCM编码");
            } else {
                // 默认为PCM原始格式
                aueFormat = "raw";
                audioFormat = "audio/L16;rate=16000";
                encoding = "raw";
                log.debug("使用默认PCM格式");
            }
            
            log.debug("音频编码参数: aue={}, format={}, encoding={}", aueFormat, audioFormat, encoding);
            
            // 第一帧：发送开始标识和业务参数
            JSONObject firstFrame = new JSONObject();
            
            // 公共参数
            JSONObject common = new JSONObject();
            common.put("app_id", config.getAppId());
            firstFrame.put("common", common);
            
            // 业务参数
            JSONObject business = new JSONObject();
            business.put("language", config.getLanguage());
            business.put("domain", config.getDomain());
            business.put("accent", "mandarin");
            business.put("vinfo", 1);
            business.put("vad_eos", 5000); // 减少静音检测时间
            business.put("aue", aueFormat); // 使用正确的音频编码格式
            
            log.debug("讯飞业务参数: language={}, domain={}, aue={}", 
                    config.getLanguage(), config.getDomain(), aueFormat);
            
            firstFrame.put("business", business);
            
            // 第一帧数据参数
            JSONObject data = new JSONObject();
            data.put("status", 0); // 第一帧标识
            data.put("format", audioFormat); // 使用正确的音频格式
            data.put("encoding", encoding); // 使用正确的编码方式
            data.put("audio", ""); // 第一帧不包含音频数据
            firstFrame.put("data", data);
            
            // 发送第一帧
            String firstFrameJson = firstFrame.toJSONString();
            log.debug("发送第一帧: {}", firstFrameJson);
            webSocket.send(firstFrameJson);
            
            // 等待一下确保第一帧发送完成
            Thread.sleep(100);
            
            // 分块发送音频数据
            int chunkSize = "mp3".equalsIgnoreCase(format) ? 1280 : 1280; // MP3和PCM使用相同块大小
            int frameCount = 0;
            
            // 对于MP3格式，直接发送整个文件，不分块（因为MP3有帧结构）
            if ("mp3".equalsIgnoreCase(format)) {
                log.debug("发送MP3音频数据，不分块处理");
                
                JSONObject audioFrame = new JSONObject();
                JSONObject frameData = new JSONObject();
                frameData.put("status", 1); // 中间帧
                frameData.put("format", audioFormat);
                frameData.put("encoding", encoding);
                frameData.put("audio", Base64.encodeBase64String(audioData));
                audioFrame.put("data", frameData);
                
                String frameJson = audioFrame.toJSONString();
                webSocket.send(frameJson);
                frameCount = 1;
                
                log.debug("发送MP3数据帧，大小: {}字节", audioData.length);
                
                // 适当延时
                Thread.sleep(200);
            } else {
                // 对于PCM/WAV格式，分块发送
                log.debug("发送PCM/WAV音频数据，分块处理");
                
                for (int i = 0; i < audioData.length; i += chunkSize) {
                    int end = Math.min(i + chunkSize, audioData.length);
                    byte[] chunk = new byte[end - i];
                    System.arraycopy(audioData, i, chunk, 0, end - i);
                    
                    JSONObject audioFrame = new JSONObject();
                    JSONObject frameData = new JSONObject();
                    frameData.put("status", 1); // 中间帧
                    frameData.put("format", audioFormat);
                    frameData.put("encoding", encoding);
                    frameData.put("audio", Base64.encodeBase64String(chunk));
                    audioFrame.put("data", frameData);
                    
                    String frameJson = audioFrame.toJSONString();
                    webSocket.send(frameJson);
                    frameCount++;
                    
                    // 适当延时，模拟实时传输
                    Thread.sleep(40); // 增加延时，确保接收端有足够时间处理
                }
            }
            
            log.debug("音频数据发送完成，共发送{}帧", frameCount);
            
            // 等待一下确保所有数据帧发送完成
            Thread.sleep(100);
            
            // 发送结束帧
            JSONObject endFrame = new JSONObject();
            JSONObject endData = new JSONObject();
            endData.put("status", 2); // 结束帧标识
            endData.put("format", audioFormat);
            endData.put("encoding", encoding);
            endData.put("audio", ""); // 结束帧不包含音频数据
            endFrame.put("data", endData);
            
            String endFrameJson = endFrame.toJSONString();
            log.debug("发送结束帧: {}", endFrameJson);
            webSocket.send(endFrameJson);
            
            log.debug("音频数据发送完成，共 {} 字节，{}帧", audioData.length, frameCount);
            
        } catch (Exception e) {
            log.error("发送音频数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送音频数据失败: " + e.getMessage());
        }
    }

    
    /**
     * 构建认证URL - WebSocket方式
     */
    private String buildAuthUrl() {
        try {
            String baseUrl = "wss://iat-api.xfyun.cn/v2/iat";
            URL url = new URL("https://iat-api.xfyun.cn/v2/iat"); // 用于HTTPS解析host
            String host = url.getHost();
            
            // RFC1123格式的时间戳
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());
            
            // 构建签名字符串
            String signatureOrigin = "host: " + host + "\n" +
                                   "date: " + date + "\n" +
                                   "GET /v2/iat HTTP/1.1";
            
            log.debug("签名原始字符串: {}", signatureOrigin);
            
            // 进行hmac-sha256签名
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec spec = new SecretKeySpec(config.getApiSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
            String sha = Base64.encodeBase64String(hexDigits);
            
            // 构建 authorization 原始字符串
            String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                    config.getApiKey(), "hmac-sha256", "host date request-line", sha);
            
            // 对authorization进行Base64编码
            String authorization = Base64.encodeBase64String(authorizationOrigin.getBytes(StandardCharsets.UTF_8));
            
            // 构建最终的WebSocket URL
            String finalUrl = baseUrl + "?authorization=" + urlEncode(authorization) + 
                             "&date=" + urlEncode(date) + 
                             "&host=" + urlEncode(host);
            
            log.debug("认证URL: {}", finalUrl);
            
            return finalUrl;
            
        } catch (Exception e) {
            log.error("构建讯飞认证URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("构建认证URL失败: " + e.getMessage());
        }
    }
    
    /**
     * URL编码
     */
    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("URL编码失败: {}", e.getMessage());
            return str;
        }
    }
    
    /**
     * 检查服务可用性
     */
    public boolean isServiceAvailable() {
        try {
            // 验证配置
            if (config.getAppId() == null || config.getApiKey() == null || config.getApiSecret() == null) {
                log.warn("讯飞语音识别配置不完整，请检查APPID、APIKey和APISecret");
                return false;
            }
            
            // 构建认证URL
            String authUrl = buildAuthUrl();
            log.debug("测试连接URL: {}", authUrl);
            
            // 创建测试WebSocket连接
            Request request = new Request.Builder()
                    .url(authUrl)
                    .build();
            
            CountDownLatch latch = new CountDownLatch(1);
            boolean[] available = {false};
            
            WebSocketListener testListener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    log.debug("测试WebSocket连接成功");
                    available[0] = true;
                    webSocket.close(1000, "test connection");
                }
                
                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    log.warn("测试WebSocket连接失败: {}", t.getMessage());
                    available[0] = false;
                    latch.countDown();
                }
                
                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    log.debug("测试WebSocket连接关闭: code={}, reason={}", code, reason);
                    latch.countDown();
                }
            };
            
            WebSocket webSocket = httpClient.newWebSocket(request, testListener);
            
            // 等待5秒
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                webSocket.close(1000, "timeout");
            }
            
            return available[0];
            
        } catch (Exception e) {
            log.warn("检查讯飞服务可用性失败: {}", e.getMessage());
            return false;
        }
    }
}