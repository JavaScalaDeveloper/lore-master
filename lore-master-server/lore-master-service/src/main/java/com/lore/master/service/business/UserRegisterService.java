package com.lore.master.service.business;

import com.lore.master.data.dto.UserRegisterRequest;
import com.lore.master.data.vo.UserRegisterResponse;

/**
 * 用户注册服务接口
 */
public interface UserRegisterService {
    
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
     * @return 是否可用
     */
    boolean isRegisterKeyAvailable(String registerType, String registerKey);
    
    /**
     * 发送验证码（邮箱、手机注册时使用）
     *
     * @param registerType 注册类型
     * @param registerKey 注册标识
     * @return 是否发送成功
     */
    boolean sendVerifyCode(String registerType, String registerKey);
}
