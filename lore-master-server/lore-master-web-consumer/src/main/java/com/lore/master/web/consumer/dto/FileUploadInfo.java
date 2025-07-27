package com.lore.master.web.consumer.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传信息封装类
 */
@Data
public class FileUploadInfo {
    
    /**
     * 文件ID
     */
    private String fileId;
    
    /**
     * 上传的文件对象
     */
    private MultipartFile file;
    
    /**
     * 生成的文件名
     */
    private String fileName;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 文件扩展名
     */
    private String fileExtension;
    
    /**
     * 文件二进制数据
     */
    private byte[] fileData;
    
    /**
     * MD5哈希值
     */
    private String md5Hash;
    
    /**
     * SHA256哈希值
     */
    private String sha256Hash;
    
    /**
     * 上传用户ID
     */
    private String userId;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 文件分类
     */
    private String fileCategory;
    
    /**
     * 存储桶名称
     */
    private String bucketName;
    
    /**
     * 是否公开
     */
    private Boolean isPublic;
    
    /**
     * 构造方法
     */
    public FileUploadInfo() {
        this.fileCategory = "image";
        this.bucketName = "avatars";
        this.isPublic = true;
    }
    
    /**
     * 便捷构造方法
     */
    public FileUploadInfo(String fileId, MultipartFile file, String userId, String userNickname, String remark) {
        this();
        this.fileId = fileId;
        this.file = file;
        this.userId = userId;
        this.userNickname = userNickname;
        this.remark = remark;
    }
}
