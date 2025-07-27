package com.lore.master.service.middleware.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件存储配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "file-storage")
public class FileStorageProperties {

    /**
     * 存储策略类型
     * 可选值: mysql, aliyun-oss, local-file, minio 等
     */
    private String strategy = "mysql";

    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize = 100 * 1024 * 1024L; // 100MB

    /**
     * 允许的文件类型
     */
    private String[] allowedTypes = {
        "image/*", "video/*", "audio/*", 
        "application/pdf", "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };

    /**
     * 临时文件过期时间（小时）
     */
    private Integer tempFileExpireHours = 24;

    /**
     * 是否启用文件去重
     */
    private Boolean enableDeduplication = true;

    /**
     * 是否启用访问日志
     */
    private Boolean enableAccessLog = true;

    /**
     * 默认存储桶名称
     */
    private String defaultBucketName = "default";

    /**
     * URL签名过期时间（分钟）
     */
    private Integer urlExpireMinutes = 60;

    /**
     * MySQL存储配置
     */
    private MySQLConfig mysql = new MySQLConfig();

    /**
     * 阿里云OSS存储配置
     */
    private AliyunOSSConfig aliyunOss = new AliyunOSSConfig();

    /**
     * 本地文件存储配置
     */
    private LocalFileConfig localFile = new LocalFileConfig();

    /**
     * MySQL存储配置
     */
    @Data
    public static class MySQLConfig {
        /**
         * 是否压缩存储
         */
        private Boolean enableCompression = false;

        /**
         * 压缩阈值（字节）
         */
        private Long compressionThreshold = 1024 * 1024L; // 1MB
    }

    /**
     * 阿里云OSS存储配置
     */
    @Data
    public static class AliyunOSSConfig {
        /**
         * 访问密钥ID
         */
        private String accessKeyId;

        /**
         * 访问密钥Secret
         */
        private String accessKeySecret;

        /**
         * 端点地址
         */
        private String endpoint;

        /**
         * 存储桶名称
         */
        private String bucketName;

        /**
         * 自定义域名
         */
        private String customDomain;

        /**
         * 是否使用HTTPS
         */
        private Boolean useHttps = true;

        /**
         * 连接超时时间（毫秒）
         */
        private Integer connectionTimeout = 10000;

        /**
         * Socket超时时间（毫秒）
         */
        private Integer socketTimeout = 50000;
    }

    /**
     * 本地文件存储配置
     */
    @Data
    public static class LocalFileConfig {
        /**
         * 存储根目录
         */
        private String rootPath = "/data/files";

        /**
         * 是否按日期分目录
         */
        private Boolean enableDatePath = true;

        /**
         * 访问URL前缀
         */
        private String urlPrefix = "/files";
    }
}
