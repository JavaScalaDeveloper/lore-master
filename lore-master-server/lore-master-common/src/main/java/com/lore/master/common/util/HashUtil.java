package com.lore.master.common.util;

public class HashUtil {
    public static String multiHash(String pwd) {
        return md5(sha1(sha256(pwd)));
    }
    public static String sha256(String str) {
        return hash(str, "SHA-256");
    }
    public static String sha1(String str) {
        return hash(str, "SHA-1");
    }
    public static String md5(String str) {
        return hash(str, "MD5");
    }
    public static String hash(String str, String algorithm) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance(algorithm);
            byte[] hash = md.digest(str.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 