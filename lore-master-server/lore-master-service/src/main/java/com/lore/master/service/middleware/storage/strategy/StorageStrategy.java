package com.lore.master.service.middleware.storage.strategy;

import com.lore.master.data.dto.storage.FileUploadRequest;
import com.lore.master.data.entity.storage.FileStorage;

import java.io.InputStream;

/**
 * 存储策略接口
 * 定义文件存储的标准操作，支持多种存储后端实现
 */
public interface StorageStrategy {

    /**
     * 获取存储策略类型
     * 
     * @return 存储类型标识
     */
    String getStorageType();

    /**
     * 存储文件
     * 
     * @param fileStorage 文件存储实体（包含元数据）
     * @param inputStream 文件输入流
     * @return 存储后的文件路径或标识
     */
    String storeFile(FileStorage fileStorage, InputStream inputStream);

    /**
     * 存储文件（字节数组方式）
     * 
     * @param fileStorage 文件存储实体（包含元数据）
     * @param fileData 文件字节数组
     * @return 存储后的文件路径或标识
     */
    String storeFile(FileStorage fileStorage, byte[] fileData);

    /**
     * 读取文件
     * 
     * @param fileStorage 文件存储实体
     * @return 文件字节数组
     */
    byte[] retrieveFile(FileStorage fileStorage);

    /**
     * 读取文件流
     * 
     * @param fileStorage 文件存储实体
     * @return 文件输入流
     */
    InputStream retrieveFileStream(FileStorage fileStorage);

    /**
     * 删除文件
     * 
     * @param fileStorage 文件存储实体
     * @return 是否删除成功
     */
    boolean deleteFile(FileStorage fileStorage);

    /**
     * 检查文件是否存在
     * 
     * @param fileStorage 文件存储实体
     * @return 是否存在
     */
    boolean fileExists(FileStorage fileStorage);

    /**
     * 获取文件访问URL
     * 
     * @param fileStorage 文件存储实体
     * @param expireMinutes 过期时间（分钟）
     * @return 访问URL
     */
    String generateAccessUrl(FileStorage fileStorage, int expireMinutes);

    /**
     * 获取文件下载URL
     * 
     * @param fileStorage 文件存储实体
     * @param expireMinutes 过期时间（分钟）
     * @return 下载URL
     */
    String generateDownloadUrl(FileStorage fileStorage, int expireMinutes);

    /**
     * 复制文件
     * 
     * @param sourceFileStorage 源文件存储实体
     * @param targetFileStorage 目标文件存储实体
     * @return 是否复制成功
     */
    boolean copyFile(FileStorage sourceFileStorage, FileStorage targetFileStorage);

    /**
     * 移动文件
     * 
     * @param sourceFileStorage 源文件存储实体
     * @param targetFileStorage 目标文件存储实体
     * @return 是否移动成功
     */
    boolean moveFile(FileStorage sourceFileStorage, FileStorage targetFileStorage);

    /**
     * 获取文件大小
     * 
     * @param fileStorage 文件存储实体
     * @return 文件大小（字节）
     */
    long getFileSize(FileStorage fileStorage);

    /**
     * 验证存储配置
     * 
     * @return 配置是否有效
     */
    boolean validateConfiguration();

    /**
     * 获取存储统计信息
     * 
     * @return 存储统计信息
     */
    StorageStatistics getStorageStatistics();

    /**
     * 存储统计信息
     */
    class StorageStatistics {
        private long totalFiles;
        private long totalSize;
        private long availableSpace;
        private double usagePercentage;

        // Getters and Setters
        public long getTotalFiles() { return totalFiles; }
        public void setTotalFiles(long totalFiles) { this.totalFiles = totalFiles; }

        public long getTotalSize() { return totalSize; }
        public void setTotalSize(long totalSize) { this.totalSize = totalSize; }

        public long getAvailableSpace() { return availableSpace; }
        public void setAvailableSpace(long availableSpace) { this.availableSpace = availableSpace; }

        public double getUsagePercentage() { return usagePercentage; }
        public void setUsagePercentage(double usagePercentage) { this.usagePercentage = usagePercentage; }
    }
}
