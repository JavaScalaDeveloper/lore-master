package com.lore.master.service.consumer.strategy;

import com.lore.master.data.dto.UserLoginRequest;
import com.lore.master.data.entity.consumer.ConsumerUser;

/**
 * C端用户登录策略接口
 */
public interface ConsumerUserLoginStrategy {

    /**
     * 获取策略类型
     * 
     * @return 策略类型
     */
    String getType();

    /**
     * 验证登录请求
     * 
     * @param request 登录请求
     */
    void validateRequest(UserLoginRequest request);

    /**
     * 执行登录认证
     * 
     * @param request 登录请求
     * @return 用户信息，如果认证失败返回null
     */
    ConsumerUser authenticate(UserLoginRequest request);

    /**
     * 是否支持自动注册
     * 
     * @return true-支持，false-不支持
     */
    boolean supportAutoRegister();

    /**
     * 自动注册用户（仅在supportAutoRegister返回true时调用）
     * 
     * @param request 登录请求
     * @return 新注册的用户
     */
    default ConsumerUser autoRegister(UserLoginRequest request) {
        throw new UnsupportedOperationException("该登录方式不支持自动注册");
    }
}
