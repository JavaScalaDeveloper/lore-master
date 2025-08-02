package com.lore.master.service.consumer;

import com.lore.master.data.dto.UserLoginRequest;
import com.lore.master.data.vo.UserLoginResponse;

/**
 * C端用户登录服务接口
 */
public interface ConsumerUserLoginService {

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应
     */
    UserLoginResponse login(UserLoginRequest request);

    /**
     * 刷新令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    UserLoginResponse refreshToken(String refreshToken);

    /**
     * 用户登出
     * 
     * @param token 访问令牌
     * @return 是否成功
     */
    boolean logout(String token);

    /**
     * 验证令牌
     * 
     * @param token 访问令牌
     * @return 用户ID，如果令牌无效返回null
     */
    String validateToken(String token);

    /**
     * 获取当前用户信息
     * 
     * @param userId 用户ID
     * @return 用户登录响应，包含用户信息
     */
    UserLoginResponse getUserInfo(String userId);
}
