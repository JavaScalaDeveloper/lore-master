package com.lore.master.service.consumer.impl;

import com.lore.master.common.util.JwtUtil;
import com.lore.master.data.dto.UserLoginRequest;
import com.lore.master.data.entity.consumer.ConsumerUser;
import com.lore.master.data.repository.consumer.ConsumerUserRepository;
import com.lore.master.data.vo.UserLoginResponse;
import com.lore.master.service.consumer.ConsumerUserLoginService;
import com.lore.master.service.consumer.factory.ConsumerUserLoginStrategyFactory;
import com.lore.master.service.consumer.strategy.ConsumerUserLoginStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * C端用户登录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerUserLoginServiceImpl implements ConsumerUserLoginService {

    private final ConsumerUserRepository consumerUserRepository;
    private final ConsumerUserLoginStrategyFactory strategyFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponse login(UserLoginRequest request) {
        log.info("开始用户登录，登录类型: {}, 登录标识: {}", request.getLoginType(), request.getLoginKey());

        // 1. 获取登录策略
        ConsumerUserLoginStrategy strategy = strategyFactory.getStrategy(request.getLoginType());

        // 2. 验证请求参数
        strategy.validateRequest(request);

        // 3. 执行认证
        ConsumerUser user = strategy.authenticate(request);

        // 4. 如果认证失败且支持自动注册，则自动注册
        boolean isFirstLogin = false;
        if (user == null && strategy.supportAutoRegister()) {
            user = strategy.autoRegister(request);
            isFirstLogin = true;
            log.info("自动注册新用户，用户ID: {}", user.getUserId());
        }

        // 5. 如果仍然没有用户，说明认证失败
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 6. 更新登录信息
        updateLoginInfo(user, request);

        // 7. 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUserId());
        String refreshToken = JwtUtil.generateRefreshToken(user.getId(), user.getUserId());

        // 8. 构建响应
        UserLoginResponse response = buildLoginResponse(user, token, refreshToken, isFirstLogin);

        log.info("用户登录成功，用户ID: {}, 登录类型: {}", user.getUserId(), request.getLoginType());
        return response;
    }

    @Override
    public UserLoginResponse refreshToken(String refreshToken) {
        try {
            // 验证刷新令牌
            if (!JwtUtil.validateToken(refreshToken)) {
                throw new RuntimeException("刷新令牌无效");
            }

            // 从令牌中获取用户信息
            String userId = JwtUtil.getUserIdFromToken(refreshToken);
            Optional<ConsumerUser> userOpt = consumerUserRepository.findByUserId(userId);
            
            if (userOpt.isEmpty()) {
                throw new RuntimeException("用户不存在");
            }

            ConsumerUser user = userOpt.get();
            
            // 检查用户状态
            if (user.getStatus() != 1) {
                throw new RuntimeException("账号已被禁用");
            }

            // 生成新的访问令牌
            String newToken = JwtUtil.generateToken(user.getId(), user.getUserId());
            String newRefreshToken = JwtUtil.generateRefreshToken(user.getId(), user.getUserId());

            return buildLoginResponse(user, newToken, newRefreshToken, false);

        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage(), e);
            throw new RuntimeException("刷新令牌失败");
        }
    }

    @Override
    public boolean logout(String token) {
        try {
            // TODO: 将令牌加入黑名单（可以使用Redis实现）
            log.info("用户登出成功");
            return true;
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String validateToken(String token) {
        try {
            if (JwtUtil.validateToken(token)) {
                return JwtUtil.getUserIdFromToken(token);
            }
            return null;
        } catch (Exception e) {
            log.warn("令牌验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 更新登录信息
     */
    private void updateLoginInfo(ConsumerUser user, UserLoginRequest request) {
        user.setLoginCount(user.getLoginCount() + 1);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(request.getClientIp());
        consumerUserRepository.save(user);
    }

    /**
     * 构建登录响应
     */
    private UserLoginResponse buildLoginResponse(ConsumerUser user, String token, String refreshToken, boolean isFirstLogin) {
        UserLoginResponse response = new UserLoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(24 * 60 * 60 * 1000L); // 24小时
        response.setRefreshToken(refreshToken);
        response.setIsFirstLogin(isFirstLogin);

        // 构建用户信息
        UserLoginResponse.UserInfo userInfo = new UserLoginResponse.UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setNickname(user.getNickname());
        userInfo.setRealName(user.getRealName());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setGender(user.getGender());
        userInfo.setCurrentLevel(user.getCurrentLevel());
        userInfo.setTotalScore(user.getTotalScore());
        userInfo.setStudyDays(user.getStudyDays());
        userInfo.setStatus(user.getStatus());
        userInfo.setIsVerified(user.getIsVerified());
        userInfo.setLastLoginTime(user.getLastLoginTime());
        userInfo.setLoginCount(user.getLoginCount());

        response.setUserInfo(userInfo);
        return response;
    }
}
