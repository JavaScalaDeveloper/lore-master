package com.lore.master.service.consumer.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.common.util.CryptoUtil;
import com.lore.master.data.dto.UserLoginRequest;
import com.lore.master.data.entity.consumer.ConsumerUser;
import com.lore.master.data.entity.consumer.ConsumerUserAuthMethod;
import com.lore.master.data.repository.consumer.ConsumerUserAuthMethodRepository;
import com.lore.master.data.repository.consumer.ConsumerUserRepository;
import com.lore.master.service.consumer.strategy.ConsumerUserLoginStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 用户名密码登录策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsernamePasswordLoginStrategy implements ConsumerUserLoginStrategy {

    private final ConsumerUserRepository consumerUserRepository;
    private final ConsumerUserAuthMethodRepository consumerUserAuthMethodRepository;

    @Override
    public String getType() {
        return "username";
    }

    @Override
    public void validateRequest(UserLoginRequest request) {
        if (StrUtil.isBlank(request.getLoginKey())) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        if (StrUtil.isBlank(request.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }
    }

    @Override
    public ConsumerUser authenticate(UserLoginRequest request) {
        String loginKey = request.getLoginKey();
        String encryptedPassword = request.getPassword();

        // 根据用户名查找认证方式
        Optional<ConsumerUserAuthMethod> authMethodOpt = consumerUserAuthMethodRepository
                .findByAuthTypeAndAuthKey("username", loginKey);

        if (authMethodOpt.isEmpty()) {
            log.warn("用户名不存在: {}", loginKey);
            return null;
        }

        ConsumerUserAuthMethod authMethod = authMethodOpt.get();
        
        // 验证密码
        if (!CryptoUtil.verifyPassword(encryptedPassword, authMethod.getAuthSecret())) {
            log.warn("密码错误，用户名: {}", loginKey);
            return null;
        }

        // 查找用户
        Optional<ConsumerUser> userOpt = consumerUserRepository.findByUserId(authMethod.getUserId());
        if (userOpt.isEmpty()) {
            log.error("认证方式存在但用户不存在，用户ID: {}", authMethod.getUserId());
            return null;
        }

        ConsumerUser user = userOpt.get();
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        return user;
    }

    @Override
    public boolean supportAutoRegister() {
        return false; // 用户名密码登录不支持自动注册
    }
}
