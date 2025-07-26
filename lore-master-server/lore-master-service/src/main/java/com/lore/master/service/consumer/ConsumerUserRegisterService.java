package com.lore.master.service.consumer;

import com.lore.master.data.dto.UserRegisterRequest;
import com.lore.master.data.vo.UserRegisterResponse;

/**
 * C端用户注册服务接口
 */
public interface ConsumerUserRegisterService {

    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 注册响应
     */
    UserRegisterResponse register(UserRegisterRequest request);

    /**
     * 检查注册标识是否可用
     * 
     * @param registerType 注册类型
     * @param registerKey 注册标识
     * @return true-可用，false-不可用
     */
    boolean isRegisterKeyAvailable(String registerType, String registerKey);

    /**
     * 发送验证码
     * 
     * @param registerType 注册类型
     * @param registerKey 注册标识
     * @return true-发送成功，false-发送失败
     */
    boolean sendVerifyCode(String registerType, String registerKey);

    /**
     * 验证验证码
     * 
     * @param registerType 注册类型
     * @param registerKey 注册标识
     * @param verifyCode 验证码
     * @return true-验证成功，false-验证失败
     */
    boolean verifyCode(String registerType, String registerKey, String verifyCode);
}
