package com.lore.master.service.middleware.storage;

import com.lore.master.data.dto.storage.FileUploadRequest;
import com.lore.master.data.vo.storage.FileInfoVO;
import com.lore.master.service.middleware.storage.strategy.StorageStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文件存储服务接口
 * 使用策略模式支持多种存储后端
 */
public interface FileStorageService {

    /**
     * 上传文件
     * 
     * @param request 上传请求
     * @return 文件信息
     */
    FileInfoVO uploadFile(FileUploadRequest request);

    /**
     * 简单上传文件
     * 
     * @param file 文件
     * @param uploadUserId 上传用户ID
     * @param uploadUserType 上传用户类型
     * @return 文件信息
     */
    FileInfoVO uploadFile(MultipartFile file, String uploadUserId, String uploadUserType);

    /**
     * 通过输入流上传文件
     * 
     * @param inputStream 输入流
     * @param originalFileName 原始文件名
     * @param contentType 内容类型
     * @param uploadUserId 上传用户ID
     * @param uploadUserType 上传用户类型
     * @return 文件信息
     */
    FileInfoVO uploadFile(InputStream inputStream, String originalFileName, String contentType, 
                         String uploadUserId, String uploadUserType);

    /**
     * 下载文件
     * 
     * @param fileId 文件ID
     * @param accessUserId 访问用户ID
     * @param accessUserType 访问用户类型
     * @param accessIp 访问IP
     * @return 文件字节数组
     */
    byte[] downloadFile(String fileId, String accessUserId, String accessUserType, String accessIp);

    /**
     * 获取文件信息
     * 
     * @param fileId 文件ID
     * @return 文件信息
     */
    FileInfoVO getFileInfo(String fileId);

    /**
     * 删除文件
     * 
     * @param fileId 文件ID
     * @param operatorUserId 操作用户ID
     * @param operatorUserType 操作用户类型
     * @return 是否成功
     */
    boolean deleteFile(String fileId, String operatorUserId, String operatorUserType);

    /**
     * 批量删除文件
     * 
     * @param fileIds 文件ID列表
     * @param operatorUserId 操作用户ID
     * @param operatorUserType 操作用户类型
     * @return 删除成功的数量
     */
    int batchDeleteFiles(List<String> fileIds, String operatorUserId, String operatorUserType);

    /**
     * 分页查询文件
     * 
     * @param fileCategory 文件分类
     * @param uploadUserId 上传用户ID
     * @param uploadUserType 上传用户类型
     * @param bucketName 存储桶名称
     * @param fileName 文件名（模糊查询）
     * @param isPublic 是否公开
     * @param pageable 分页参数
     * @return 文件列表
     */
    Page<FileInfoVO> queryFiles(String fileCategory, String uploadUserId, String uploadUserType,
                               String bucketName, String fileName, Boolean isPublic, Pageable pageable);

    /**
     * 获取用户文件统计
     * 
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 统计信息
     */
    FileStatisticsVO getUserFileStatistics(String userId, String userType);

    /**
     * 检查文件是否存在
     * 
     * @param fileId 文件ID
     * @return 是否存在
     */
    boolean fileExists(String fileId);

    /**
     * 根据MD5检查文件是否已存在
     * 
     * @param md5Hash MD5哈希值
     * @return 文件信息，不存在返回null
     */
    FileInfoVO getFileByMd5(String md5Hash);

    /**
     * 生成文件访问URL
     * 
     * @param fileId 文件ID
     * @param expireMinutes 过期时间（分钟）
     * @return 访问URL
     */
    String generateAccessUrl(String fileId, int expireMinutes);

    /**
     * 生成文件下载URL
     * 
     * @param fileId 文件ID
     * @param expireMinutes 过期时间（分钟）
     * @return 下载URL
     */
    String generateDownloadUrl(String fileId, int expireMinutes);

    /**
     * 清理过期的临时文件
     *
     * @param expireHours 过期小时数
     * @return 清理的文件数量
     */
    int cleanExpiredTempFiles(int expireHours);

    /**
     * 获取当前使用的存储策略
     *
     * @return 存储策略实例
     */
    StorageStrategy getCurrentStorageStrategy();

    /**
     * 切换存储策略
     *
     * @param strategyType 策略类型
     * @return 是否切换成功
     */
    boolean switchStorageStrategy(String strategyType);

    /**
     * 获取所有可用的存储策略类型
     *
     * @return 策略类型列表
     */
    List<String> getAvailableStorageStrategies();

    /**
     * 验证存储策略配置
     *
     * @return 配置是否有效
     */
    boolean validateStorageConfiguration();

    /**
     * 获取存储策略统计信息
     *
     * @return 存储统计信息
     */
    StorageStrategy.StorageStatistics getStorageStatistics();

    /**
     * 文件统计信息VO
     */
    class FileStatisticsVO {
        private Long totalCount;
        private Long totalSize;
        private String totalSizeFormatted;
        private Long imageCount;
        private Long videoCount;
        private Long audioCount;
        private Long documentCount;
        private Long otherCount;

        // getters and setters
        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }

        public Long getTotalSize() { return totalSize; }
        public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }

        public String getTotalSizeFormatted() { return totalSizeFormatted; }
        public void setTotalSizeFormatted(String totalSizeFormatted) { this.totalSizeFormatted = totalSizeFormatted; }

        public Long getImageCount() { return imageCount; }
        public void setImageCount(Long imageCount) { this.imageCount = imageCount; }

        public Long getVideoCount() { return videoCount; }
        public void setVideoCount(Long videoCount) { this.videoCount = videoCount; }

        public Long getAudioCount() { return audioCount; }
        public void setAudioCount(Long audioCount) { this.audioCount = audioCount; }

        public Long getDocumentCount() { return documentCount; }
        public void setDocumentCount(Long documentCount) { this.documentCount = documentCount; }

        public Long getOtherCount() { return otherCount; }
        public void setOtherCount(Long otherCount) { this.otherCount = otherCount; }
    }
}
