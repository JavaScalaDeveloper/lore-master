package com.lore.master.common.context;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户的信息
 */
@Slf4j
public class UserContext {

    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(String userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清除当前用户上下文
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}