package com.lore.master.service.business.strategy;

import com.lore.master.data.dto.UserRegisterRequest;

/**
 * 用户注册策略接口（策略模式）
 */
public interface UserRegisterStrategy {
    
    /**
     * 获取支持的注册类型
     */
    String getSupportedType();
    
    /**
     * 验证注册请求
     */
    void validateRequest(UserRegisterRequest request);
    
    /**
     * 验证注册凭证（密码或验证码）
     */
    void validateCredential(UserRegisterRequest request);
    
    /**
     * 检查注册标识是否已存在
     */
    boolean isRegisterKeyExists(String registerKey);
    
    /**
     * 生成认证密钥（密码hash或其他）
     */
    String generateAuthSecret(UserRegisterRequest request);
    
    /**
     * 获取默认昵称
     */
    String getDefaultNickname(UserRegisterRequest request);
}
