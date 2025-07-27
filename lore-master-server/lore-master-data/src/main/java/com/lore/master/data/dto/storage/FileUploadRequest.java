package com.lore.master.data.dto.storage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传请求DTO
 */
@Data
public class FileUploadRequest {

    /**
     * 上传的文件
     */
    @NotNull(message = "上传文件不能为空")
    private MultipartFile file;

    /**
     * 存储桶名称
     */
    private String bucketName = "default";

    /**
     * 文件路径（可选，用于指定存储路径）
     */
    private String filePath;

    /**
     * 是否公开访问
     */
    private Boolean isPublic = false;

    /**
     * 上传用户ID
     */
    private String uploadUserId;

    /**
     * 上传用户类型
     */
    private String uploadUserType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否覆盖同名文件
     */
    private Boolean overwrite = false;

    /**
     * 自定义文件名（可选）
     */
    private String customFileName;
}
