package com.lore.master.service.consumer.strategy;

import com.lore.master.data.dto.UserRegisterRequest;

/**
 * C端用户注册策略接口
 * 使用策略模式支持多种注册方式
 */
public interface ConsumerUserRegisterStrategy {

    /**
     * 获取支持的注册类型
     * 
     * @return 注册类型（username、email、phone、qq、wechat等）
     */
    String getSupportedType();

    /**
     * 验证注册请求参数
     * 
     * @param request 注册请求
     * @throws IllegalArgumentException 参数验证失败时抛出
     */
    void validateRequest(UserRegisterRequest request);

    /**
     * 验证认证凭据（密码、验证码等）
     * 
     * @param request 注册请求
     * @throws IllegalArgumentException 凭据验证失败时抛出
     */
    void validateCredential(UserRegisterRequest request);

    /**
     * 检查注册标识是否已存在
     * 
     * @param registerKey 注册标识
     * @return true-已存在，false-不存在
     */
    boolean isRegisterKeyExists(String registerKey);

    /**
     * 生成认证密钥（密码hash、token等）
     * 
     * @param request 注册请求
     * @return 认证密钥
     */
    String generateAuthSecret(UserRegisterRequest request);

    /**
     * 获取默认昵称
     * 
     * @param request 注册请求
     * @return 默认昵称
     */
    String getDefaultNickname(UserRegisterRequest request);

    /**
     * 发送验证码（如果需要）
     * 
     * @param registerKey 注册标识
     * @return true-发送成功，false-发送失败
     */
    default boolean sendVerifyCode(String registerKey) {
        return true; // 默认不需要验证码
    }

    /**
     * 验证验证码（如果需要）
     * 
     * @param registerKey 注册标识
     * @param verifyCode 验证码
     * @return true-验证成功，false-验证失败
     */
    default boolean verifyCode(String registerKey, String verifyCode) {
        return true; // 默认不需要验证码
    }

    /**
     * 是否需要验证码
     * 
     * @return true-需要，false-不需要
     */
    default boolean requiresVerifyCode() {
        return false;
    }

    /**
     * 获取第三方用户信息（第三方登录时使用）
     * 
     * @param request 注册请求
     * @return 第三方用户信息
     */
    default Object getThirdPartyUserInfo(UserRegisterRequest request) {
        return null;
    }
}
