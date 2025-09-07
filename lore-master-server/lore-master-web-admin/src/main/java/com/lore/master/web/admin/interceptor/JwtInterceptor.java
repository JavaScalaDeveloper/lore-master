package com.lore.master.web.admin.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.lore.master.common.result.Result;
import com.lore.master.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 获取请求路径
        String requestURI = request.getRequestURI();
        
        // 白名单路径，不需要验证token
        if (isWhiteList(requestURI)) {
            return true;
        }
        
        // 获取token
        String token = getTokenFromRequest(request);
        if (StrUtil.isBlank(token)) {
            writeErrorResponse(request, response, 401, "未登录，请先登录");
            return false;
        }
        
        // 验证token
        if (!JwtUtil.verifyToken(token)) {
            writeErrorResponse(request, response, 401, "登录已过期，请重新登录");
            return false;
        }
        
        // 检查token是否过期
        if (JwtUtil.isTokenExpired(token)) {
            writeErrorResponse(request, response, 401, "登录已过期，请重新登录");
            return false;
        }
        
        // 获取用户信息并设置到请求属性中
        Long userId = JwtUtil.getUserId(token);
        String username = JwtUtil.getUsername(token);
        
        if (userId == null || StrUtil.isBlank(username)) {
            writeErrorResponse(request, response, 401, "无效的登录信息");
            return false;
        }
        
        // 将用户信息设置到请求属性中，供后续使用
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        
        log.debug("JWT验证通过: userId={}, username={}, uri={}", userId, username, requestURI);
        return true;
    }
    
    /**
     * 判断是否为白名单路径
     */
    private boolean isWhiteList(String requestURI) {
        // 登录相关接口
        if (requestURI.startsWith("/api/admin/auth/login") || 
            requestURI.startsWith("/api/admin/auth/logout")) {
            return true;
        }
        
        // 健康检查和测试接口
        if (requestURI.startsWith("/api/admin/health") || 
            requestURI.startsWith("/api/admin/test")) {
            return true;
        }
        
        // 静态资源
        if (requestURI.startsWith("/static/") || 
            requestURI.startsWith("/public/") ||
            requestURI.startsWith("/favicon.ico")) {
            return true;
        }
        
        // Actuator监控端点
        if (requestURI.startsWith("/actuator/")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 从Header中获取
        String bearerToken = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 从参数中获取（兼容某些场景）
        String tokenParam = request.getParameter("token");
        if (StrUtil.isNotBlank(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }
    
    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletRequest request, HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code); // 设置HTTP状态码为实际的错误码（如401）
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 设置跨域头 - 从请求中获取Origin并验证
        String origin = request.getHeader("Origin");
        if (origin != null && isAllowedOrigin(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            // 如果没有Origin或不在允许列表中，使用默认的localhost
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3001");
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        Result<String> result = Result.error(code, message);
        String jsonResult = JSON.toJSONString(result);

        log.info("返回认证失败响应: status={}, message={}", code, message);

        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonResult);
            writer.flush();
        }
    }
    
    /**
     * 检查Origin是否在允许列表中
     */
    private boolean isAllowedOrigin(String origin) {
        String[] allowedOrigins = {
            "http://localhost:3000",
            "http://localhost:3001", 
            "http://localhost:3002",
            "http://localhost:3003",
            "http://localhost:3004",
            "https://www.loremaster.com",
            "https://test.loremaster.com",
            "https://ly112978940c.vicp.fun"
        };
        
        for (String allowedOrigin : allowedOrigins) {
            if (allowedOrigin.equals(origin)) {
                return true;
            }
        }
        return false;
    }
}
