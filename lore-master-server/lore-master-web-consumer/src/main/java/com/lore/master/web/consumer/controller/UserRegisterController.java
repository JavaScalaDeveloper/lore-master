package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.dto.UserRegisterRequest;
import com.lore.master.data.vo.UserRegisterResponse;
import com.lore.master.service.consumer.ConsumerUserRegisterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * C端用户注册控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserRegisterController {
    
    private final ConsumerUserRegisterService consumerUserRegisterService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest request,
                                                HttpServletRequest httpRequest) {
        try {
            // 设置客户端信息
            request.setClientIp(getClientIp(httpRequest));
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
            request.setDeviceType(getDeviceType(httpRequest));
            
            UserRegisterResponse response = consumerUserRegisterService.register(request);
            return Result.success(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("C端用户注册参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("C端用户注册失败", e);
            return Result.error("注册失败，请稍后重试");
        }
    }
    
    /**
     * 检查注册标识是否可用
     */
    @GetMapping("/register/check")
    public Result<Boolean> checkRegisterKey(@RequestParam String registerType,
                                          @RequestParam String registerKey) {
        try {
            boolean available = consumerUserRegisterService.isRegisterKeyAvailable(registerType, registerKey);
            return Result.success(available);
        } catch (Exception e) {
            log.error("检查注册标识可用性失败", e);
            return Result.error("检查失败，请稍后重试");
        }
    }
    
    /**
     * 发送验证码
     */
    @PostMapping("/register/send-code")
    public Result<Boolean> sendVerifyCode(@RequestParam String registerType,
                                        @RequestParam String registerKey) {
        try {
            boolean success = consumerUserRegisterService.sendVerifyCode(registerType, registerKey);
            return success ? Result.success(true) : Result.error("发送验证码失败");
        } catch (IllegalArgumentException e) {
            log.warn("发送验证码参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            return Result.error("发送失败，请稍后重试");
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 获取设备类型
     */
    private String getDeviceType(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "unknown";
        }
        
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "tablet";
        } else {
            return "web";
        }
    }
}
