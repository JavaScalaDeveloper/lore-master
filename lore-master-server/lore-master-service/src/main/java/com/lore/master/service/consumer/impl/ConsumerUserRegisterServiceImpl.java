package com.lore.master.service.consumer.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.common.util.UserIdGenerator;
import com.lore.master.data.dto.UserRegisterRequest;
import com.lore.master.data.entity.consumer.ConsumerUser;
import com.lore.master.data.entity.consumer.ConsumerUserAuthMethod;
import com.lore.master.data.repository.consumer.ConsumerUserAuthMethodRepository;
import com.lore.master.data.repository.consumer.ConsumerUserRepository;
import com.lore.master.data.vo.UserRegisterResponse;
import com.lore.master.service.consumer.ConsumerUserRegisterService;
import com.lore.master.service.consumer.factory.ConsumerUserRegisterStrategyFactory;
import com.lore.master.service.consumer.strategy.ConsumerUserRegisterStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * C端用户注册服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerUserRegisterServiceImpl implements ConsumerUserRegisterService {

    private final ConsumerUserRepository consumerUserRepository;
    private final ConsumerUserAuthMethodRepository consumerUserAuthMethodRepository;
    private final ConsumerUserRegisterStrategyFactory strategyFactory;
    private final UserIdGenerator userIdGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRegisterResponse register(UserRegisterRequest request) {
        log.info("开始用户注册，注册类型: {}, 注册标识: {}", request.getRegisterType(), request.getRegisterKey());

        // 1. 获取注册策略
        ConsumerUserRegisterStrategy strategy = strategyFactory.getStrategy(request.getRegisterType());

        // 2. 验证请求参数
        strategy.validateRequest(request);

        // 3. 验证认证凭据
        strategy.validateCredential(request);

        // 4. 检查注册标识是否已存在
        if (strategy.isRegisterKeyExists(request.getRegisterKey())) {
            throw new IllegalArgumentException("该账号已被注册");
        }

        // 5. 创建用户
        ConsumerUser user = createUser(strategy, request);
        log.info("保存用户前，userId: {}", user.getUserId());
        user = consumerUserRepository.save(user);
        log.info("保存用户后，userId: {}", user.getUserId());

        // 6. 创建认证方式
        ConsumerUserAuthMethod authMethod = createAuthMethod(strategy, request, user.getUserId());
        consumerUserAuthMethodRepository.save(authMethod);

        // 7. 构建响应
        UserRegisterResponse response = buildResponse(user, authMethod);

        log.info("用户注册成功，用户ID: {}, 注册类型: {}", user.getUserId(), request.getRegisterType());
        return response;
    }

    @Override
    public boolean isRegisterKeyAvailable(String registerType, String registerKey) {
        try {
            ConsumerUserRegisterStrategy strategy = strategyFactory.getStrategy(registerType);
            return !strategy.isRegisterKeyExists(registerKey);
        } catch (Exception e) {
            log.error("检查注册标识可用性失败", e);
            return false;
        }
    }

    @Override
    public boolean sendVerifyCode(String registerType, String registerKey) {
        try {
            ConsumerUserRegisterStrategy strategy = strategyFactory.getStrategy(registerType);

            if (!strategy.requiresVerifyCode()) {
                log.warn("注册类型 {} 不需要验证码", registerType);
                return false;
            }

            return strategy.sendVerifyCode(registerKey);

        } catch (Exception e) {
            log.error("发送验证码失败", e);
            return false;
        }
    }

    @Override
    public boolean verifyCode(String registerType, String registerKey, String verifyCode) {
        try {
            ConsumerUserRegisterStrategy strategy = strategyFactory.getStrategy(registerType);
            return strategy.verifyCode(registerKey, verifyCode);
        } catch (Exception e) {
            log.error("验证验证码失败", e);
            return false;
        }
    }

    /**
     * 创建用户
     */
    private ConsumerUser createUser(ConsumerUserRegisterStrategy strategy, UserRegisterRequest request) {
        log.info("开始创建用户");
        ConsumerUser user = new ConsumerUser();
        log.info("创建用户对象成功");
        
        // 生成并设置用户ID
        log.info("开始生成用户ID");
        String userId = userIdGenerator.generateUniqueUserId();
        log.info("生成的用户ID: {}, 长度: {}, 是否为空: {}", userId, userId != null ? userId.length() : 0, userId == null || userId.isEmpty());
        
        if (userId == null || userId.isEmpty()) {
            log.error("生成用户ID失败，userId为空");
            throw new RuntimeException("生成用户ID失败，userId为空");
        }
        
        user.setUserId(userId);
        log.info("用户ID设置成功: {}, 设置后用户对象中的userId: {}", userId, user.getUserId());
        
        // 验证设置是否成功
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            log.error("用户ID设置失败，设置后userId为空");
            throw new RuntimeException("用户ID设置失败，设置后userId为空");
        }
        
        user.setNickname(strategy.getDefaultNickname(request));
        log.info("设置用户昵称: {}", user.getNickname());
        
        // 设置默认值
        user.setCurrentLevel(1);
        user.setTotalScore(0);
        user.setStudyDays(0);
        user.setStatus(1); // 正常状态
        user.setIsVerified(1); // 已验证
        user.setLoginCount(0);
        log.info("设置用户默认属性完成");
        
        // 设置客户端信息
        if (StrUtil.isNotBlank(request.getClientIp())) {
            user.setLastLoginIp(request.getClientIp());
        }
        
        // 再次检查userId是否为空
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            log.error("用户对象中的userId为空");
            throw new RuntimeException("用户对象中的userId为空");
        }
        
        return user;
    }

    /**
     * 创建认证方式
     */
    private ConsumerUserAuthMethod createAuthMethod(ConsumerUserRegisterStrategy strategy, UserRegisterRequest request, String userId) {
        ConsumerUserAuthMethod authMethod = new ConsumerUserAuthMethod();
        authMethod.setUserId(userId);
        authMethod.setAuthType(strategy.getSupportedType());
        authMethod.setAuthKey(request.getRegisterKey());
        authMethod.setAuthSecret(strategy.generateAuthSecret(request));
        
        // 设置为主要认证方式
        authMethod.setIsPrimary(1);
        authMethod.setIsVerified(1);
        authMethod.setStatus(1);
        authMethod.setLastUsedTime(LocalDateTime.now());
        
        // 处理第三方登录信息
        Object thirdPartyInfo = strategy.getThirdPartyUserInfo(request);
        if (thirdPartyInfo != null) {
            // TODO: 处理第三方用户信息
            log.debug("第三方用户信息: {}", thirdPartyInfo);
        }
        
        return authMethod;
    }

    /**
     * 构建响应
     */
    private UserRegisterResponse buildResponse(ConsumerUser user, ConsumerUserAuthMethod authMethod) {
        // 简化响应对象创建，只包含基本信息
        return UserRegisterResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .currentLevel(user.getCurrentLevel())
                .totalScore(user.getTotalScore())
                .registerTime(user.getCreateTime())
                .build();
    }
}
