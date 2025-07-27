package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.entity.consumer.ConsumerUser;
import com.lore.master.data.repository.consumer.ConsumerUserRepository;
import com.lore.master.data.vo.storage.FileInfoVO;
import com.lore.master.service.middleware.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用户头像管理控制器（使用JPA Repository实现）
 */
@Slf4j
@RestController
@RequestMapping("/api/user/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final ConsumerUserRepository consumerUserRepository;
    private final FileStorageService fileStorageService;

    /**
     * 上传用户头像（使用JPA Repository实现）
     */
    @PostMapping("/upload")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "remark", required = false) String remark) {

        try {
            log.info("接收到头像上传请求: userId={}, fileName={}, fileSize={}",
                    userId, file.getOriginalFilename(), file.getSize());

            // 1. 验证用户是否存在
            Optional<ConsumerUser> userOpt = consumerUserRepository.findByUserId(userId);
            if (userOpt.isEmpty()) {
                return Result.error("用户不存在: " + userId);
            }

            ConsumerUser user = userOpt.get();

            // 2. 验证文件
            if (!validateAvatarFile(file)) {
                return Result.error("文件验证失败");
            }

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

            // 4. 使用FileStorageService上传文件
            FileInfoVO fileInfo = fileStorageService.uploadFile(file, userId, "consumer");
            String actualFileId = fileInfo.getFileId();

            // 5. 生成访问URL
            String avatarUrl = "/api/file/view?fileId=" + actualFileId;
            String downloadUrl = "/api/file/download?fileId=" + actualFileId;

            // 7. 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("fileId", actualFileId);
            responseData.put("fileName", fileInfo.getOriginalName());
            responseData.put("fileSize", fileInfo.getFileSize());
            responseData.put("fileSizeFormatted", fileInfo.getFileSizeFormatted());
            responseData.put("accessUrl", avatarUrl);
            responseData.put("downloadUrl", downloadUrl);
            responseData.put("uploadTime", fileInfo.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            responseData.put("fileType", fileInfo.getFileType());
            responseData.put("fileCategory", fileInfo.getFileCategory());
            responseData.put("bucketName", fileInfo.getBucketName());
            responseData.put("md5Hash", fileInfo.getMd5Hash());
            responseData.put("fileExtension", fileInfo.getFileExtension());
            responseData.put("userId", userId);
            responseData.put("userNickname", user.getNickname());

            log.info("用户头像上传成功: userId={}, actualFileId={}, fileName={}, accessUrl={}",
                    userId, actualFileId, fileInfo.getOriginalName(), avatarUrl);

            return Result.success("头像上传成功", responseData);

        } catch (Exception e) {
            log.error("用户头像上传失败: userId={}, fileName={}", userId,
                    file != null ? file.getOriginalFilename() : "unknown", e);
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户头像URL
     */
    @GetMapping("/url")
    public Result<Map<String, String>> getAvatarUrl(@RequestParam("userId") String userId) {
        try {
            Optional<ConsumerUser> userOpt = consumerUserRepository.findByUserId(userId);
            if (userOpt.isEmpty()) {
                return Result.error("用户不存在: " + userId);
            }

            ConsumerUser user = userOpt.get();
            String avatarUrl = user.getAvatarUrl();

            Map<String, String> result = new HashMap<>();
            result.put("userId", userId);
            result.put("avatarUrl", avatarUrl);
            result.put("hasAvatar", avatarUrl != null ? "true" : "false");

            return Result.success("获取头像URL成功", result);

        } catch (Exception e) {
            log.error("获取用户头像URL失败: userId={}", userId, e);
            return Result.error("获取头像URL失败: " + e.getMessage());
        }
    }

    /**
     * 验证头像文件
     */
    private boolean validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.error("头像文件不能为空");
            return false;
        }

        // 验证文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            log.error("头像文件大小不能超过5MB: {}", file.getSize());
            return false;
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !isImageFile(contentType)) {
            log.error("只支持上传图片文件: {}", contentType);
            return false;
        }

        return true;
    }

    /**
     * 检查是否为图片文件
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
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        if (digitGroups >= units.length) {
            digitGroups = units.length - 1;
        }

        double formattedSize = size / Math.pow(1024, digitGroups);
        return String.format("%.1f %s", formattedSize, units[digitGroups]);
    }

    // 文件处理相关方法已移至FileStorageService

    /**
     * 从URL中提取fileId
     */
    private String extractFileIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // 从查询参数格式的URL中提取fileId
        // 例如: /api/file/view?fileId=avatar_123_456&accessUserId=123
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

    // 所有文件存储相关的逻辑已移至FileStorageService，使用JPA Repository实现
}
