package com.lore.master.data.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * C端用户登录请求DTO
 */
@Data
public class UserLoginRequest {

    /**
     * 登录类型：username、email、phone、wechat等
     */
    @NotBlank(message = "登录类型不能为空")
    private String loginType;

    /**
     * 登录标识（用户名、邮箱、手机号、微信openid等）
     */
    private String loginKey;

    /**
     * 密码（加密后的）
     */
    private String password;

    /**
     * 微信授权码（微信登录时使用）
     */
    private String code;

    /**
     * 微信用户信息（微信登录时使用）
     */
    private WechatUserInfo wechatUserInfo;

    /**
     * 微信openid（用于避免重复调用微信接口）
     */
    private String openid;

    /**
     * 是否记住我
     */
    private Boolean rememberMe = false;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 微信用户信息
     */
    @Data
    public static class WechatUserInfo {
        /**
         * 微信openid
         */
        private String openid;

        /**
         * 微信unionid
         */
        private String unionid;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 头像URL
         */
        private String avatarUrl;

        /**
         * 性别：1男 2女 0未知
         */
        private Integer gender;

        /**
         * 国家
         */
        private String country;

        /**
         * 省份
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 语言
         */
        private String language;
    }
}
