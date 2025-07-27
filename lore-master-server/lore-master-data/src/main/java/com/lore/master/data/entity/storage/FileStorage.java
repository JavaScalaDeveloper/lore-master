package com.lore.master.data.entity.storage;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 文件存储实体
 */
@Data
@Entity
@Table(name = "file_storage")
@EqualsAndHashCode(callSuper = false)
public class FileStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "file_id", nullable = false, unique = true, length = 64)
    @Comment("文件唯一标识")
    private String fileId;

    @Column(name = "original_name", nullable = false)
    @Comment("原始文件名")
    private String originalName;

    @Column(name = "file_name", nullable = false)
    @Comment("存储文件名")
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 500)
    @Comment("文件路径")
    private String filePath;

    @Column(name = "file_size", nullable = false)
    @Comment("文件大小(字节)")
    private Long fileSize;

    @Column(name = "file_type", nullable = false, length = 100)
    @Comment("文件类型(MIME类型)")
    private String fileType;

    @Column(name = "file_extension", nullable = false, length = 20)
    @Comment("文件扩展名")
    private String fileExtension;

    @Column(name = "file_category", nullable = false, length = 50)
    @Comment("文件分类")
    private String fileCategory;

    @Lob
    @Column(name = "file_data", nullable = false)
    @Comment("文件二进制数据")
    private byte[] fileData;

    @Column(name = "md5_hash", nullable = false, length = 32, unique = true)
    @Comment("文件MD5哈希值")
    private String md5Hash;

    @Column(name = "sha256_hash", length = 64)
    @Comment("文件SHA256哈希值")
    private String sha256Hash;

    @Column(name = "upload_user_id", length = 64)
    @Comment("上传用户ID")
    private String uploadUserId;

    @Column(name = "upload_user_type", length = 20)
    @Comment("上传用户类型")
    private String uploadUserType;

    @Column(name = "bucket_name", nullable = false, length = 100)
    @Comment("存储桶名称")
    private String bucketName = "default";

    @Column(name = "is_public", nullable = false)
    @Comment("是否公开访问")
    private Boolean isPublic = false;

    @Column(name = "access_count", nullable = false)
    @Comment("访问次数")
    private Long accessCount = 0L;

    @Column(name = "last_access_time")
    @Comment("最后访问时间")
    private LocalDateTime lastAccessTime;

    @Column(name = "status", nullable = false)
    @Comment("状态")
    private Integer status = 1;

    @Column(name = "remark", length = 500)
    @Comment("备注")
    private String remark;

    @Column(name = "created_time", nullable = false)
    @Comment("创建时间")
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    @Comment("更新时间")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdTime == null) {
            createdTime = now;
        }
        if (updatedTime == null) {
            updatedTime = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    /**
     * 文件分类枚举
     */
    public enum FileCategory {
        IMAGE("image", "图片"),
        VIDEO("video", "视频"),
        AUDIO("audio", "音频"),
        DOCUMENT("document", "文档"),
        OTHER("other", "其他");

        private final String code;
        private final String name;

        FileCategory(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static FileCategory fromMimeType(String mimeType) {
            if (mimeType == null) {
                return OTHER;
            }
            
            String type = mimeType.toLowerCase();
            if (type.startsWith("image/")) {
                return IMAGE;
            } else if (type.startsWith("video/")) {
                return VIDEO;
            } else if (type.startsWith("audio/")) {
                return AUDIO;
            } else if (type.contains("pdf") || type.contains("document") || 
                      type.contains("text") || type.contains("word") || 
                      type.contains("excel") || type.contains("powerpoint")) {
                return DOCUMENT;
            } else {
                return OTHER;
            }
        }
    }

    /**
     * 用户类型枚举
     */
    public enum UserType {
        ADMIN("admin", "管理员"),
        BUSINESS("business", "商家"),
        CONSUMER("consumer", "用户");

        private final String code;
        private final String name;

        UserType(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }
}
