package com.lore.master.service.business.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.data.dto.UserRegisterRequest;
import com.lore.master.data.repository.business.UserAuthMethodRepository;
import com.lore.master.service.business.strategy.UserRegisterStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 邮箱注册策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailRegisterStrategy implements UserRegisterStrategy {
    
    private final UserAuthMethodRepository userAuthMethodRepository;
    private final StringRedisTemplate redisTemplate;
    
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
    private static final Pattern EMAIL_REGEX = Pattern.compile(EMAIL_PATTERN);
    private static final String VERIFY_CODE_KEY_PREFIX = "email_verify_code:";
    
    @Override
    public String getSupportedType() {
        return "email";
    }
    
    @Override
    public void validateRequest(UserRegisterRequest request) {
        // 验证邮箱格式
        if (!EMAIL_REGEX.matcher(request.getRegisterKey()).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        
        // 验证验证码不能为空
        if (StrUtil.isBlank(request.getVerifyCode())) {
            throw new IllegalArgumentException("验证码不能为空");
        }
    }
    
    @Override
    public void validateCredential(UserRegisterRequest request) {
        // 邮箱注册需要验证验证码
        String email = request.getRegisterKey();
        String verifyCode = request.getVerifyCode();
        
        // 从Redis获取验证码
        String redisKey = VERIFY_CODE_KEY_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        
        if (StrUtil.isBlank(storedCode)) {
            throw new IllegalArgumentException("验证码已过期，请重新获取");
        }
        
        if (!storedCode.equals(verifyCode)) {
            throw new IllegalArgumentException("验证码错误");
        }
        
        // 验证成功后删除验证码
        redisTemplate.delete(redisKey);
        log.info("邮箱验证码验证成功: {}", email);
    }
    
    @Override
    public boolean isRegisterKeyExists(String registerKey) {
        return userAuthMethodRepository.existsByAuthTypeAndAuthKey("email", registerKey);
    }
    
    @Override
    public String generateAuthSecret(UserRegisterRequest request) {
        // 邮箱注册不需要密码，返回null
        return null;
    }
    
    @Override
    public String getDefaultNickname(UserRegisterRequest request) {
        if (StrUtil.isNotBlank(request.getNickname())) {
            return request.getNickname();
        }
        
        // 从邮箱提取用户名作为默认昵称
        String email = request.getRegisterKey();
        String username = email.substring(0, email.indexOf("@"));
        return "用户" + username;
    }
}
