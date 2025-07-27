package com.lore.master.common.util;

import cn.hutool.core.util.StrUtil;

import java.text.DecimalFormat;

/**
 * 文件工具类
 */
public class FileUtil {
    
    private FileUtil() {
        // 工具类不允许实例化
    }
    
    /**
     * 获取文件扩展名
     * 
     * @param fileName 文件名
     * @return 扩展名（不包含点）
     */
    public static String getFileExtension(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 格式化文件大小
     * 
     * @param size 文件大小（字节）
     * @return 格式化后的大小
     */
    public static String formatFileSize(Long size) {
        if (size == null || size <= 0) {
            return "0 B";
        }
        
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        if (digitGroups >= units.length) {
            digitGroups = units.length - 1;
        }
        
        double formattedSize = size / Math.pow(1024, digitGroups);
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        
        return decimalFormat.format(formattedSize) + " " + units[digitGroups];
    }
    
    /**
     * 检查是否为图片文件
     * 
     * @param mimeType MIME类型
     * @return 是否为图片
     */
    public static boolean isImageFile(String mimeType) {
        return StrUtil.isNotBlank(mimeType) && mimeType.toLowerCase().startsWith("image/");
    }
    
    /**
     * 检查是否为视频文件
     * 
     * @param mimeType MIME类型
     * @return 是否为视频
     */
    public static boolean isVideoFile(String mimeType) {
        return StrUtil.isNotBlank(mimeType) && mimeType.toLowerCase().startsWith("video/");
    }
    
    /**
     * 检查是否为音频文件
     * 
     * @param mimeType MIME类型
     * @return 是否为音频
     */
    public static boolean isAudioFile(String mimeType) {
        return StrUtil.isNotBlank(mimeType) && mimeType.toLowerCase().startsWith("audio/");
    }
    
    /**
     * 检查文件类型是否被允许
     * 
     * @param mimeType MIME类型
     * @param allowedTypes 允许的类型列表
     * @return 是否允许
     */
    public static boolean isAllowedFileType(String mimeType, String[] allowedTypes) {
        if (StrUtil.isBlank(mimeType) || allowedTypes == null || allowedTypes.length == 0) {
            return false;
        }
        
        String lowerMimeType = mimeType.toLowerCase();
        for (String allowedType : allowedTypes) {
            if (lowerMimeType.contains(allowedType.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 生成安全的文件名
     * 
     * @param originalFileName 原始文件名
     * @return 安全的文件名
     */
    public static String generateSafeFileName(String originalFileName) {
        if (StrUtil.isBlank(originalFileName)) {
            return "unknown";
        }
        
        // 移除危险字符
        String safeFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // 限制长度
        if (safeFileName.length() > 100) {
            String extension = getFileExtension(safeFileName);
            String nameWithoutExt = safeFileName.substring(0, safeFileName.lastIndexOf('.'));
            if (nameWithoutExt.length() > 90) {
                nameWithoutExt = nameWithoutExt.substring(0, 90);
            }
            safeFileName = nameWithoutExt + (StrUtil.isNotBlank(extension) ? "." + extension : "");
        }
        
        return safeFileName;
    }
}
