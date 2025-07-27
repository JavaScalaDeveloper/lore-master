package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.vo.storage.FileInfoVO;
import com.lore.master.service.middleware.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 文件查看控制器（简化版，使用查询参数风格，不使用RESTful风格）
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileViewController {

    private final FileStorageService fileStorageService;

    /**
     * 查看文件（在线预览）- 使用查询参数
     * GET /api/file/view?fileId=xxx&accessUserId=xxx&accessUserType=consumer
     */
    @GetMapping("/view")
    public void viewFile(@RequestParam("fileId") String fileId,
                        @RequestParam(value = "accessUserId", required = false) String accessUserId,
                        @RequestParam(value = "accessUserType", defaultValue = "consumer") String accessUserType,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        try {
            log.info("接收到文件查看请求: fileId={}, accessUserId={}", fileId, accessUserId);

            // 获取文件信息
            FileInfoVO fileInfo = fileStorageService.getFileInfo(fileId);
            if (fileInfo == null) {
                log.warn("文件信息不存在: fileId={}", fileId);
                handleFileError(response, "File info not found: " + fileId, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 使用FileStorageService下载文件数据
            byte[] fileData = fileStorageService.downloadFile(fileId, accessUserId, "consumer", request.getRemoteAddr());
            if (fileData == null || fileData.length == 0) {
                log.warn("文件不存在或数据为空: fileId={}", fileId);
                handleFileError(response, "File not found: " + fileId, HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String originalName = fileInfo.getOriginalName();
            Long fileSize = fileInfo.getFileSize();
            String fileType = fileInfo.getFileType();
            byte[] binaryData = fileData;

            // 设置响应头
            response.setContentType(fileType);
            response.setContentLength(binaryData.length);
            response.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=3600"); // 缓存1小时

            // 设置为内联显示（浏览器直接显示图片）
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" +
                URLEncoder.encode(originalName, StandardCharsets.UTF_8) + "\"");

            // 写入响应
            response.getOutputStream().write(binaryData);
            response.getOutputStream().flush();

            log.info("文件查看成功: fileId={}, fileName={}, size={}",
                    fileId, originalName, binaryData.length);

        } catch (Exception e) {
            log.error("查看文件失败: fileId={}", fileId, e);
            handleFileError(response, "Internal server error: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 下载文件 - 使用查询参数
     * GET /api/file/download?fileId=xxx&accessUserId=xxx&accessUserType=consumer
     */
    @GetMapping("/download")
    public void downloadFile(@RequestParam("fileId") String fileId,
                           @RequestParam(value = "accessUserId", required = false) String accessUserId,
                           @RequestParam(value = "accessUserType", defaultValue = "consumer") String accessUserType,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        try {
            log.info("接收到文件下载请求: fileId={}, accessUserId={}", fileId, accessUserId);

            // 获取文件信息
            FileInfoVO fileInfo = fileStorageService.getFileInfo(fileId);
            if (fileInfo == null) {
                log.warn("文件信息不存在: fileId={}", fileId);
                handleFileError(response, "File info not found: " + fileId, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 使用FileStorageService下载文件数据
            byte[] fileData = fileStorageService.downloadFile(fileId, accessUserId, "consumer", request.getRemoteAddr());
            if (fileData == null || fileData.length == 0) {
                log.warn("文件不存在或数据为空: fileId={}", fileId);
                handleFileError(response, "File not found: " + fileId, HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String originalName = fileInfo.getOriginalName();
            byte[] binaryData = fileData;

            // 设置响应头（强制下载）
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentLength(binaryData.length);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                URLEncoder.encode(originalName, StandardCharsets.UTF_8) + "\"");

            // 写入响应
            response.getOutputStream().write(binaryData);
            response.getOutputStream().flush();

            log.info("文件下载成功: fileId={}, fileName={}, size={}",
                    fileId, originalName, binaryData.length);

        } catch (Exception e) {
            log.error("下载文件失败: fileId={}", fileId, e);
            handleFileError(response, "Internal server error: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 检查文件是否存在
     * GET /api/file/exists?fileId=xxx
     */
    @GetMapping("/exists")
    public Result<Boolean> fileExists(@RequestParam("fileId") String fileId) {
        try {
            boolean exists = fileStorageService.fileExists(fileId);
            return Result.success("文件存在性检查完成", exists);

        } catch (Exception e) {
            log.error("检查文件存在性失败: fileId={}", fileId, e);
            return Result.error("检查文件存在性失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     * GET /api/file/info?fileId=xxx
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getFileInfo(@RequestParam("fileId") String fileId) {
        try {
            FileInfoVO fileInfo = fileStorageService.getFileInfo(fileId);
            if (fileInfo == null) {
                return Result.error("文件不存在: " + fileId);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("fileId", fileInfo.getFileId());
            result.put("fileName", fileInfo.getOriginalName());
            result.put("fileSize", fileInfo.getFileSize());
            result.put("fileType", fileInfo.getFileType());
            result.put("fileCategory", fileInfo.getFileCategory());
            result.put("bucketName", fileInfo.getBucketName());
            result.put("isPublic", fileInfo.getIsPublic());
            result.put("accessUrl", "/api/file/view?fileId=" + fileId);
            result.put("downloadUrl", "/api/file/download?fileId=" + fileId);
            result.put("uploadTime", fileInfo.getCreatedTime());
            result.put("accessCount", fileInfo.getAccessCount());
            result.put("lastAccessTime", fileInfo.getLastAccessTime());

            return Result.success("获取文件信息成功", result);

        } catch (Exception e) {
            log.error("获取文件信息失败: fileId={}", fileId, e);
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }

    // 访问统计更新已移至FileStorageService

    /**
     * 处理文件错误
     */
    private void handleFileError(HttpServletResponse response, String message, int statusCode) {
        try {
            response.setStatus(statusCode);
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(message);
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("写入错误响应失败", e);
        }
    }
}
