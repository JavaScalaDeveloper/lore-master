package com.lore.master.web.consumer.controller;

import com.alibaba.fastjson2.JSONObject;
import com.lore.master.common.result.Result;
import com.lore.master.data.entity.consumer.ConsumerUser;
import com.lore.master.data.repository.consumer.ConsumerUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户头像管理控制器（简化版，使用查询参数风格）
 */
@Slf4j
@RestController
@RequestMapping("/api/user/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final ConsumerUserRepository consumerUserRepository;

    @Autowired
    @Qualifier("storageEntityManager")
    private EntityManager storageEntityManager;

    /**
     * 上传用户头像
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

            // 3. 读取文件数据并计算哈希
            byte[] fileData = file.getBytes();
            String md5Hash = calculateMD5(fileData);
            String sha256Hash = calculateSHA256(fileData);

            // 4. 如果用户已有头像，先删除旧头像
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                try {
                    String oldFileId = extractFileIdFromUrl(user.getAvatarUrl());
                    if (oldFileId != null) {
                        deleteOldAvatar(oldFileId);
                        log.info("已删除用户旧头像: userId={}, oldFileId={}", userId, oldFileId);
                    }
                } catch (Exception e) {
                    log.warn("删除旧头像失败，继续上传新头像: userId={}, error={}", userId, e.getMessage());
                }
            }

            // 5. 生成文件ID和文件信息
            String fileId = "avatar_" + userId + "_" + System.currentTimeMillis();
            String fileName = generateFileName(file.getOriginalFilename());
            String filePath = generateFilePath("avatars", fileName);
            String fileExtension = getFileExtension(file.getOriginalFilename());

            // 6. 插入文件数据到数据库
            String insertSql = "INSERT INTO file_storage (file_id, original_name, file_name, file_path, file_size, " +
                    "file_type, file_extension, file_category, file_data, md5_hash, sha256_hash, " +
                    "upload_user_id, upload_user_type, bucket_name, is_public, remark, status, access_count, " +
                    "created_time, updated_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

            Query insertQuery = storageEntityManager.createNativeQuery(insertSql);
            insertQuery.setParameter(1, fileId);
            insertQuery.setParameter(2, file.getOriginalFilename());
            insertQuery.setParameter(3, fileName);
            insertQuery.setParameter(4, filePath);
            insertQuery.setParameter(5, file.getSize());
            insertQuery.setParameter(6, file.getContentType());
            insertQuery.setParameter(7, fileExtension);
            insertQuery.setParameter(8, "image");
            insertQuery.setParameter(9, fileData);
            insertQuery.setParameter(10, md5Hash);
            insertQuery.setParameter(11, sha256Hash);
            insertQuery.setParameter(12, userId);
            insertQuery.setParameter(13, "consumer");
            insertQuery.setParameter(14, "avatars");
            insertQuery.setParameter(15, true);
            insertQuery.setParameter(16, remark != null ? remark : "用户头像 - " + user.getNickname());
            insertQuery.setParameter(17, 1);
            insertQuery.setParameter(18, 0L);
            try {
                int result = insertQuery.executeUpdate();

                if (result <= 0) {
                    throw new RuntimeException("文件保存到数据库失败");
                }
            } catch (Exception e) {
                log.warn("文件存在，query={}", JSONObject.toJSONString(insertQuery), e);
            }

            // 7. 生成访问URL（使用查询参数格式）
            String avatarUrl = "/api/file/view?fileId=" + fileId + "&accessUserId=" + userId + "&accessUserType=consumer";
            String downloadUrl = "/api/file/download?fileId=" + fileId + "&accessUserId=" + userId + "&accessUserType=consumer";

            // 8. 更新用户头像信息
            user.setAvatarUrl(avatarUrl);
            consumerUserRepository.save(user);

            // 9. 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("fileId", fileId);
            responseData.put("fileName", file.getOriginalFilename());
            responseData.put("fileSize", file.getSize());
            responseData.put("fileSizeFormatted", formatFileSize(file.getSize()));
            responseData.put("accessUrl", avatarUrl);
            responseData.put("downloadUrl", downloadUrl);
            responseData.put("uploadTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            responseData.put("fileType", file.getContentType());
            responseData.put("fileCategory", "image");
            responseData.put("bucketName", "avatars");
            responseData.put("md5Hash", md5Hash);
            responseData.put("fileExtension", fileExtension);
            responseData.put("userId", userId);
            responseData.put("userNickname", user.getNickname());

            log.info("用户头像上传成功: userId={}, fileId={}, fileName={}, accessUrl={}",
                    userId, fileId, file.getOriginalFilename(), avatarUrl);

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

    /**
     * 生成文件名
     */
    private String generateFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        return uuid + (extension.isEmpty() ? "" : "." + extension);
    }

    /**
     * 生成文件路径
     */
    private String generateFilePath(String bucketName, String fileName) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%s/%d/%02d/%02d/%s",
                bucketName, now.getYear(), now.getMonthValue(), now.getDayOfMonth(), fileName);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

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

    /**
     * 删除旧头像
     */
    private void deleteOldAvatar(String fileId) {
        try {
            String deleteSql = "UPDATE file_storage SET status = 0, updated_time = NOW() WHERE file_id = ?";
            Query deleteQuery = storageEntityManager.createNativeQuery(deleteSql);
            deleteQuery.setParameter(1, fileId);
            deleteQuery.executeUpdate();
        } catch (Exception e) {
            log.warn("删除旧头像失败: fileId={}, error={}", fileId, e.getMessage());
        }
    }

    /**
     * 计算MD5哈希
     */
    private String calculateMD5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("计算MD5失败", e);
            return "";
        }
    }

    /**
     * 计算SHA256哈希
     */
    private String calculateSHA256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("计算SHA256失败", e);
            return "";
        }
    }
}
