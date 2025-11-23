import CryptoJS from 'crypto-js';

/**
 * 前后端通信加解密工具类
 * 用于密码等敏感信息的传输加密
 */

// AES加密密钥 - 需要与后端保持一致
const AES_KEY = 'LoreMaster2024!@#$%^&*()_+{}|';

/**
 * 获取AES密钥
 */
function getAESKey(): CryptoJS.lib.WordArray {
  // 使用SHA-256对密钥进行哈希，确保长度为32字节
  return CryptoJS.SHA256(AES_KEY);
}

/**
 * AES加密
 * @param plainText 明文
 * @returns 加密后的Base64字符串
 */
export function aesEncrypt(plainText: string): string {
  if (!plainText) {
    return plainText;
  }

  try {
    const key = getAESKey();
    const encrypted = CryptoJS.AES.encrypt(plainText, key, {
      mode: CryptoJS.mode.ECB,
      padding: CryptoJS.pad.Pkcs7
    });
    return encrypted.toString();
  } catch (error) {
    console.error('AES加密失败:', error);
    throw new Error('AES加密失败');
  }
}

/**
 * AES解密
 * @param encryptedText 加密的Base64字符串
 * @returns 解密后的明文
 */
export function aesDecrypt(encryptedText: string): string {
  if (!encryptedText) {
    return encryptedText;
  }

  try {
    const key = getAESKey();
    const decrypted = CryptoJS.AES.decrypt(encryptedText, key, {
      mode: CryptoJS.mode.ECB,
      padding: CryptoJS.pad.Pkcs7
    });
    return decrypted.toString(CryptoJS.enc.Utf8);
  } catch (error) {
    console.error('AES解密失败:', error);
    throw new Error('AES解密失败');
  }
}

/**
 * MD5哈希（用于简单的数据校验，不用于密码存储）
 * @param text 原文
 * @returns MD5哈希值
 */
export function md5Hash(text: string): string {
  if (!text) {
    return text;
  }

  try {
    return CryptoJS.MD5(text).toString();
  } catch (error) {
    console.error('MD5哈希失败:', error);
    throw new Error('MD5哈希失败');
  }
}

/**
 * SHA-256哈希
 * @param text 原文
 * @returns SHA-256哈希值
 */
export function sha256Hash(text: string): string {
  if (!text) {
    return text;
  }

  try {
    return CryptoJS.SHA256(text).toString();
  } catch (error) {
    console.error('SHA-256哈希失败:', error);
    throw new Error('SHA-256哈希失败');
  }
}

/**
 * 生成随机盐值
 * @param length 盐值长度（字节数）
 * @returns 随机盐值的Base64字符串
 */
export function generateSalt(length: number = 16): string {
  try {
    const salt = CryptoJS.lib.WordArray.random(length);
    return CryptoJS.enc.Base64.stringify(salt);
  } catch (error) {
    console.error('生成盐值失败:', error);
    throw new Error('生成盐值失败');
  }
}

/**
 * 带盐值的SHA-256哈希
 * @param text 原文
 * @param salt 盐值
 * @returns 哈希值
 */
export function sha256HashWithSalt(text: string, salt: string): string {
  if (!text) {
    return text;
  }

  const saltedText = text + salt;
  return sha256Hash(saltedText);
}

/**
 * 加密密码用于传输
 * @param plainPassword 明文密码
 * @returns 加密后的密码
 */
export function encryptPasswordForTransmission(plainPassword: string): string {
  return aesEncrypt(plainPassword);
}

/**
 * 解密传输的密码
 * @param encryptedPassword 加密的密码
 * @returns 明文密码
 */
export function decryptPasswordFromTransmission(encryptedPassword: string): string {
  return aesDecrypt(encryptedPassword);
}

/**
 * Base64编码
 * @param text 原文
 * @returns Base64编码字符串
 */
export function base64Encode(text: string): string {
  try {
    return CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(text));
  } catch (error) {
    console.error('Base64编码失败:', error);
    throw new Error('Base64编码失败');
  }
}

/**
 * Base64解码
 * @param encodedText Base64编码字符串
 * @returns 原文
 */
export function base64Decode(encodedText: string): string {
  try {
    return CryptoJS.enc.Base64.parse(encodedText).toString(CryptoJS.enc.Utf8);
  } catch (error) {
    console.error('Base64解码失败:', error);
    throw new Error('Base64解码失败');
  }
}

/**
 * 生成随机字符串
 * @param length 长度
 * @param chars 字符集
 * @returns 随机字符串
 */
export function generateRandomString(
  length: number = 16,
  chars: string = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
): string {
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

/**
 * 验证密码强度
 * @param password 密码
 * @returns 强度等级 (1-5)
 */
export function getPasswordStrength(password: string): number {
  if (!password) return 0;

  let strength = 0;
  
  // 长度检查
  if (password.length >= 8) strength++;
  if (password.length >= 12) strength++;
  
  // 字符类型检查
  if (/[a-z]/.test(password)) strength++; // 小写字母
  if (/[A-Z]/.test(password)) strength++; // 大写字母
  if (/[0-9]/.test(password)) strength++; // 数字
  if (/[^A-Za-z0-9]/.test(password)) strength++; // 特殊字符
  
  // 最大强度为5
  return Math.min(strength, 5);
}

/**
 * 密码强度描述
 */
export const PASSWORD_STRENGTH_LABELS = {
  0: '无',
  1: '很弱',
  2: '弱',
  3: '中等',
  4: '强',
  5: '很强'
};

/**
 * 密码强度颜色
 */
export const PASSWORD_STRENGTH_COLORS = {
  0: '#d9d9d9',
  1: '#ff4d4f',
  2: '#ff7a45',
  3: '#ffa940',
  4: '#52c41a',
  5: '#389e0d'
};
