package com.lore.master.data.vo;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户注册响应VO
 */
@Data
@Builder
public class UserRegisterResponse {
    
    /**
     * 用户唯一ID
     */
    private String userId;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像链接
     */
    private String avatarUrl;
    
    /**
     * 性别：1男 2女 0未知
     */
    private Integer gender;
    
    /**
     * 当前等级
     */
    private Integer currentLevel;
    
    /**
     * 总积分
     */
    private Integer totalScore;
    
    /**
     * 用户状态：1正常 0禁用 2待激活
     */
    private Integer status;
    
    /**
     * 是否已验证：1已验证 0未验证
     */
    private Integer isVerified;
    
    /**
     * 注册时间
     */
    private LocalDateTime registerTime;
    
    /**
     * 访问令牌（JWT）
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 令牌过期时间
     */
    private LocalDateTime tokenExpireTime;
}
