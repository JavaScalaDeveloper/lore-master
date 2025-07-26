package com.lore.master.web.consumer.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lore.master.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * C端用户登录拦截器
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 预检请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // TODO: 实现JWT token验证逻辑
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            // 未登录，返回401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            
            Result<Object> result = Result.error("请先登录");
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }

        // TODO: 验证token有效性，解析用户信息
        // 暂时放行所有请求
        return true;
    }
}
