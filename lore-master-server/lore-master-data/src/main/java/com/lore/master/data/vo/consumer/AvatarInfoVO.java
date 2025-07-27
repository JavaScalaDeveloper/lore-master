package com.lore.master.data.vo.consumer;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 头像信息VO
 */
@Data
public class AvatarInfoVO {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 是否有头像
     */
    private Boolean hasAvatar;
    
    /**
     * 文件ID
     */
    private String fileId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 格式化的文件大小
     */
    private String fileSizeFormatted;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
    
    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;
    
    /**
     * 访问次数
     */
    private Long accessCount;
}
