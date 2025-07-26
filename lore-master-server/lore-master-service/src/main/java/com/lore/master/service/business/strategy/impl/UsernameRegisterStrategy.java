package com.lore.master.service.business.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.common.util.CryptoUtil;
import com.lore.master.common.util.PasswordUtil;
import com.lore.master.data.dto.UserRegisterRequest;
import com.lore.master.data.repository.business.UserAuthMethodRepository;
import com.lore.master.service.business.strategy.UserRegisterStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 用户名注册策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsernameRegisterStrategy implements UserRegisterStrategy {
    
    private final UserAuthMethodRepository userAuthMethodRepository;
    
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{4,20}$";
    private static final Pattern USERNAME_REGEX = Pattern.compile(USERNAME_PATTERN);
    
    @Override
    public String getSupportedType() {
        return "username";
    }
    
    @Override
    public void validateRequest(UserRegisterRequest request) {
        // 验证用户名格式
        if (!USERNAME_REGEX.matcher(request.getRegisterKey()).matches()) {
            throw new IllegalArgumentException("用户名格式不正确，只能包含字母、数字和下划线，长度4-20位");
        }

        // 验证密码不能为空
        if (StrUtil.isBlank(request.getPassword())) {
            throw new IllegalArgumentException("密码不能为空");
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
        return userAuthMethodRepository.existsByAuthTypeAndAuthKey("username", registerKey);
    }
    
    @Override
    public String generateAuthSecret(UserRegisterRequest request) {
        // 使用CryptoUtil解密前端传来的加密密码，再使用PasswordUtil进行BCrypt加密存储
        String plainPassword = CryptoUtil.aesDecrypt(request.getPassword());
        return PasswordUtil.encode(plainPassword);
    }
    
    @Override
    public String getDefaultNickname(UserRegisterRequest request) {
        return StrUtil.isNotBlank(request.getNickname()) 
            ? request.getNickname() 
            : "用户" + request.getRegisterKey();
    }
}
