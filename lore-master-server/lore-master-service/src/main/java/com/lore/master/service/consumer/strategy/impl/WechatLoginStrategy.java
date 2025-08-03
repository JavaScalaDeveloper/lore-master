package com.lore.master.service.consumer.strategy.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.lore.master.common.exception.BusinessException;
import com.lore.master.common.result.ResultCode;
import com.lore.master.common.util.UserIdGenerator;
import com.lore.master.data.dto.UserLoginRequest;
import com.lore.master.data.entity.consumer.ConsumerUser;
import com.lore.master.data.entity.consumer.ConsumerUserAuthMethod;
import com.lore.master.data.repository.consumer.ConsumerUserAuthMethodRepository;
import com.lore.master.data.repository.consumer.ConsumerUserRepository;
import com.lore.master.service.consumer.strategy.ConsumerUserLoginStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 微信登录策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatLoginStrategy implements ConsumerUserLoginStrategy {

    private final ConsumerUserRepository consumerUserRepository;
    private final ConsumerUserAuthMethodRepository consumerUserAuthMethodRepository;
    private final UserIdGenerator userIdGenerator;

    @Value("${wechat.miniapp.appid}")
    private String appid;

    @Value("${wechat.miniapp.secret}")
    private String secret;

    private static final String LOGIN_TYPE = "wechat";
    private static final String WECHAT_CODE_TO_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Override
    public String getType() {
        return LOGIN_TYPE;
    }

    @Override
    public void validateRequest(UserLoginRequest request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "登录请求不能为空");
        }

        String code = request.getCode();
        if (StrUtil.isBlank(code)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "微信登录code不能为空");
        }
    }

    @Override
    public ConsumerUser authenticate(UserLoginRequest request) {
        // 1. 获取openid（优先使用请求中已有的openid，避免重复调用微信接口）
        String openid = request.getOpenid();
        if (StrUtil.isBlank(openid)) {
            openid = getOpenidByCode(request.getCode());
            // 将获取到的openid存储到请求中，供后续流程使用
            request.setOpenid(openid);
        }

        if (StrUtil.isBlank(openid)) {
            log.error("微信登录获取openid失败");
            return null;
        }

        // 2. 根据openid查询用户
        Optional<ConsumerUserAuthMethod> authMethodOpt = consumerUserAuthMethodRepository
                .findByAuthTypeAndAuthKey(LOGIN_TYPE, openid);

        if (authMethodOpt.isPresent()) {
            // 3. 如果找到认证记录，返回关联的用户
            String userId = authMethodOpt.get().getUserId();
            return consumerUserRepository.findByUserId(userId).orElse(null);
        }

        // 4. 未找到用户，返回null
        return null;
    }

    @Override
    public boolean supportAutoRegister() {
        return true;
    }

    @Override
    public ConsumerUser autoRegister(UserLoginRequest request) {
        // 1. 获取openid（优先使用请求中已有的openid，避免重复调用微信接口）
        String openid = request.getOpenid();
        if (StrUtil.isBlank(openid)) {
            openid = getOpenidByCode(request.getCode());
            // 将获取到的openid存储到请求中，供后续流程使用
            request.setOpenid(openid);
        }

        if (StrUtil.isBlank(openid)) {
            throw new BusinessException(ResultCode.ERROR, "微信登录获取openid失败");
        }
        String userId = userIdGenerator.generateUniqueUserId();

        // 2. 创建新用户
        ConsumerUser user = new ConsumerUser();
        // 设置用户基本信息
        user.setNickname(request.getWechatUserInfo() != null ? request.getWechatUserInfo().getNickname() : "微信用户");
        user.setAvatarUrl(request.getWechatUserInfo() != null ? request.getWechatUserInfo().getAvatarUrl() : "");
        user.setGender(request.getWechatUserInfo() != null ? request.getWechatUserInfo().getGender() : 0);
        user.setStatus(1); // 正常状态
        user.setIsVerified(1); // 已验证
        user.setLoginCount(1);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(request.getClientIp());
        user.setUserId(userId);
        // 3. 保存用户
        user = consumerUserRepository.save(user);

        // 4. 创建认证记录
        ConsumerUserAuthMethod authMethod = new ConsumerUserAuthMethod();
        authMethod.setUserId(user.getUserId());
        authMethod.setAuthType(LOGIN_TYPE);
        authMethod.setAuthKey(openid);
        authMethod.setStatus(1);
        consumerUserAuthMethodRepository.save(authMethod);

        return user;
    }

    /**
     * 使用微信登录code换取openid
     */
    private String getOpenidByCode(String code) {
        if (StringUtils.isBlank(secret)) {
            secret = System.getenv("WECHAT_MINIAPP_SECRET");
        }
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("appid", appid);
            params.put("secret", secret);
            params.put("js_code", code);
            params.put("grant_type", "authorization_code");

            log.info("准备调用微信code2session接口: appid={}, code={}", appid, code);

            // 发送请求
            String response = HttpUtil.get(WECHAT_CODE_TO_SESSION_URL, params);
            log.info("微信code2session响应: {}", response);

            // 解析响应
            JSONObject jsonObject = JSON.parseObject(response);
            if (jsonObject.containsKey("errcode")) {
                int errCode = jsonObject.getIntValue("errcode");
                String errMsg = jsonObject.getString("errmsg");
                log.error("微信登录失败: errcode={}, errmsg={}, code={}", errCode, errMsg, code);

                // 根据不同错误码给出更具体的错误提示
                String errorDetail;
                switch (errCode) {
                    case 40029:
                        errorDetail = "code无效";
                        break;
                    case 40163:
                        errorDetail = "code已被使用";
                        break;
                    case 40013:
                        errorDetail = "appid无效";
                        break;
                    default:
                        errorDetail = "未知错误";
                }
                throw new BusinessException(ResultCode.ERROR, "微信登录获取openid失败: " + errorDetail + "(" + errCode + ":" + errMsg + ")");
            }

            String openid = jsonObject.getString("openid");
            if (StrUtil.isBlank(openid)) {
                log.error("微信登录响应中未包含openid: {}", response);
                throw new BusinessException(ResultCode.ERROR, "微信登录响应中未包含openid");
            }

            return openid;
        } catch (BusinessException e) {
            // 直接抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("微信登录异常: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.ERROR, "微信登录获取openid失败: " + e.getMessage());
        }
    }

// 删除updateLoginInfo方法
}
