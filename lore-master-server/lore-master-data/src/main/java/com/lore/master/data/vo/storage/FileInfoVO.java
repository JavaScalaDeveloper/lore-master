package com.lore.master.data.vo.storage;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件信息响应VO
 */
@Data
public class FileInfoVO {

    /**
     * 文件唯一标识
     */
    private String fileId;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件大小(格式化)
     */
    private String fileSizeFormatted;

    /**
     * 文件类型(MIME类型)
     */
    private String fileType;

    /**
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * 文件分类
     */
    private String fileCategory;

    /**
     * 文件分类名称
     */
    private String fileCategoryName;

    /**
     * MD5哈希值
     */
    private String md5Hash;

    /**
     * 上传用户ID
     */
    private String uploadUserId;

    /**
     * 上传用户类型
     */
    private String uploadUserType;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 是否公开访问
     */
    private Boolean isPublic;

    /**
     * 访问次数
     */
    private Long accessCount;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 访问URL
     */
    private String accessUrl;

    /**
     * 下载URL
     */
    private String downloadUrl;

    /**
     * 缩略图URL（仅图片文件）
     */
    private String thumbnailUrl;

    /**
     * 是否为图片
     */
    private Boolean isImage;

    /**
     * 是否为视频
     */
    private Boolean isVideo;

    /**
     * 是否为音频
     */
    private Boolean isAudio;

    /**
     * 是否为文档
     */
    private Boolean isDocument;
}
