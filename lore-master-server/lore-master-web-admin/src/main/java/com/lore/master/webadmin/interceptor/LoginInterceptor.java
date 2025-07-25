package com.lore.master.webadmin.interceptor;

import com.lore.master.data.entity.User;
import com.lore.master.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        logger.info("LoginInterceptor - URI: {}, Method: {}", uri, method);

        // 放行登录接口和OPTIONS请求
        if (uri.equals("/api/admin/login") || "OPTIONS".equalsIgnoreCase(method)) {
            logger.info("Allowing request to: {}", uri);
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        logger.info("Authorization header: {}", authHeader != null ? "Bearer ***" : "null");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String[] parts = token.split(":");

            if (parts.length == 2 && "loremaster2024".equals(parts[1])) {
                String username = parts[0];
                logger.info("Validating token for user: {}", username);

                User user = userService.getByUsername(username);
                if (user != null) {
                    logger.info("User authenticated successfully: {}", username);
                    request.setAttribute("loginUser", user);
                    return true;
                } else {
                    logger.warn("User not found: {}", username);
                }
            } else {
                logger.warn("Invalid token format");
            }
        } else {
            logger.warn("No Authorization header or invalid format");
        }

        logger.warn("Authentication failed for URI: {}", uri);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"未登录或token无效\"}");
        return false;
    }
}