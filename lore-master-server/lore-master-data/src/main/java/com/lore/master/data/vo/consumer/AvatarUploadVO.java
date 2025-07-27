package com.lore.master.data.vo.consumer;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 头像上传响应VO
 */
@Data
public class AvatarUploadVO {
    
    /**
     * 文件ID
     */
    private String fileId;
    
    /**
     * 原始文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 格式化的文件大小
     */
    private String fileSizeFormatted;
    
    /**
     * 文件访问URL
     */
    private String accessUrl;
    
    /**
     * 文件下载URL
     */
    private String downloadUrl;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件分类
     */
    private String fileCategory;
    
    /**
     * 存储桶名称
     */
    private String bucketName;
    
    /**
     * 是否公开访问
     */
    private Boolean isPublic;
    
    /**
     * MD5哈希值
     */
    private String md5Hash;
    
    /**
     * 文件扩展名
     */
    private String fileExtension;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户昵称
     */
    private String userNickname;
}
