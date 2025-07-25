package com.lore.master.data.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应VO
 */
@Data
public class LoginResponse {
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * Token类型
     */
    private String tokenType = "Bearer";
    
    /**
     * Token过期时间（毫秒）
     */
    private Long expiresIn;
    
    /**
     * 用户信息
     */
    private UserInfo userInfo;
    
    /**
     * 用户信息内部类
     */
    @Data
    public static class UserInfo {
        /**
         * 用户ID
         */
        private Long id;
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 真实姓名
         */
        private String realName;
        
        /**
         * 邮箱
         */
        private String email;
        
        /**
         * 头像URL
         */
        private String avatarUrl;
        
        /**
         * 角色
         */
        private String role;
        
        /**
         * 权限列表
         */
        private List<String> permissions;
        
        /**
         * 最后登录时间
         */
        private LocalDateTime lastLoginTime;
    }
}
