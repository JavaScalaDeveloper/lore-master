package com.lore.master.service.middleware.storage.strategy.impl;

import cn.hutool.core.io.IoUtil;
import com.lore.master.data.entity.storage.FileStorage;
import com.lore.master.data.repository.storage.FileStorageRepository;
import com.lore.master.service.middleware.storage.strategy.StorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

/**
 * MySQL存储策略实现
 * 将文件以二进制数据形式存储在MySQL数据库中
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file-storage.strategy", havingValue = "mysql", matchIfMissing = true)
public class MySQLStorageStrategy implements StorageStrategy {

    private final FileStorageRepository fileStorageRepository;

    @Override
    public String getStorageType() {
        return "mysql";
    }

    @Override
    public String storeFile(FileStorage fileStorage, InputStream inputStream) {
        try {
            byte[] fileData = IoUtil.readBytes(inputStream);
            return storeFile(fileStorage, fileData);
        } catch (Exception e) {
            log.error("MySQL存储文件失败: fileId={}", fileStorage.getFileId(), e);
            throw new RuntimeException("MySQL存储文件失败: " + e.getMessage());
        }
    }

    @Override
    public String storeFile(FileStorage fileStorage, byte[] fileData) {
        try {
            // 设置文件数据
            fileStorage.setFileData(fileData);
            
            // 保存到数据库
            FileStorage savedFile = fileStorageRepository.save(fileStorage);
            
            log.info("MySQL存储文件成功: fileId={}, size={}", 
                    savedFile.getFileId(), fileData.length);
            
            // 返回文件路径（对于MySQL存储，返回fileId作为标识）
            return savedFile.getFilePath();
            
        } catch (Exception e) {
            log.error("MySQL存储文件失败: fileId={}", fileStorage.getFileId(), e);
            throw new RuntimeException("MySQL存储文件失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] retrieveFile(FileStorage fileStorage) {
        try {
            Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileStorage.getFileId());
            if (fileOpt.isEmpty()) {
                throw new RuntimeException("文件不存在: " + fileStorage.getFileId());
            }
            
            FileStorage file = fileOpt.get();
            byte[] fileData = file.getFileData();
            
            if (fileData == null || fileData.length == 0) {
                throw new RuntimeException("文件数据为空: " + fileStorage.getFileId());
            }
            
            log.debug("MySQL读取文件成功: fileId={}, size={}", 
                    file.getFileId(), fileData.length);
            
            return fileData;
            
        } catch (Exception e) {
            log.error("MySQL读取文件失败: fileId={}", fileStorage.getFileId(), e);
            throw new RuntimeException("MySQL读取文件失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream retrieveFileStream(FileStorage fileStorage) {
        byte[] fileData = retrieveFile(fileStorage);
        return new ByteArrayInputStream(fileData);
    }

    @Override
    public boolean deleteFile(FileStorage fileStorage) {
        try {
            // MySQL存储策略中，删除文件就是将数据库中的file_data字段清空
            // 但保留文件元数据，实现软删除
            Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileStorage.getFileId());
            if (fileOpt.isEmpty()) {
                log.warn("要删除的文件不存在: fileId={}", fileStorage.getFileId());
                return false;
            }
            
            FileStorage file = fileOpt.get();
            file.setFileData(null); // 清空文件数据
            file.setStatus(0); // 设置为删除状态
            fileStorageRepository.save(file);
            
            log.info("MySQL删除文件成功: fileId={}", fileStorage.getFileId());
            return true;
            
        } catch (Exception e) {
            log.error("MySQL删除文件失败: fileId={}", fileStorage.getFileId(), e);
            return false;
        }
    }

    @Override
    public boolean fileExists(FileStorage fileStorage) {
        try {
            Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileStorage.getFileId());
            if (fileOpt.isEmpty()) {
                return false;
            }
            
            FileStorage file = fileOpt.get();
            return file.getStatus() == 1 && file.getFileData() != null && file.getFileData().length > 0;
            
        } catch (Exception e) {
            log.error("MySQL检查文件存在性失败: fileId={}", fileStorage.getFileId(), e);
            return false;
        }
    }

    @Override
    public String generateAccessUrl(FileStorage fileStorage, int expireMinutes) {
        // MySQL存储策略生成的是本地访问URL
        return String.format("/api/file/view?fileId=%s", fileStorage.getFileId());
    }

    @Override
    public String generateDownloadUrl(FileStorage fileStorage, int expireMinutes) {
        // MySQL存储策略生成的是本地下载URL
        return String.format("/api/file/download?fileId=%s", fileStorage.getFileId());
    }

    @Override
    public boolean copyFile(FileStorage sourceFileStorage, FileStorage targetFileStorage) {
        try {
            // 读取源文件数据
            byte[] sourceData = retrieveFile(sourceFileStorage);
            
            // 存储到目标位置
            storeFile(targetFileStorage, sourceData);
            
            log.info("MySQL复制文件成功: source={}, target={}", 
                    sourceFileStorage.getFileId(), targetFileStorage.getFileId());
            return true;
            
        } catch (Exception e) {
            log.error("MySQL复制文件失败: source={}, target={}", 
                    sourceFileStorage.getFileId(), targetFileStorage.getFileId(), e);
            return false;
        }
    }

    @Override
    public boolean moveFile(FileStorage sourceFileStorage, FileStorage targetFileStorage) {
        try {
            // 先复制文件
            if (!copyFile(sourceFileStorage, targetFileStorage)) {
                return false;
            }
            
            // 再删除源文件
            return deleteFile(sourceFileStorage);
            
        } catch (Exception e) {
            log.error("MySQL移动文件失败: source={}, target={}", 
                    sourceFileStorage.getFileId(), targetFileStorage.getFileId(), e);
            return false;
        }
    }

    @Override
    public long getFileSize(FileStorage fileStorage) {
        try {
            Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileStorage.getFileId());
            if (fileOpt.isEmpty()) {
                return 0;
            }
            
            FileStorage file = fileOpt.get();
            return file.getFileSize() != null ? file.getFileSize() : 0;
            
        } catch (Exception e) {
            log.error("MySQL获取文件大小失败: fileId={}", fileStorage.getFileId(), e);
            return 0;
        }
    }

    @Override
    public boolean validateConfiguration() {
        try {
            // 检查数据库连接是否正常
            fileStorageRepository.count();
            log.info("MySQL存储策略配置验证成功");
            return true;
            
        } catch (Exception e) {
            log.error("MySQL存储策略配置验证失败", e);
            return false;
        }
    }

    @Override
    public StorageStatistics getStorageStatistics() {
        try {
            StorageStatistics statistics = new StorageStatistics();
            
            // 统计文件总数
            long totalFiles = fileStorageRepository.count();
            statistics.setTotalFiles(totalFiles);
            
            // 统计总大小（这里需要自定义查询）
            // 由于MySQL存储在LONGBLOB中，无法直接统计，这里返回估算值
            long totalSize = totalFiles * 1024 * 1024; // 假设平均每个文件1MB
            statistics.setTotalSize(totalSize);
            
            // MySQL存储的可用空间取决于数据库配置，这里返回一个大概值
            statistics.setAvailableSpace(Long.MAX_VALUE);
            statistics.setUsagePercentage(0.0);
            
            return statistics;
            
        } catch (Exception e) {
            log.error("获取MySQL存储统计信息失败", e);
            StorageStatistics statistics = new StorageStatistics();
            statistics.setTotalFiles(0);
            statistics.setTotalSize(0);
            statistics.setAvailableSpace(0);
            statistics.setUsagePercentage(0.0);
            return statistics;
        }
    }
}
