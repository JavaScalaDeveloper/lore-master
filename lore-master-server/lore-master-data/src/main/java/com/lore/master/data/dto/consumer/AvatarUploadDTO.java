package com.lore.master.data.dto.consumer;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 头像上传请求DTO
 */
@Data
public class AvatarUploadDTO {
    
    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    /**
     * 头像文件
     */
    @NotNull(message = "头像文件不能为空")
    private MultipartFile file;
    
    /**
     * 备注信息
     */
    private String remark;
}
