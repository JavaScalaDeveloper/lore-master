package com.lore.master.common.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * 前后端通信加解密工具类
 * 用于密码等敏感信息的传输加密
 */
@Slf4j
public class CryptoUtil {
    
    /**
     * AES加密密钥 - 实际项目中应该从配置文件或环境变量中获取
     */
    private static final String AES_KEY = "LoreMaster2024!@#$%^&*()_+{}|";
    
    /**
     * AES加密算法
     */
    private static final String AES_ALGORITHM = "AES";
    
    /**
     * AES加密模式
     */
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    /**
     * 获取AES密钥
     */
    private static SecretKeySpec getAESKey() {
        try {
            // 使用SHA-256对密钥进行哈希，确保长度为32字节
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(AES_KEY.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, AES_ALGORITHM);
        } catch (Exception e) {
            log.error("生成AES密钥失败", e);
            throw new RuntimeException("生成AES密钥失败", e);
        }
    }
    
    /**
     * AES加密
     * 
     * @param plainText 明文
     * @return 加密后的Base64字符串
     */
    public static String aesEncrypt(String plainText) {
        if (StrUtil.isBlank(plainText)) {
            return plainText;
        }
        
        try {
            SecretKeySpec keySpec = getAESKey();
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encode(encrypted);
        } catch (Exception e) {
            log.error("AES加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("AES加密失败", e);
        }
    }
    
    /**
     * AES解密
     * 
     * @param encryptedText 加密的Base64字符串
     * @return 解密后的明文
     */
    public static String aesDecrypt(String encryptedText) {
        if (StrUtil.isBlank(encryptedText)) {
            return encryptedText;
        }
        
        try {
            SecretKeySpec keySpec = getAESKey();
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] encrypted = Base64.decode(encryptedText);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败: {}", e.getMessage(), e);
            throw new RuntimeException("AES解密失败", e);
        }
    }
    
    /**
     * MD5哈希（用于简单的数据校验，不用于密码存储）
     * 
     * @param text 原文
     * @return MD5哈希值
     */
    public static String md5Hash(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("MD5哈希失败: {}", e.getMessage(), e);
            throw new RuntimeException("MD5哈希失败", e);
        }
    }
    
    /**
     * SHA-256哈希
     * 
     * @param text 原文
     * @return SHA-256哈希值
     */
    public static String sha256Hash(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = sha.digest(text.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("SHA-256哈希失败: {}", e.getMessage(), e);
            throw new RuntimeException("SHA-256哈希失败", e);
        }
    }
    
    /**
     * 生成随机盐值
     * 
     * @param length 盐值长度
     * @return 随机盐值
     */
    public static String generateSalt(int length) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[length];
            random.nextBytes(salt);
            return Base64.encode(salt);
        } catch (Exception e) {
            log.error("生成盐值失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成盐值失败", e);
        }
    }
    
    /**
     * 带盐值的SHA-256哈希
     * 
     * @param text 原文
     * @param salt 盐值
     * @return 哈希值
     */
    public static String sha256HashWithSalt(String text, String salt) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        
        String saltedText = text + salt;
        return sha256Hash(saltedText);
    }
    
    /**
     * 验证前端传输的加密密码
     * 
     * @param encryptedPassword 前端传输的加密密码
     * @param plainPassword 数据库中的明文密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String encryptedPassword, String plainPassword) {
        try {
            // 解密前端传输的密码
            String decryptedPassword = aesDecrypt(encryptedPassword);
            // 与数据库明文密码比较
            return plainPassword.equals(decryptedPassword);
        } catch (Exception e) {
            log.error("密码验证失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 加密密码用于前端传输
     * 
     * @param plainPassword 明文密码
     * @return 加密后的密码
     */
    public static String encryptPasswordForTransmission(String plainPassword) {
        return aesEncrypt(plainPassword);
    }
    
    /**
     * 解密前端传输的密码
     * 
     * @param encryptedPassword 加密的密码
     * @return 明文密码
     */
    public static String decryptPasswordFromTransmission(String encryptedPassword) {
        return aesDecrypt(encryptedPassword);
    }
}
