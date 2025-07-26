package com.lore.master.service.consumer.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.data.dto.UserRegisterRequest;
import com.lore.master.data.repository.consumer.ConsumerUserAuthMethodRepository;
import com.lore.master.service.consumer.strategy.ConsumerUserRegisterStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * C端邮箱验证码注册策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerEmailRegisterStrategy implements ConsumerUserRegisterStrategy {

    private final ConsumerUserAuthMethodRepository consumerUserAuthMethodRepository;

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_REGEX = Pattern.compile(EMAIL_PATTERN);

    @Override
    public String getSupportedType() {
        return "email";
    }

    @Override
    public void validateRequest(UserRegisterRequest request) {
        if (StrUtil.isBlank(request.getRegisterKey())) {
            throw new IllegalArgumentException("邮箱地址不能为空");
        }

        if (StrUtil.isBlank(request.getVerifyCode())) {
            throw new IllegalArgumentException("验证码不能为空");
        }

        // 验证邮箱格式
        if (!EMAIL_REGEX.matcher(request.getRegisterKey()).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
    }

    @Override
    public void validateCredential(UserRegisterRequest request) {
        // 验证验证码
        if (!verifyCode(request.getRegisterKey(), request.getVerifyCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }
    }

    @Override
    public boolean isRegisterKeyExists(String registerKey) {
        return consumerUserAuthMethodRepository.existsByAuthTypeAndAuthKey(getSupportedType(), registerKey);
    }

    @Override
    public String generateAuthSecret(UserRegisterRequest request) {
        // 邮箱注册不需要密码，返回null
        return null;
    }

    @Override
    public String getDefaultNickname(UserRegisterRequest request) {
        // 如果用户提供了昵称，使用用户提供的
        if (StrUtil.isNotBlank(request.getNickname())) {
            return request.getNickname();
        }
        
        // 否则使用邮箱用户名部分作为默认昵称
        String email = request.getRegisterKey();
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            return email.substring(0, atIndex);
        }
        
        return "用户" + System.currentTimeMillis() % 10000;
    }

    @Override
    public boolean requiresVerifyCode() {
        return true; // 邮箱注册需要验证码
    }

    @Override
    public boolean sendVerifyCode(String registerKey) {
        try {
            // TODO: 集成邮件服务发送验证码
            // 1. 生成6位数字验证码
            String verifyCode = generateVerifyCode();
            
            // 2. 存储验证码到Redis或数据库，设置5分钟过期
            storeVerifyCode(registerKey, verifyCode);
            
            // 3. 发送邮件
            sendEmail(registerKey, verifyCode);
            
            log.info("邮箱验证码发送成功: {}", registerKey);
            return true;
            
        } catch (Exception e) {
            log.error("邮箱验证码发送失败: {}", registerKey, e);
            return false;
        }
    }

    @Override
    public boolean verifyCode(String registerKey, String verifyCode) {
        try {
            // TODO: 从Redis或数据库验证验证码
            String storedCode = getStoredVerifyCode(registerKey);
            
            if (StrUtil.isBlank(storedCode)) {
                log.warn("验证码不存在或已过期: {}", registerKey);
                return false;
            }
            
            boolean isValid = storedCode.equals(verifyCode);
            
            if (isValid) {
                // 验证成功后删除验证码
                removeVerifyCode(registerKey);
                log.info("邮箱验证码验证成功: {}", registerKey);
            } else {
                log.warn("邮箱验证码验证失败: {}", registerKey);
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("邮箱验证码验证异常: {}", registerKey, e);
            return false;
        }
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerifyCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * 存储验证码
     */
    private void storeVerifyCode(String email, String verifyCode) {
        // TODO: 实现Redis存储逻辑
        log.debug("存储验证码: {} -> {}", email, verifyCode);
    }

    /**
     * 获取存储的验证码
     */
    private String getStoredVerifyCode(String email) {
        // TODO: 实现Redis获取逻辑
        log.debug("获取验证码: {}", email);
        return "123456"; // 临时返回固定验证码，便于测试
    }

    /**
     * 删除验证码
     */
    private void removeVerifyCode(String email) {
        // TODO: 实现Redis删除逻辑
        log.debug("删除验证码: {}", email);
    }

    /**
     * 发送邮件
     */
    private void sendEmail(String email, String verifyCode) {
        // TODO: 实现邮件发送逻辑
        log.info("发送邮件到 {}: 您的验证码是 {}", email, verifyCode);
    }
}
