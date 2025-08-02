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

        // 从请求头中获取token
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.replace("Bearer ", "");

            // 验证token并获取用户ID
            if (JwtUtil.validateToken(token)) {
                String userId = JwtUtil.getUserIdFromToken(token);
                UserContext.setCurrentUserId(userId);
                log.debug("设置当前用户ID: {}", userId);
            }
        }

        try {
            // 执行目标方法
            return joinPoint.proceed();
        } finally {
            // 清除ThreadLocal中的用户ID，避免内存泄漏
            UserContext.clear();
        }
    }
}