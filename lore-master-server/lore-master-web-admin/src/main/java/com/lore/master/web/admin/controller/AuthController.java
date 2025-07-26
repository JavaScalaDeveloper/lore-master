package com.lore.master.web.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.lore.master.common.result.Result;
import com.lore.master.data.dto.LoginRequest;
import com.lore.master.data.vo.LoginResponse;
import com.lore.master.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdminUserService adminUserService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                     HttpServletRequest request) {
        try {
            // 获取客户端IP
            String clientIp = getClientIp(request);

            // 执行登录
            LoginResponse response = adminUserService.login(loginRequest, clientIp);

            return Result.success("登录成功", response);
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (StrUtil.isNotBlank(token)) {
                // 这里可以将token加入黑名单，暂时简单处理
                log.info("用户登出: token={}", token);
            }

            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StrUtil.isNotBlank(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StrUtil.isNotBlank(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
