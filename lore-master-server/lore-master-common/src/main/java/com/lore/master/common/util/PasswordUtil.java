package com.lore.master.common.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 密码工具类
 */
@Slf4j
public class PasswordUtil {
    
    /**
     * BCrypt加密强度
     */
    private static final int BCRYPT_ROUNDS = 12;
    
    /**
     * 使用BCrypt加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        if (StrUtil.isBlank(rawPassword)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        try {
            return BCrypt.withDefaults().hashToString(BCRYPT_ROUNDS, rawPassword.toCharArray());
        } catch (Exception e) {
            log.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 验证密码是否匹配
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(encodedPassword)) {
            return false;
        }
        
        try {
            BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword);
            return result.verified;
        } catch (Exception e) {
            log.warn("密码验证失败", e);
            return false;
        }
    }
    
    /**
     * 生成MD5哈希（用于前端传输加密）
     *
     * @param password 密码
     * @return MD5哈希值
     */
    public static String md5(String password) {
        if (StrUtil.isBlank(password)) {
            return "";
        }
        return DigestUtil.md5Hex(password);
    }
    
    /**
     * 生成SHA256哈希
     *
     * @param password 密码
     * @return SHA256哈希值
     */
    public static String sha256(String password) {
        if (StrUtil.isBlank(password)) {
            return "";
        }
        return DigestUtil.sha256Hex(password);
    }
    
    /**
     * 多重哈希（MD5 + SHA256）
     * 用于前端传输时的密码加密
     *
     * @param password 原始密码
     * @return 多重哈希值
     */
    public static String multiHash(String password) {
        if (StrUtil.isBlank(password)) {
            return "";
        }
        
        // 先MD5，再SHA256
        String md5Hash = md5(password);
        return sha256(md5Hash);
    }
    
    /**
     * 验证密码强度
     *
     * @param password 密码
     * @return 密码强度等级 (1-5)
     */
    public static int getPasswordStrength(String password) {
        if (StrUtil.isBlank(password)) {
            return 0;
        }
        
        int score = 0;
        
        // 长度检查
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // 包含小写字母
        if (password.matches(".*[a-z].*")) score++;
        
        // 包含大写字母
        if (password.matches(".*[A-Z].*")) score++;
        
        // 包含数字
        if (password.matches(".*[0-9].*")) score++;
        
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;
        
        return Math.min(score, 5);
    }
    
    /**
     * 检查密码是否符合最低安全要求
     *
     * @param password 密码
     * @return 是否符合要求
     */
    public static boolean isPasswordValid(String password) {
        if (StrUtil.isBlank(password)) {
            return false;
        }
        
        // 最低要求：长度至少8位，包含字母和数字
        return password.length() >= 8 
                && password.matches(".*[a-zA-Z].*") 
                && password.matches(".*[0-9].*");
    }
}
