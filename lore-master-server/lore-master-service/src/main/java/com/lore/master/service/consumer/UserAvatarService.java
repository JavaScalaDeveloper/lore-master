package com.lore.master.service.consumer;

import com.lore.master.data.dto.consumer.AvatarUploadDTO;
import com.lore.master.data.dto.storage.FileUploadRequest;
import com.lore.master.data.entity.consumer.ConsumerUser;
import com.lore.master.data.repository.consumer.ConsumerUserRepository;
import com.lore.master.data.vo.consumer.AvatarInfoVO;
import com.lore.master.data.vo.consumer.AvatarUploadVO;
import com.lore.master.data.vo.storage.FileInfoVO;
import com.lore.master.service.middleware.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * 用户头像服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private final FileStorageService fileStorageService;
    private final ConsumerUserRepository consumerUserRepository;

    /**
     * 上传用户头像
     * 
     * @param uploadDTO 上传请求
     * @return 头像上传结果
     */
    @Transactional(rollbackFor = Exception.class)
    public AvatarUploadVO uploadUserAvatar(AvatarUploadDTO uploadDTO) {
        try {
            String userId = uploadDTO.getUserId();
            MultipartFile file = uploadDTO.getFile();
            
            log.info("开始上传用户头像: userId={}, fileName={}, fileSize={}", 
                    userId, file.getOriginalFilename(), file.getSize());

            // 1. 验证用户是否存在
            Optional<ConsumerUser> userOpt = consumerUserRepository.findByUserId(userId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("用户不存在: " + userId);
            }

            ConsumerUser user = userOpt.get();

            // 2. 验证文件
            validateAvatarFile(file);

            // 3. 如果用户已有头像，先删除旧头像
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                try {
                    String oldFileId = extractFileIdFromUrl(user.getAvatarUrl());
                    if (oldFileId != null) {
                        fileStorageService.deleteFile(oldFileId, userId, "consumer");
                        log.info("已删除用户旧头像: userId={}, oldFileId={}", userId, oldFileId);
                    }
                } catch (Exception e) {
                    log.warn("删除旧头像失败，继续上传新头像: userId={}, error={}", userId, e.getMessage());
                }
            }

            // 4. 创建文件上传请求
            FileUploadRequest uploadRequest = new FileUploadRequest();
            uploadRequest.setFile(file);
            uploadRequest.setUploadUserId(userId);
            uploadRequest.setUploadUserType("consumer");
            uploadRequest.setBucketName("avatars");
            uploadRequest.setIsPublic(true);
            uploadRequest.setRemark(uploadDTO.getRemark() != null ? uploadDTO.getRemark() : "用户头像 - " + user.getNickname());
            uploadRequest.setOverwrite(true);

            // 5. 上传文件到存储服务
            FileInfoVO fileInfo = fileStorageService.uploadFile(uploadRequest);
            
            // 6. 更新用户头像信息
            String avatarUrl = fileInfo.getAccessUrl();
            user.setAvatarUrl(avatarUrl);
            consumerUserRepository.save(user);
            
            // 7. 构建返回结果
            AvatarUploadVO result = new AvatarUploadVO();
            result.setFileId(fileInfo.getFileId());
            result.setFileName(fileInfo.getOriginalName());
            result.setFileSize(fileInfo.getFileSize());
            result.setFileSizeFormatted(fileInfo.getFileSizeFormatted());
            result.setAccessUrl(fileInfo.getAccessUrl());
            result.setDownloadUrl(fileInfo.getDownloadUrl());
            result.setUploadTime(fileInfo.getCreatedTime());
            result.setFileType(fileInfo.getFileType());
            result.setFileCategory(fileInfo.getFileCategory());
            result.setBucketName(fileInfo.getBucketName());
            result.setIsPublic(fileInfo.getIsPublic());
            result.setMd5Hash(fileInfo.getMd5Hash());
            result.setFileExtension(fileInfo.getFileExtension());
            result.setUserId(userId);
            result.setUserNickname(user.getNickname());
            
            log.info("用户头像上传成功: userId={}, fileId={}, fileName={}, accessUrl={}", 
                    userId, fileInfo.getFileId(), fileInfo.getOriginalName(), fileInfo.getAccessUrl());

            return result;

        } catch (Exception e) {
            log.error("用户头像上传失败: userId={}, fileName={}", 
                    uploadDTO.getUserId(), 
                    uploadDTO.getFile() != null ? uploadDTO.getFile().getOriginalFilename() : "unknown", e);
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户头像信息
     * 
     * @param userId 用户ID
     * @return 头像信息
     */
    public AvatarInfoVO getUserAvatarInfo(String userId) {
        try {
            Optional<ConsumerUser> userOpt = consumerUserRepository.findByUserId(userId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("用户不存在: " + userId);
            }

            ConsumerUser user = userOpt.get();
            AvatarInfoVO result = new AvatarInfoVO();
            result.setUserId(userId);
            result.setUserNickname(user.getNickname());
            result.setAvatarUrl(user.getAvatarUrl());
            result.setHasAvatar(user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty());

            // 如果有头像，获取详细信息
            if (result.getHasAvatar()) {
                try {
                    String fileId = extractFileIdFromUrl(user.getAvatarUrl());
                    if (fileId != null) {
                        FileInfoVO fileInfo = fileStorageService.getFileInfo(fileId);
                        result.setFileId(fileInfo.getFileId());
                        result.setFileName(fileInfo.getOriginalName());
                        result.setFileSize(fileInfo.getFileSize());
                        result.setFileSizeFormatted(fileInfo.getFileSizeFormatted());
                        result.setFileType(fileInfo.getFileType());
                        result.setUploadTime(fileInfo.getCreatedTime());
                        result.setLastAccessTime(fileInfo.getLastAccessTime());
                        result.setAccessCount(fileInfo.getAccessCount());
                    }
                } catch (Exception e) {
                    log.warn("获取头像文件详细信息失败: userId={}, error={}", userId, e.getMessage());
                }
            }

            return result;

        } catch (Exception e) {
            log.error("获取用户头像信息失败: userId={}", userId, e);
            throw new RuntimeException("获取头像信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户头像
     * 
     * @param userId 用户ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserAvatar(String userId) {
        try {
            Optional<ConsumerUser> userOpt = consumerUserRepository.findByUserId(userId);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("用户不存在: " + userId);
            }

            ConsumerUser user = userOpt.get();
            if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
                log.info("用户没有设置头像，无需删除: userId={}", userId);
                return true;
            }

            // 删除文件存储中的头像
            String fileId = extractFileIdFromUrl(user.getAvatarUrl());
            if (fileId != null) {
                boolean deleteSuccess = fileStorageService.deleteFile(fileId, userId, "consumer");
                
                if (deleteSuccess) {
                    // 清空用户头像信息
                    user.setAvatarUrl(null);
                    consumerUserRepository.save(user);
                    log.info("用户头像删除成功: userId={}, fileId={}", userId, fileId);
                }
                
                return deleteSuccess;
            }

            return false;

        } catch (Exception e) {
            log.error("删除用户头像失败: userId={}", userId, e);
            throw new RuntimeException("删除头像失败: " + e.getMessage());
        }
    }

    /**
     * 验证头像文件
     * 
     * @param file 文件
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("头像文件不能为空");
        }

        // 验证文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("头像文件大小不能超过5MB");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !isImageFile(contentType)) {
            throw new RuntimeException("只支持上传图片文件（JPG、PNG、GIF、WebP）");
        }

        // 验证文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }
    }

    /**
     * 检查是否为图片文件
     * 
     * @param contentType MIME类型
     * @return 是否为图片
     */
    private boolean isImageFile(String contentType) {
        return contentType.startsWith("image/") && 
               (contentType.equals("image/jpeg") || 
                contentType.equals("image/jpg") || 
                contentType.equals("image/png") || 
                contentType.equals("image/gif") || 
                contentType.equals("image/webp"));
    }

    /**
     * 从URL中提取fileId
     * 
     * @param url URL
     * @return fileId
     */
    private String extractFileIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        // 从查询参数格式的URL中提取fileId
        int fileIdIndex = url.indexOf("fileId=");
        if (fileIdIndex == -1) {
            return null;
        }
        
        int startIndex = fileIdIndex + 7; // "fileId=".length()
        int endIndex = url.indexOf("&", startIndex);
        if (endIndex == -1) {
            endIndex = url.length();
        }
        
        return url.substring(startIndex, endIndex);
    }
}
