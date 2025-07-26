package com.lore.master.service.consumer.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.common.util.CryptoUtil;
import com.lore.master.common.util.PasswordUtil;
import com.lore.master.data.dto.UserRegisterRequest;
import com.lore.master.data.repository.consumer.ConsumerUserAuthMethodRepository;
import com.lore.master.service.consumer.strategy.ConsumerUserRegisterStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * C端用户名密码注册策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerUsernameRegisterStrategy implements ConsumerUserRegisterStrategy {

    private final ConsumerUserAuthMethodRepository consumerUserAuthMethodRepository;

    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{4,20}$";
    private static final Pattern USERNAME_REGEX = Pattern.compile(USERNAME_PATTERN);

    @Override
    public String getSupportedType() {
        return "username";
    }

    @Override
    public void validateRequest(UserRegisterRequest request) {
        if (StrUtil.isBlank(request.getRegisterKey())) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        if (StrUtil.isBlank(request.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
        }

        // 验证用户名格式
        if (!USERNAME_REGEX.matcher(request.getRegisterKey()).matches()) {
            throw new IllegalArgumentException("用户名格式不正确，只能包含字母、数字和下划线，长度4-20位");
        }
    }

    @Override
    public void validateCredential(UserRegisterRequest request) {
        // 使用CryptoUtil解密密码
        String password = CryptoUtil.aesDecrypt(request.getPassword());
        
        if (password.length() < 6 || password.length() > 20) {
            throw new IllegalArgumentException("密码长度必须在6-20个字符之间");
        }
        
        // 检查密码复杂度（至少包含字母和数字）
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("密码必须包含字母和数字");
        }
    }

    @Override
    public boolean isRegisterKeyExists(String registerKey) {
        return consumerUserAuthMethodRepository.existsByAuthTypeAndAuthKey(getSupportedType(), registerKey);
    }

    @Override
    public String generateAuthSecret(UserRegisterRequest request) {
        // 使用CryptoUtil解密前端传来的加密密码，再使用PasswordUtil进行BCrypt加密存储
        String plainPassword = CryptoUtil.aesDecrypt(request.getPassword());
        return PasswordUtil.encode(plainPassword);
    }

    @Override
    public String getDefaultNickname(UserRegisterRequest request) {
        // 如果用户提供了昵称，使用用户提供的
        if (StrUtil.isNotBlank(request.getNickname())) {
            return request.getNickname();
        }
        
        // 否则使用用户名作为默认昵称
        return request.getRegisterKey();
    }

    @Override
    public boolean requiresVerifyCode() {
        return false; // 用户名密码注册不需要验证码
    }
}
