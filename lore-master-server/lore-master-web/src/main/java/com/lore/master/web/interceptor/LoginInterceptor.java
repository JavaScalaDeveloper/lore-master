package com.lore.master.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        // 只拦截 /api/users/**
        if (path.startsWith("/api/users")) {
            String auth = request.getHeader("Authorization");
            if (auth == null || !auth.equals("Bearer mock-token")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("未登录或token无效（mock）");
                return false;
            }
        }
        return true;
    }
} 