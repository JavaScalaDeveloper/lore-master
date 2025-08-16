package com.lore.master.web.consumer.aspect;

import com.lore.master.common.context.UserContext;
import com.lore.master.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户上下文切面，用于获取当前登录用户信息
 */
@Aspect
@Component
@Slf4j
public class UserContextAspect {

    /**
     * 定义切点：拦截所有controller层需要用户登录的方法
     */
    @Pointcut("@annotation(com.lore.master.common.annotation.RequireLogin)")
    public void loginPointcut() {
    }

    /**
     * 环绕通知：处理用户登录态
     */
    @Around("loginPointcut()")
    public Object aroundLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestURI = request.getRequestURI();

        log.info("🔐 处理需要登录的请求: {}", requestURI);

        // 从请求头中获取token
        String authorization = request.getHeader("Authorization");
        log.info("📋 Authorization头: {}", authorization != null ? "Bearer ***" : "null");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.replace("Bearer ", "");
            log.info("🎫 提取到Token: {}...", token.length() > 10 ? token.substring(0, 10) : token);

            // 验证token并获取用户ID
            boolean isValid = JwtUtil.validateToken(token);
            log.info("✅ Token验证结果: {}", isValid);

            if (isValid) {
                String userId = JwtUtil.getUserIdFromToken(token);
                log.info("👤 从Token解析出用户ID: {}", userId);
                UserContext.setCurrentUserId(userId);
                log.info("🎯 已设置UserContext用户ID: {}", userId);
            } else {
                log.warn("❌ Token验证失败，无法设置用户上下文");
            }
        } else {
            log.warn("❌ 未找到有效的Authorization头");
        }

        try {
            // 执行目标方法
            String currentUserId = UserContext.getCurrentUserId();
            log.info("🚀 执行目标方法，当前用户ID: {}", currentUserId);
            return joinPoint.proceed();
        } finally {
            // 清除ThreadLocal中的用户ID，避免内存泄漏
            UserContext.clear();
            log.debug("🧹 已清除UserContext");
        }
    }
}