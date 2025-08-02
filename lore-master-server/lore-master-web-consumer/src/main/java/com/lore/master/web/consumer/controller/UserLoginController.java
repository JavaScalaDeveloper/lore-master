package com.lore.master.web.consumer.controller;

import com.lore.master.common.annotation.RequireLogin;
import com.lore.master.common.context.UserContext;
import com.lore.master.common.result.Result;
import com.lore.master.data.dto.UserLoginRequest;
import com.lore.master.data.vo.UserLoginResponse;
import com.lore.master.service.consumer.ConsumerUserLoginService;
import com.lore.master.service.consumer.strategy.ConsumerUserLoginStrategy;
import com.lore.master.service.consumer.factory.ConsumerUserLoginStrategyFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * C端用户登录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserLoginController {

    private final ConsumerUserLoginService userLoginService;
    private final ConsumerUserLoginStrategyFactory strategyFactory;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request,
                                         HttpServletRequest httpRequest) {
        try {
            // 设置客户端信息
            request.setClientIp(getClientIp(httpRequest));
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
            request.setDeviceType(getDeviceType(httpRequest));

            UserLoginResponse response = userLoginService.login(request);
            return Result.success("登录成功", response);

        } catch (IllegalArgumentException e) {
            log.warn("C端用户登录参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("C端用户登录失败", e);
            return Result.error("登录失败，请稍后重试");
        }
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh-token")
    public Result<UserLoginResponse> refreshToken(@RequestParam String refreshToken) {
        try {
            UserLoginResponse response = userLoginService.refreshToken(refreshToken);
            return Result.success("刷新成功", response);
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return Result.error("刷新失败，请重新登录");
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @RequireLogin
    public Result<UserLoginResponse> info() {
        try {
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                return Result.error("用户未登录");
            }
            UserLoginResponse response = userLoginService.getUserInfo(userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Boolean> logout(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            boolean success = userLoginService.logout(token);
            return success ? Result.success("登出成功", true) : Result.error("登出失败");
        } catch (Exception e) {
            log.error("用户登出失败", e);
            return Result.error("登出失败");
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

    /**
     * 测试登录策略
     */
    @GetMapping("/test-strategy")
    public Result<String> testStrategy(@RequestParam String loginType) {
        try {
            ConsumerUserLoginStrategy strategy = strategyFactory.getStrategy(loginType);
            return Result.success("获取策略成功: " + strategy.getClass().getName());
        } catch (Exception e) {
            return Result.error("获取策略失败: " + e.getMessage());
        }
    }
}
