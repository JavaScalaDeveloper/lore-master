package com.lore.master.service.consumer.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.common.util.UserIdGenerator;
import com.lore.master.data.dto.UserLoginRequest;
import com.lore.master.data.entity.consumer.ConsumerUser;
import com.lore.master.data.entity.consumer.ConsumerUserAuthMethod;
import com.lore.master.data.repository.consumer.ConsumerUserAuthMethodRepository;
import com.lore.master.data.repository.consumer.ConsumerUserRepository;
import com.lore.master.service.consumer.strategy.ConsumerUserLoginStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 微信登录策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatLoginStrategy implements ConsumerUserLoginStrategy {

    private final ConsumerUserRepository consumerUserRepository;
    private final ConsumerUserAuthMethodRepository consumerUserAuthMethodRepository;
    private final UserIdGenerator userIdGenerator;

    @Override
    public String getType() {
        return "wechat";
    }

    @Override
    public void validateRequest(UserLoginRequest request) {
        if (request.getWechatUserInfo() == null) {
            throw new IllegalArgumentException("微信用户信息不能为空");
        }

        UserLoginRequest.WechatUserInfo wechatInfo = request.getWechatUserInfo();
        if (StrUtil.isBlank(wechatInfo.getOpenid())) {
            throw new IllegalArgumentException("微信openid不能为空");
        }

        if (StrUtil.isBlank(wechatInfo.getNickname())) {
            throw new IllegalArgumentException("微信昵称不能为空");
        }
    }

    @Override
    public ConsumerUser authenticate(UserLoginRequest request) {
        UserLoginRequest.WechatUserInfo wechatInfo = request.getWechatUserInfo();
        String openid = wechatInfo.getOpenid();

        // 根据openid查找认证方式
        Optional<ConsumerUserAuthMethod> authMethodOpt = consumerUserAuthMethodRepository
                .findByAuthTypeAndAuthKey("wechat", openid);

        if (authMethodOpt.isPresent()) {
            // 已存在用户，直接返回
            ConsumerUserAuthMethod authMethod = authMethodOpt.get();
            Optional<ConsumerUser> userOpt = consumerUserRepository.findByUserId(authMethod.getUserId());
            
            if (userOpt.isPresent()) {
                ConsumerUser user = userOpt.get();
                // 更新微信用户信息
                updateWechatUserInfo(user, wechatInfo);
                return consumerUserRepository.save(user);
            }
        }

        return null; // 用户不存在，需要自动注册
    }

    @Override
    public boolean supportAutoRegister() {
        return true;
    }

    @Override
    public ConsumerUser autoRegister(UserLoginRequest request) {
        UserLoginRequest.WechatUserInfo wechatInfo = request.getWechatUserInfo();
        
        log.info("开始微信自动注册，openid: {}, nickname: {}", 
                wechatInfo.getOpenid(), wechatInfo.getNickname());

        // 创建用户
        ConsumerUser user = new ConsumerUser();
        user.setUserId(userIdGenerator.generateUniqueUserId());
        user.setNickname(wechatInfo.getNickname());
        user.setAvatarUrl(wechatInfo.getAvatarUrl());
        user.setGender(wechatInfo.getGender());
        
        // 设置默认值
        user.setCurrentLevel(1);
        user.setTotalScore(0);
        user.setStudyDays(0);
        user.setStatus(1); // 正常状态
        user.setIsVerified(1); // 微信用户默认已验证
        user.setLoginCount(0);

        // 保存用户
        user = consumerUserRepository.save(user);

        // 创建微信认证方式
        ConsumerUserAuthMethod authMethod = new ConsumerUserAuthMethod();
        authMethod.setUserId(user.getUserId());
        authMethod.setAuthType("wechat");
        authMethod.setAuthKey(wechatInfo.getOpenid());
        authMethod.setUnionId(wechatInfo.getUnionid()); // 将unionid存储在union_id字段中
        authMethod.setIsVerified(1);
        authMethod.setStatus(1);
        
        // 设置扩展信息
        String extraInfo = buildWechatExtraInfo(wechatInfo);
        authMethod.setExtraInfo(extraInfo);

        consumerUserAuthMethodRepository.save(authMethod);

        log.info("微信自动注册成功，用户ID: {}, openid: {}", user.getUserId(), wechatInfo.getOpenid());
        return user;
    }

    /**
     * 更新微信用户信息
     */
    private void updateWechatUserInfo(ConsumerUser user, UserLoginRequest.WechatUserInfo wechatInfo) {
        boolean needUpdate = false;

        // 更新昵称（如果变化）
        if (!StrUtil.equals(user.getNickname(), wechatInfo.getNickname())) {
            user.setNickname(wechatInfo.getNickname());
            needUpdate = true;
        }

        // 更新头像（如果变化）
        if (!StrUtil.equals(user.getAvatarUrl(), wechatInfo.getAvatarUrl())) {
            user.setAvatarUrl(wechatInfo.getAvatarUrl());
            needUpdate = true;
        }

        // 更新性别（如果变化）
        if (!user.getGender().equals(wechatInfo.getGender())) {
            user.setGender(wechatInfo.getGender());
            needUpdate = true;
        }

        if (needUpdate) {
            log.info("更新微信用户信息，用户ID: {}", user.getUserId());
        }
    }

    /**
     * 构建微信扩展信息JSON
     */
    private String buildWechatExtraInfo(UserLoginRequest.WechatUserInfo wechatInfo) {
        return String.format(
                "{\"country\":\"%s\",\"province\":\"%s\",\"city\":\"%s\",\"language\":\"%s\"}",
                StrUtil.nullToEmpty(wechatInfo.getCountry()),
                StrUtil.nullToEmpty(wechatInfo.getProvince()),
                StrUtil.nullToEmpty(wechatInfo.getCity()),
                StrUtil.nullToEmpty(wechatInfo.getLanguage())
        );
    }
}
