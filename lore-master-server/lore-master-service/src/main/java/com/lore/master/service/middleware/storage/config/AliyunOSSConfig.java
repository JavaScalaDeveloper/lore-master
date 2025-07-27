package com.lore.master.service.middleware.storage.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "file-storage.aliyun-oss")
@ConditionalOnProperty(name = "file-storage.strategy", havingValue = "aliyun-oss")
public class AliyunOSSConfig {

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

    /**
     * 最大连接数
     */
    private Integer maxConnections = 1024;

    /**
     * 是否启用CNAME
     */
    private Boolean enableCname = false;

    /**
     * 验证配置是否完整
     * 
     * @return 配置是否有效
     */
    public boolean isValid() {
        return accessKeyId != null && !accessKeyId.trim().isEmpty()
                && accessKeySecret != null && !accessKeySecret.trim().isEmpty()
                && endpoint != null && !endpoint.trim().isEmpty()
                && bucketName != null && !bucketName.trim().isEmpty();
    }

    /**
     * 获取完整的端点URL
     * 
     * @return 端点URL
     */
    public String getFullEndpoint() {
        if (endpoint == null) {
            return null;
        }
        
        String protocol = useHttps ? "https://" : "http://";
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        
        return protocol + endpoint;
    }

    /**
     * 获取访问域名
     * 
     * @return 访问域名
     */
    public String getAccessDomain() {
        if (customDomain != null && !customDomain.trim().isEmpty()) {
            return customDomain;
        }
        
        if (endpoint != null && bucketName != null) {
            String protocol = useHttps ? "https://" : "http://";
            return protocol + bucketName + "." + endpoint.replace("http://", "").replace("https://", "");
        }
        
        return null;
    }
}
