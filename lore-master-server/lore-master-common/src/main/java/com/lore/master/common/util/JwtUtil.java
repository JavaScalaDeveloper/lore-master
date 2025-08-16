package com.lore.master.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
public class JwtUtil {
    
    /**
     * 默认密钥
     */
    private static final String DEFAULT_SECRET = "lore-master-jwt-secret-2024";
    
    /**
     * 默认过期时间（24小时）
     */
    private static final long DEFAULT_EXPIRE_TIME = 24 * 60 * 60 * 1000L;
    
    /**
     * 生成JWT token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return token
     */
    public static String generateToken(Long userId, String username) {
        return generateToken(userId, username, DEFAULT_SECRET, DEFAULT_EXPIRE_TIME);
    }
    
    /**
     * 生成JWT token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param secret 密钥
     * @param expireTime 过期时间（毫秒）
     * @return token
     */
    public static String generateToken(Long userId, String username, String secret, long expireTime) {
        try {
            Date expireDate = new Date(System.currentTimeMillis() + expireTime);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            
            return JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withIssuedAt(new Date())
                    .withExpiresAt(expireDate)
                    .sign(algorithm);
        } catch (Exception e) {
            log.error("生成JWT token失败", e);
            return null;
        }
    }
    
    /**
     * 验证token是否有效
     *
     * @param token token
     * @return 是否有效
     */
    public static boolean verifyToken(String token) {
        return verifyToken(token, DEFAULT_SECRET);
    }
    
    /**
     * 验证token是否有效
     *
     * @param token token
     * @param secret 密钥
     * @return 是否有效
     */
    public static boolean verifyToken(String token, String secret) {
        try {
            if (StrUtil.isBlank(token)) {
                return false;
            }
            
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.warn("JWT token验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取token中的用户ID
     *
     * @param token token
     * @return 用户ID
     */
    public static Long getUserId(String token) {
        try {
            if (StrUtil.isBlank(token)) {
                return null;
            }
            
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asLong();
        } catch (JWTDecodeException e) {
            log.warn("解析JWT token失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取token中的用户名
     *
     * @param token token
     * @return 用户名
     */
    public static String getUsername(String token) {
        try {
            if (StrUtil.isBlank(token)) {
                return null;
            }
            
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        } catch (JWTDecodeException e) {
            log.warn("解析JWT token失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取token过期时间
     *
     * @param token token
     * @return 过期时间
     */
    public static Date getExpireTime(String token) {
        try {
            if (StrUtil.isBlank(token)) {
                return null;
            }
            
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt();
        } catch (JWTDecodeException e) {
            log.warn("解析JWT token失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 判断token是否过期
     *
     * @param token token
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        Date expireTime = getExpireTime(token);
        if (expireTime == null) {
            return true;
        }
        return expireTime.before(new Date());
    }

    /**
     * 生成刷新令牌
     *
     * @param userId 用户ID
     * @param username 用户名
     * @return 刷新令牌
     */
    public static String generateRefreshToken(Long userId, String username) {
        // 刷新令牌有效期为7天
        long refreshExpireTime = 7 * 24 * 60 * 60 * 1000L;
        return generateToken(userId, username, DEFAULT_SECRET, refreshExpireTime);
    }

    /**
     * 验证令牌（别名方法，兼容性）
     *
     * @param token token
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        return verifyToken(token);
    }

    /**
     * 从令牌中获取用户ID（字符串格式）
     *
     * @param token token
     * @return 用户ID字符串
     */
    public static String getUserIdFromToken(String token) {
        try {
            if (StrUtil.isBlank(token)) {
                return null;
            }

            DecodedJWT jwt = JWT.decode(token);

            // 根据JWT生成逻辑，username字段存储的是用户ID字符串
            // generateToken(user.getId(), user.getUserId()) 中：
            // - userId claim 存储的是 user.getId() (Long类型主键)
            // - username claim 存储的是 user.getUserId() (String类型用户ID)
            String userIdStr = jwt.getClaim("username").asString();
            if (StrUtil.isNotBlank(userIdStr)) {
                log.debug("从JWT token获取用户ID: {}", userIdStr);
                return userIdStr;
            }

            // 如果username为空，尝试从userId字段获取并转换为字符串
            Long userId = jwt.getClaim("userId").asLong();
            String result = userId != null ? userId.toString() : null;
            log.debug("从JWT token userId字段获取用户ID: {}", result);
            return result;
        } catch (JWTDecodeException e) {
            log.warn("解析JWT token失败: {}", e.getMessage());
            return null;
        }
    }
}
