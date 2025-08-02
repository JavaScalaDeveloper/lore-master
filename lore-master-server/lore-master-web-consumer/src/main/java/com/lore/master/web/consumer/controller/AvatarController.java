package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.common.util.JwtUtil;
import com.lore.master.data.entity.consumer.ConsumerUser;
import com.lore.master.data.repository.consumer.ConsumerUserRepository;
import com.lore.master.data.dto.consumer.AvatarUploadDTO;
import com.lore.master.data.vo.consumer.AvatarUploadVO;
import com.lore.master.service.consumer.UserAvatarService;
import com.lore.master.service.middleware.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

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
    private final UserAvatarService userAvatarService;

    /**
     * 上传用户头像（使用JPA Repository实现）
     */
    @PostMapping("/upload")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "remark", required = false) String remark,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        try {
            // 从Authorization头中获取token
        if (StringUtils.isEmpty(authorization)) {
            return Result.error("未提供Authorization令牌");
        }

        // 提取token（去掉Bearer前缀）
        String token = authorization.replace("Bearer ", "");

        // 验证token并获取userId
        String userId = JwtUtil.getUserIdFromToken(token);
        if (StringUtils.isEmpty(userId)) {
            return Result.error("无效的令牌或令牌已过期");
        }

        log.info("接收到头像上传请求: userId={}, fileName={}, fileSize={}",
                userId, file.getOriginalFilename(), file.getSize());

        // 1. 验证用户是否存在
        Optional<ConsumerUser> userOpt = consumerUserRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            return Result.error("用户不存在: " + userId);
        }

            ConsumerUser user = userOpt.get();

            // 2. 创建AvatarUploadDTO
            AvatarUploadDTO uploadDTO = new AvatarUploadDTO();
            uploadDTO.setUserId(userId);
            uploadDTO.setFile(file);
            uploadDTO.setRemark(remark);

            // 3. 调用服务层上传头像
            AvatarUploadVO uploadResult = userAvatarService.uploadUserAvatar(uploadDTO);

            // 4. 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("fileId", uploadResult.getFileId());
            responseData.put("fileName", uploadResult.getFileName());
            responseData.put("fileSize", uploadResult.getFileSize());
            responseData.put("fileSizeFormatted", uploadResult.getFileSizeFormatted());
            responseData.put("accessUrl", uploadResult.getAccessUrl());
            responseData.put("downloadUrl", uploadResult.getDownloadUrl());
            responseData.put("uploadTime", uploadResult.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            responseData.put("fileType", uploadResult.getFileType());
            responseData.put("fileCategory", uploadResult.getFileCategory());
            responseData.put("bucketName", uploadResult.getBucketName());
            responseData.put("md5Hash", uploadResult.getMd5Hash());
            responseData.put("fileExtension", uploadResult.getFileExtension());
            responseData.put("userId", uploadResult.getUserId());
            responseData.put("userNickname", uploadResult.getUserNickname());

            log.info("用户头像上传成功: userId={}, actualFileId={}, fileName={}, accessUrl={}",
                    userId, uploadResult.getFileId(), uploadResult.getFileName(), uploadResult.getAccessUrl());

            return Result.success("头像上传成功", responseData);

        } catch (Exception e) {
            log.error("用户头像上传失败: fileName={}",
                    file != null ? file.getOriginalFilename() : "unknown", e);
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户头像URL
     */
    @GetMapping("/url")
    public Result<Map<String, String>> getAvatarUrl(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            // 从Authorization头中获取token
            if (StringUtils.isEmpty(authorization)) {
                return Result.error("未提供Authorization令牌");
            }

            // 提取token（去掉Bearer前缀）
            String token = authorization.replace("Bearer ", "");

            // 验证token并获取userId
            String userId = JwtUtil.getUserIdFromToken(token);
            if (StringUtils.isEmpty(userId)) {
                return Result.error("无效的令牌或令牌已过期");
            }

            log.info("接收到获取头像URL请求: userId={}", userId);

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
            log.error("获取用户头像URL失败", e);
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
