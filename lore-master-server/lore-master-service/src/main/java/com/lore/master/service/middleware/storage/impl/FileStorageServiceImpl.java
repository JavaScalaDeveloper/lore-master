package com.lore.master.service.middleware.storage.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.lore.master.common.util.FileUtil;
import com.lore.master.data.dto.storage.FileUploadRequest;
import com.lore.master.data.entity.storage.FileAccessLog;
import com.lore.master.data.entity.storage.FileStorage;
import com.lore.master.data.repository.storage.FileAccessLogRepository;
import com.lore.master.data.repository.storage.FileStorageRepository;
import com.lore.master.data.vo.storage.FileInfoVO;
import com.lore.master.service.middleware.storage.FileStorageService;
import com.lore.master.service.middleware.storage.config.FileStorageProperties;
import com.lore.master.service.middleware.storage.factory.StorageStrategyFactory;
import com.lore.master.service.middleware.storage.strategy.StorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文件存储服务实现
 * 使用策略模式支持多种存储后端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final FileStorageRepository fileStorageRepository;
    private final FileAccessLogRepository fileAccessLogRepository;
    private final StorageStrategyFactory storageStrategyFactory;
    private final FileStorageProperties storageProperties;

    @Override
    @Transactional(value = "storageTransactionManager", rollbackFor = Exception.class)
    public FileInfoVO uploadFile(FileUploadRequest request) {
        try {
            MultipartFile file = request.getFile();

            // 验证文件
            validateFile(file);

            // 读取文件数据
            byte[] fileData = file.getBytes();
            String md5Hash = DigestUtil.md5Hex(fileData);

            // 检查是否已存在相同文件（如果启用去重）
            if (storageProperties.getEnableDeduplication()) {
                Optional<FileStorage> existingFile = fileStorageRepository.findByMd5Hash(md5Hash);
                if (existingFile.isPresent() && !request.getOverwrite()) {
                    log.info("文件已存在，返回已有文件信息: {}", md5Hash);
                    return convertToVO(existingFile.get());
                }
            }

            // 创建文件存储实体
            FileStorage fileStorage = buildFileStorage(request, file, fileData, md5Hash);

            // 获取存储策略
            StorageStrategy storageStrategy = storageStrategyFactory.getCurrentStrategy();

            // 使用存储策略存储文件
            String storedPath = storageStrategy.storeFile(fileStorage, fileData);
            fileStorage.setFilePath(storedPath);

            // 保存文件元数据到数据库
            fileStorage = fileStorageRepository.save(fileStorage);

            log.info("文件上传成功: fileId={}, originalName={}, size={}, strategy={}",
                    fileStorage.getFileId(), fileStorage.getOriginalName(),
                    fileStorage.getFileSize(), storageStrategy.getStorageType());

            return convertToVO(fileStorage);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public FileInfoVO uploadFile(MultipartFile file, String uploadUserId, String uploadUserType) {
        FileUploadRequest request = new FileUploadRequest();
        request.setFile(file);
        request.setUploadUserId(uploadUserId);
        request.setUploadUserType(uploadUserType);
        return uploadFile(request);
    }

    @Override
    @Transactional(value = "storageTransactionManager", rollbackFor = Exception.class)
    public FileInfoVO uploadFile(InputStream inputStream, String originalFileName, String contentType,
                                String uploadUserId, String uploadUserType) {
        try {
            // 读取输入流数据
            byte[] fileData = IoUtil.readBytes(inputStream);
            String md5Hash = DigestUtil.md5Hex(fileData);
            
            // 检查是否已存在相同文件
            Optional<FileStorage> existingFile = fileStorageRepository.findByMd5Hash(md5Hash);
            if (existingFile.isPresent()) {
                log.info("文件已存在，返回已有文件信息: {}", md5Hash);
                return convertToVO(existingFile.get());
            }
            
            // 创建文件存储实体
            FileStorage fileStorage = new FileStorage();
            fileStorage.setFileId(IdUtil.simpleUUID());
            fileStorage.setOriginalName(originalFileName);
            fileStorage.setFileName(generateFileName(originalFileName, null));
            fileStorage.setFilePath(generateFilePath("default", null, fileStorage.getFileName()));
            fileStorage.setFileSize((long) fileData.length);
            fileStorage.setFileType(contentType);
            fileStorage.setFileExtension(FileUtil.getFileExtension(originalFileName));
            fileStorage.setFileCategory(FileStorage.FileCategory.fromMimeType(contentType).getCode());
            fileStorage.setFileData(fileData);
            fileStorage.setMd5Hash(md5Hash);
            fileStorage.setSha256Hash(DigestUtil.sha256Hex(fileData));
            fileStorage.setUploadUserId(uploadUserId);
            fileStorage.setUploadUserType(uploadUserType);
            fileStorage.setBucketName("default");
            fileStorage.setIsPublic(false);
            
            // 保存文件
            fileStorage = fileStorageRepository.save(fileStorage);
            
            log.info("文件上传成功: fileId={}, originalName={}, size={}", 
                    fileStorage.getFileId(), fileStorage.getOriginalName(), fileStorage.getFileSize());
            
            return convertToVO(fileStorage);
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(value = "storageTransactionManager", rollbackFor = Exception.class)
    public byte[] downloadFile(String fileId, String accessUserId, String accessUserType, String accessIp) {
        // 查找文件
        Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileId);
        if (fileOpt.isEmpty()) {
            throw new RuntimeException("文件不存在: " + fileId);
        }

        FileStorage fileStorage = fileOpt.get();

        // 检查文件状态
        if (fileStorage.getStatus() != 1) {
            throw new RuntimeException("文件已被删除或禁用");
        }

        // 获取存储策略并读取文件
        StorageStrategy storageStrategy = storageStrategyFactory.getCurrentStrategy();
        byte[] fileData = storageStrategy.retrieveFile(fileStorage);

        // 记录访问日志（如果启用）
        if (storageProperties.getEnableAccessLog()) {
            recordAccessLog(fileId, accessUserId, accessUserType, accessIp,
                           FileAccessLog.AccessType.DOWNLOAD.getCode());
        }

        // 更新访问统计
        fileStorageRepository.updateAccessInfo(fileId, LocalDateTime.now());

        log.info("文件下载: fileId={}, accessUserId={}, size={}, strategy={}",
                fileId, accessUserId, fileData.length, storageStrategy.getStorageType());

        return fileData;
    }

    @Override
    public FileInfoVO getFileInfo(String fileId) {
        Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileId);
        if (fileOpt.isEmpty()) {
            throw new RuntimeException("文件不存在: " + fileId);
        }
        
        return convertToVO(fileOpt.get());
    }

    @Override
    @Transactional(value = "storageTransactionManager", rollbackFor = Exception.class)
    public boolean deleteFile(String fileId, String operatorUserId, String operatorUserType) {
        // 检查文件是否存在
        Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileId);
        if (fileOpt.isEmpty()) {
            return false;
        }
        
        FileStorage fileStorage = fileOpt.get();
        
        // 检查权限（只有上传者或管理员可以删除）
        if (!canDeleteFile(fileStorage, operatorUserId, operatorUserType)) {
            throw new RuntimeException("没有权限删除该文件");
        }
        
        // 软删除
        int result = fileStorageRepository.softDeleteByFileId(fileId);
        
        log.info("文件删除: fileId={}, operatorUserId={}, result={}", 
                fileId, operatorUserId, result > 0);
        
        return result > 0;
    }

    @Override
    @Transactional(value = "storageTransactionManager", rollbackFor = Exception.class)
    public int batchDeleteFiles(List<String> fileIds, String operatorUserId, String operatorUserType) {
        if (fileIds == null || fileIds.isEmpty()) {
            return 0;
        }
        
        // 检查权限
        for (String fileId : fileIds) {
            Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileId);
            if (fileOpt.isPresent() && !canDeleteFile(fileOpt.get(), operatorUserId, operatorUserType)) {
                throw new RuntimeException("没有权限删除文件: " + fileId);
            }
        }
        
        // 批量软删除
        int result = fileStorageRepository.batchSoftDelete(fileIds);
        
        log.info("批量删除文件: fileIds={}, operatorUserId={}, result={}", 
                fileIds, operatorUserId, result);
        
        return result;
    }

    @Override
    public Page<FileInfoVO> queryFiles(String fileCategory, String uploadUserId, String uploadUserType,
                                      String bucketName, String fileName, Boolean isPublic, Pageable pageable) {
        Page<FileStorage> filePage;
        
        if (StrUtil.isNotBlank(fileName)) {
            // 按文件名模糊查询
            filePage = fileStorageRepository.findByFileNameContaining(fileName, 1, pageable);
        } else if (StrUtil.isNotBlank(fileCategory)) {
            // 按分类查询
            filePage = fileStorageRepository.findByFileCategoryAndStatus(fileCategory, 1, pageable);
        } else if (StrUtil.isNotBlank(uploadUserId) && StrUtil.isNotBlank(uploadUserType)) {
            // 按上传用户查询
            filePage = fileStorageRepository.findByUploadUserIdAndUploadUserTypeAndStatus(
                    uploadUserId, uploadUserType, 1, pageable);
        } else if (StrUtil.isNotBlank(bucketName)) {
            // 按存储桶查询
            filePage = fileStorageRepository.findByBucketNameAndStatus(bucketName, 1, pageable);
        } else if (isPublic != null) {
            // 按公开状态查询
            filePage = fileStorageRepository.findByIsPublicAndStatus(isPublic, 1, pageable);
        } else {
            // 查询最近文件
            filePage = fileStorageRepository.findRecentFiles(1, pageable);
        }
        
        return filePage.map(this::convertToVO);
    }

    @Override
    public FileStatisticsVO getUserFileStatistics(String userId, String userType) {
        FileStatisticsVO statistics = new FileStatisticsVO();
        
        // 总数量和大小
        Long totalCount = fileStorageRepository.countByUser(userId, userType, 1);
        Long totalSize = fileStorageRepository.sumFileSizeByUser(userId, userType, 1);
        
        statistics.setTotalCount(totalCount != null ? totalCount : 0L);
        statistics.setTotalSize(totalSize != null ? totalSize : 0L);
        statistics.setTotalSizeFormatted(FileUtil.formatFileSize(statistics.getTotalSize()));
        
        // TODO: 按分类统计（需要扩展Repository方法）
        statistics.setImageCount(0L);
        statistics.setVideoCount(0L);
        statistics.setAudioCount(0L);
        statistics.setDocumentCount(0L);
        statistics.setOtherCount(0L);
        
        return statistics;
    }

    @Override
    public boolean fileExists(String fileId) {
        return fileStorageRepository.findByFileId(fileId).isPresent();
    }

    @Override
    public FileInfoVO getFileByMd5(String md5Hash) {
        Optional<FileStorage> fileOpt = fileStorageRepository.findByMd5Hash(md5Hash);
        return fileOpt.map(this::convertToVO).orElse(null);
    }

    @Override
    public String generateAccessUrl(String fileId, int expireMinutes) {
        Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileId);
        if (fileOpt.isEmpty()) {
            throw new RuntimeException("文件不存在: " + fileId);
        }

        StorageStrategy storageStrategy = storageStrategyFactory.getCurrentStrategy();
        return storageStrategy.generateAccessUrl(fileOpt.get(), expireMinutes);
    }

    @Override
    public String generateDownloadUrl(String fileId, int expireMinutes) {
        Optional<FileStorage> fileOpt = fileStorageRepository.findByFileId(fileId);
        if (fileOpt.isEmpty()) {
            throw new RuntimeException("文件不存在: " + fileId);
        }

        StorageStrategy storageStrategy = storageStrategyFactory.getCurrentStrategy();
        return storageStrategy.generateDownloadUrl(fileOpt.get(), expireMinutes);
    }

    @Override
    public StorageStrategy getCurrentStorageStrategy() {
        return storageStrategyFactory.getCurrentStrategy();
    }

    @Override
    public boolean switchStorageStrategy(String strategyType) {
        try {
            StorageStrategy strategy = storageStrategyFactory.getStrategy(strategyType);
            boolean isValid = strategy.validateConfiguration();

            if (isValid) {
                log.info("存储策略切换成功: {}", strategyType);
                return true;
            } else {
                log.error("存储策略配置无效: {}", strategyType);
                return false;
            }
        } catch (Exception e) {
            log.error("存储策略切换失败: {}", strategyType, e);
            return false;
        }
    }

    @Override
    public List<String> getAvailableStorageStrategies() {
        return storageStrategyFactory.getAvailableStrategyTypes();
    }

    @Override
    public boolean validateStorageConfiguration() {
        return storageStrategyFactory.validateCurrentStrategy();
    }

    @Override
    public StorageStrategy.StorageStatistics getStorageStatistics() {
        return storageStrategyFactory.getCurrentStrategyStatistics();
    }

    @Override
    @Transactional(value = "storageTransactionManager", rollbackFor = Exception.class)
    public int cleanExpiredTempFiles(int expireHours) {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(expireHours);
        List<FileStorage> expiredFiles = fileStorageRepository.findExpiredTempFiles(expireTime);
        
        if (expiredFiles.isEmpty()) {
            return 0;
        }
        
        List<String> fileIds = expiredFiles.stream()
                .map(FileStorage::getFileId)
                .toList();
        
        int result = fileStorageRepository.batchSoftDelete(fileIds);
        
        log.info("清理过期临时文件: expireHours={}, cleanedCount={}", expireHours, result);
        
        return result;
    }

    /**
     * 构建文件存储实体
     */
    private FileStorage buildFileStorage(FileUploadRequest request, MultipartFile file,
                                       byte[] fileData, String md5Hash) {
        FileStorage fileStorage = new FileStorage();
        fileStorage.setFileId(IdUtil.simpleUUID());
        fileStorage.setOriginalName(file.getOriginalFilename());
        fileStorage.setFileName(generateFileName(file.getOriginalFilename(), request.getCustomFileName()));
        fileStorage.setFilePath(generateFilePath(request.getBucketName(), request.getFilePath(), fileStorage.getFileName()));
        fileStorage.setFileSize(file.getSize());
        fileStorage.setFileType(file.getContentType());
        fileStorage.setFileExtension(FileUtil.getFileExtension(file.getOriginalFilename()));
        fileStorage.setFileCategory(FileStorage.FileCategory.fromMimeType(file.getContentType()).getCode());
        fileStorage.setMd5Hash(md5Hash);
        fileStorage.setSha256Hash(DigestUtil.sha256Hex(fileData));
        fileStorage.setUploadUserId(request.getUploadUserId());
        fileStorage.setUploadUserType(request.getUploadUserType());
        fileStorage.setBucketName(StrUtil.isNotBlank(request.getBucketName()) ?
                                 request.getBucketName() : storageProperties.getDefaultBucketName());
        fileStorage.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);
        fileStorage.setRemark(request.getRemark());

        // 对于MySQL存储策略，需要设置文件数据
        if ("mysql".equals(storageStrategyFactory.getCurrentStrategy().getStorageType())) {
            fileStorage.setFileData(fileData);
        }

        return fileStorage;
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        if (file.getSize() > storageProperties.getMaxFileSize()) {
            throw new RuntimeException("文件大小不能超过" +
                FileUtil.formatFileSize(storageProperties.getMaxFileSize()));
        }

        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new RuntimeException("文件名不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (!FileUtil.isAllowedFileType(contentType, storageProperties.getAllowedTypes())) {
            throw new RuntimeException("不支持的文件类型: " + contentType);
        }
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String originalFileName, String customFileName) {
        if (StrUtil.isNotBlank(customFileName)) {
            return customFileName;
        }
        
        String extension = FileUtil.getFileExtension(originalFileName);
        return IdUtil.simpleUUID() + (StrUtil.isNotBlank(extension) ? "." + extension : "");
    }

    /**
     * 生成文件路径
     */
    private String generateFilePath(String bucketName, String customPath, String fileName) {
        if (StrUtil.isNotBlank(customPath)) {
            return customPath + "/" + fileName;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return String.format("%s/%d/%02d/%02d/%s", 
                bucketName, now.getYear(), now.getMonthValue(), now.getDayOfMonth(), fileName);
    }

    /**
     * 记录访问日志
     */
    private void recordAccessLog(String fileId, String accessUserId, String accessUserType, 
                               String accessIp, String accessType) {
        try {
            FileAccessLog accessLog = new FileAccessLog();
            accessLog.setFileId(fileId);
            accessLog.setAccessUserId(accessUserId);
            accessLog.setAccessUserType(accessUserType);
            accessLog.setAccessIp(accessIp);
            accessLog.setAccessType(accessType);
            
            fileAccessLogRepository.save(accessLog);
        } catch (Exception e) {
            log.warn("记录文件访问日志失败: fileId={}, error={}", fileId, e.getMessage());
        }
    }

    /**
     * 检查是否可以删除文件
     */
    private boolean canDeleteFile(FileStorage fileStorage, String operatorUserId, String operatorUserType) {
        // 管理员可以删除任何文件
        if ("admin".equals(operatorUserType)) {
            return true;
        }
        
        // 上传者可以删除自己的文件
        return operatorUserId.equals(fileStorage.getUploadUserId()) && 
               operatorUserType.equals(fileStorage.getUploadUserType());
    }

    /**
     * 转换为VO
     */
    private FileInfoVO convertToVO(FileStorage fileStorage) {
        FileInfoVO vo = new FileInfoVO();
        BeanUtils.copyProperties(fileStorage, vo);
        
        // 设置格式化文件大小
        vo.setFileSizeFormatted(FileUtil.formatFileSize(fileStorage.getFileSize()));
        
        // 设置分类名称
        FileStorage.FileCategory category = FileStorage.FileCategory.valueOf(fileStorage.getFileCategory().toUpperCase());
        vo.setFileCategoryName(category.getName());
        
        // 设置文件类型标识
        vo.setIsImage("image".equals(fileStorage.getFileCategory()));
        vo.setIsVideo("video".equals(fileStorage.getFileCategory()));
        vo.setIsAudio("audio".equals(fileStorage.getFileCategory()));
        vo.setIsDocument("document".equals(fileStorage.getFileCategory()));
        
        // 设置访问URL
        vo.setAccessUrl(generateAccessUrl(fileStorage.getFileId(), 60));
        vo.setDownloadUrl(generateDownloadUrl(fileStorage.getFileId(), 60));
        
        return vo;
    }
}
