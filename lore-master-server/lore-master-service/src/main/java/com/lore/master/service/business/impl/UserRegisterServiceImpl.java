package com.lore.master.service.business.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.service.config.UserIdGenerator;
import com.lore.master.data.dto.UserRegisterRequest;
import com.lore.master.data.entity.business.User;
import com.lore.master.data.entity.business.UserAuthMethod;
import com.lore.master.data.repository.business.UserAuthMethodRepository;
import com.lore.master.data.repository.business.UserRepository;
import com.lore.master.data.vo.UserRegisterResponse;
import com.lore.master.service.business.UserRegisterService;
import com.lore.master.service.business.factory.UserRegisterStrategyFactory;
import com.lore.master.service.business.strategy.UserRegisterStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户注册服务实现类（模板方法模式）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegisterServiceImpl implements UserRegisterService {
    
    private final UserRepository userRepository;
    private final UserAuthMethodRepository userAuthMethodRepository;
    private final UserRegisterStrategyFactory strategyFactory;
    private final UserIdGenerator userIdGenerator;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRegisterResponse register(UserRegisterRequest request) {
        log.info("开始用户注册流程: registerType={}, registerKey={}", 
                request.getRegisterType(), request.getRegisterKey());
        
        // 模板方法模式：定义注册流程的骨架
        try {
            // 1. 获取注册策略
            UserRegisterStrategy strategy = getRegisterStrategy(request);
            
            // 2. 验证注册请求
            validateRegisterRequest(strategy, request);
            
            // 3. 验证注册凭证
            validateRegisterCredential(strategy, request);
            
            // 4. 检查注册标识是否已存在
            checkRegisterKeyExists(strategy, request);
            
            // 5. 创建用户
            User user = createUser(strategy, request);
            
            // 6. 创建登录方式
            UserAuthMethod authMethod = createAuthMethod(strategy, request, user.getUserId());
            
            // 7. 记录注册日志
            logRegisterOperation(request, user, true, null);
            
            // 8. 构建响应
            UserRegisterResponse response = buildRegisterResponse(user, authMethod);
            
            log.info("用户注册成功: userId={}, registerType={}", 
                    user.getUserId(), request.getRegisterType());
            
            return response;
            
        } catch (Exception e) {
            log.error("用户注册失败: registerType={}, registerKey={}, error={}", 
                    request.getRegisterType(), request.getRegisterKey(), e.getMessage(), e);
            
            // 记录失败日志
            logRegisterOperation(request, null, false, e.getMessage());
            
            throw e;
        }
    }
    
    @Override
    public boolean isRegisterKeyAvailable(String registerType, String registerKey) {
        try {
            UserRegisterStrategy strategy = strategyFactory.getStrategy(registerType);
            return !strategy.isRegisterKeyExists(registerKey);
        } catch (Exception e) {
            log.error("检查注册标识可用性失败: registerType={}, registerKey={}", 
                    registerType, registerKey, e);
            return false;
        }
    }
    
    @Override
    public boolean sendVerifyCode(String registerType, String registerKey) {
        // TODO: 实现验证码发送逻辑
        log.info("发送验证码: registerType={}, registerKey={}", registerType, registerKey);
        return true;
    }
    
    /**
     * 获取注册策略
     */
    private UserRegisterStrategy getRegisterStrategy(UserRegisterRequest request) {
        return strategyFactory.getStrategy(request.getRegisterType());
    }
    
    /**
     * 验证注册请求
     */
    private void validateRegisterRequest(UserRegisterStrategy strategy, UserRegisterRequest request) {
        strategy.validateRequest(request);
    }
    
    /**
     * 验证注册凭证
     */
    private void validateRegisterCredential(UserRegisterStrategy strategy, UserRegisterRequest request) {
        strategy.validateCredential(request);
    }
    
    /**
     * 检查注册标识是否已存在
     */
    private void checkRegisterKeyExists(UserRegisterStrategy strategy, UserRegisterRequest request) {
        if (strategy.isRegisterKeyExists(request.getRegisterKey())) {
            throw new IllegalArgumentException("该" + request.getRegisterType() + "已被注册");
        }
    }
    
    /**
     * 创建用户
     */
    private User createUser(UserRegisterStrategy strategy, UserRegisterRequest request) {
        User user = new User();
        user.setUserId(userIdGenerator.generateUniqueUserId());
        user.setNickname(strategy.getDefaultNickname(request));
        
        // 设置性别
        if (StrUtil.isNotBlank(request.getGender())) {
            user.setGender(Integer.valueOf(request.getGender()));
        } else {
            user.setGender(0); // 默认未知
        }
        
        // 设置默认值
        user.setCurrentLevel(1);
        user.setTotalScore(0);
        user.setStudyDays(0);
        user.setLoginCount(0);
        user.setStatus(1); // 正常状态
        user.setIsVerified("email".equals(request.getRegisterType()) ? 1 : 0);
        
        return userRepository.save(user);
    }
    
    /**
     * 创建登录方式
     */
    private UserAuthMethod createAuthMethod(UserRegisterStrategy strategy, 
                                          UserRegisterRequest request, String userId) {
        UserAuthMethod authMethod = new UserAuthMethod();
        authMethod.setUserId(userId);
        authMethod.setAuthType(request.getRegisterType());
        authMethod.setAuthKey(request.getRegisterKey());
        authMethod.setAuthSecret(strategy.generateAuthSecret(request));
        authMethod.setIsVerified("email".equals(request.getRegisterType()) ? 1 : 0);
        authMethod.setIsPrimary(1); // 注册时的登录方式为主要方式
        authMethod.setStatus(1);
        
        return userAuthMethodRepository.save(authMethod);
    }
    
    /**
     * 记录注册操作日志
     */
    private void logRegisterOperation(UserRegisterRequest request, User user, 
                                    boolean success, String errorMessage) {
        // TODO: 实现操作日志记录
        log.info("记录注册日志: userId={}, registerType={}, success={}, error={}", 
                user != null ? user.getUserId() : null, 
                request.getRegisterType(), success, errorMessage);
    }
    
    /**
     * 构建注册响应
     */
    private UserRegisterResponse buildRegisterResponse(User user, UserAuthMethod authMethod) {
        return UserRegisterResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .currentLevel(user.getCurrentLevel())
                .totalScore(user.getTotalScore())
                .status(user.getStatus())
                .isVerified(user.getIsVerified())
                .registerTime(user.getCreateTime())
                // TODO: 生成JWT令牌
                .accessToken("mock_access_token")
                .refreshToken("mock_refresh_token")
                .tokenExpireTime(LocalDateTime.now().plusHours(24))
                .build();
    }
}
