package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件查看控制器（简化版，使用查询参数风格，不使用RESTful风格）
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileViewController {

    @PersistenceContext(unitName = "storage")
    private EntityManager storageEntityManager;

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

            // 从数据库查询文件信息
            String sql = "SELECT file_id, original_name, file_size, file_type, file_data, status FROM file_storage WHERE file_id = ?";
            Query query = storageEntityManager.createNativeQuery(sql);
            query.setParameter(1, fileId);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            if (results.isEmpty()) {
                log.warn("文件不存在: fileId={}", fileId);
                handleFileError(response, "File not found: " + fileId, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Object[] fileData = results.get(0);
            String originalName = (String) fileData[1];
            Long fileSize = ((Number) fileData[2]).longValue();
            String fileType = (String) fileData[3];
            byte[] binaryData = (byte[]) fileData[4];
            Boolean status = (Boolean) fileData[5];

            // 检查文件状态
            if (status == null || !status) {
                log.warn("文件已被删除: fileId={}, status={}", fileId, status);
                handleFileError(response, "File has been deleted: " + fileId, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (binaryData == null || binaryData.length == 0) {
                log.warn("文件数据为空: fileId={}", fileId);
                handleFileError(response, "File data not found: " + fileId, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 更新访问统计
            updateAccessCount(fileId);

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

            // 从数据库查询文件信息
            String sql = "SELECT file_id, original_name, file_size, file_type, file_data, status FROM file_storage WHERE file_id = ?";
            Query query = storageEntityManager.createNativeQuery(sql);
            query.setParameter(1, fileId);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            if (results.isEmpty()) {
                log.warn("文件不存在: fileId={}", fileId);
                handleFileError(response, "File not found: " + fileId, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Object[] fileData = results.get(0);
            String originalName = (String) fileData[1];
            Long fileSize = ((Number) fileData[2]).longValue();
            String fileType = (String) fileData[3];
            byte[] binaryData = (byte[]) fileData[4];
            Boolean status = (Boolean) fileData[5];

            // 检查文件状态
            if (status == null || !status) {
                log.warn("文件已被删除: fileId={}, status={}", fileId, status);
                handleFileError(response, "File has been deleted: " + fileId, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            if (binaryData == null || binaryData.length == 0) {
                log.warn("文件数据为空: fileId={}", fileId);
                handleFileError(response, "File data not found: " + fileId, HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 更新访问统计
            updateAccessCount(fileId);

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
            String sql = "SELECT COUNT(*) FROM file_storage WHERE file_id = ? AND status = true";
            Query query = storageEntityManager.createNativeQuery(sql);
            query.setParameter(1, fileId);

            Number count = (Number) query.getSingleResult();
            boolean exists = count.intValue() > 0;

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
            String sql = "SELECT file_id, original_name, file_size, file_type, file_category, bucket_name, " +
                        "is_public, created_time, access_count, last_access_time, status FROM file_storage WHERE file_id = ?";
            Query query = storageEntityManager.createNativeQuery(sql);
            query.setParameter(1, fileId);

            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            if (results.isEmpty()) {
                return Result.error("文件不存在: " + fileId);
            }

            Object[] data = results.get(0);
            Boolean status = (Boolean) data[10];

            if (status == null || !status) {
                return Result.error("文件已被删除: " + fileId);
            }

            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileId", data[0]);
            fileInfo.put("fileName", data[1]);
            fileInfo.put("fileSize", data[2]);
            fileInfo.put("fileType", data[3]);
            fileInfo.put("fileCategory", data[4]);
            fileInfo.put("bucketName", data[5]);
            fileInfo.put("isPublic", data[6]);
            fileInfo.put("accessUrl", "/api/file/view?fileId=" + fileId);
            fileInfo.put("downloadUrl", "/api/file/download?fileId=" + fileId);
            fileInfo.put("uploadTime", data[7]);
            fileInfo.put("accessCount", data[8]);
            fileInfo.put("lastAccessTime", data[9]);

            return Result.success("获取文件信息成功", fileInfo);

        } catch (Exception e) {
            log.error("获取文件信息失败: fileId={}", fileId, e);
            return Result.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新访问统计
     */
    private void updateAccessCount(String fileId) {
        try {
            String updateSql = "UPDATE file_storage SET access_count = access_count + 1, last_access_time = NOW() WHERE file_id = ?";
            Query updateQuery = storageEntityManager.createNativeQuery(updateSql);
            updateQuery.setParameter(1, fileId);
            updateQuery.executeUpdate();

            log.debug("更新文件访问统计成功: fileId={}", fileId);
        } catch (Exception e) {
            log.warn("更新访问统计失败: fileId={}, error={}", fileId, e.getMessage());
        }
    }

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
