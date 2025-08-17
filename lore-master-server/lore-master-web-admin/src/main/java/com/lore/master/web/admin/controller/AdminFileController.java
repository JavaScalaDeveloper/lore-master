package com.lore.master.web.admin.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.dto.storage.FileUploadRequest;
import com.lore.master.data.vo.storage.FileInfoVO;
import com.lore.master.service.middleware.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

/**
 * 管理端文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/file")
@RequiredArgsConstructor
public class AdminFileController {

    private final FileStorageService fileStorageService;

    /**
     * 上传文件（用于课程内容）
     */
    @PostMapping("/upload")
    public Result<FileInfoVO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bucketName", defaultValue = "course-content") String bucketName,
            @RequestParam(value = "isPublic", defaultValue = "true") Boolean isPublic,
            @RequestParam(value = "remark", required = false) String remark) {
        
        try {
            log.info("管理端上传文件，文件名：{}，大小：{}", file.getOriginalFilename(), file.getSize());

            FileUploadRequest request = new FileUploadRequest();
            request.setFile(file);
            request.setUploadUserId("admin"); // TODO: 从当前登录用户获取
            request.setUploadUserType("admin");
            request.setBucketName(bucketName);
            request.setIsPublic(isPublic);
            request.setRemark(remark);

            FileInfoVO result = fileStorageService.uploadFile(request);
            
            log.info("管理端上传文件成功，fileId：{}", result.getFileId());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("管理端上传文件失败", e);
            return Result.error("上传文件失败：" + e.getMessage());
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    public Result<java.util.List<FileInfoVO>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "bucketName", defaultValue = "course-content") String bucketName,
            @RequestParam(value = "isPublic", defaultValue = "true") Boolean isPublic) {
        
        try {
            log.info("管理端批量上传文件，文件数量：{}", files.length);

            java.util.List<FileInfoVO> results = new java.util.ArrayList<>();
            
            for (MultipartFile file : files) {
                FileUploadRequest request = new FileUploadRequest();
                request.setFile(file);
                request.setUploadUserId("admin"); // TODO: 从当前登录用户获取
                request.setUploadUserType("admin");
                request.setBucketName(bucketName);
                request.setIsPublic(isPublic);
                request.setRemark("批量上传");

                FileInfoVO result = fileStorageService.uploadFile(request);
                results.add(result);
            }
            
            log.info("管理端批量上传文件成功，成功数量：{}", results.size());
            return Result.success(results);
            
        } catch (Exception e) {
            log.error("管理端批量上传文件失败", e);
            return Result.error("批量上传文件失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{fileId}")
    public Result<FileInfoVO> getFileInfo(@PathVariable String fileId) {
        try {
            log.info("管理端获取文件信息，fileId：{}", fileId);

            FileInfoVO result = fileStorageService.getFileInfo(fileId);
            if (result == null) {
                return Result.error("文件不存在");
            }

            return Result.success(result);
            
        } catch (Exception e) {
            log.error("管理端获取文件信息失败，fileId：{}", fileId, e);
            return Result.error("获取文件信息失败：" + e.getMessage());
        }
    }



    /**
     * 生成文件访问URL
     */
    @GetMapping("/url/{fileId}")
    public Result<String> generateAccessUrl(
            @PathVariable String fileId,
            @RequestParam(value = "expireMinutes", defaultValue = "60") Integer expireMinutes) {
        
        try {
            log.info("管理端生成文件访问URL，fileId：{}，过期时间：{}分钟", fileId, expireMinutes);

            String url = fileStorageService.generateAccessUrl(fileId, expireMinutes);
            
            return Result.success(url);
            
        } catch (Exception e) {
            log.error("管理端生成文件访问URL失败，fileId：{}", fileId, e);
            return Result.error("生成访问URL失败：" + e.getMessage());
        }
    }
}
