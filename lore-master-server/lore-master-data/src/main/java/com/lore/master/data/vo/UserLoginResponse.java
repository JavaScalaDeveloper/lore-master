package com.lore.master.data.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * C端用户登录响应VO
 */
@Data
public class UserLoginResponse {

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间（毫秒）
     */
    private Long expiresIn;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    /**
     * 是否首次登录
     */
    private Boolean isFirstLogin = false;

    /**
     * 用户信息
     */
    @Data
    public static class UserInfo {
        /**
         * 用户ID
         */
        private String userId;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 真实姓名
         */
        private String realName;

        /**
         * 头像URL
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
         * 学习天数
         */
        private Integer studyDays;

        /**
         * 状态：1正常 0禁用 2待激活
         */
        private Integer status;

        /**
         * 是否已验证：1已验证 0未验证
         */
        private Integer isVerified;

        /**
         * 最后登录时间
         */
        private LocalDateTime lastLoginTime;

        /**
         * 登录次数
         */
        private Integer loginCount;
    }
}
