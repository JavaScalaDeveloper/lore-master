package com.lore.master.data.entity.storage;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 文件访问日志实体
 */
@Data
@Entity
@Table(name = "file_access_log")
@EqualsAndHashCode(callSuper = false)
public class FileAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "file_id", nullable = false, length = 64)
    @Comment("文件ID")
    private String fileId;

    @Column(name = "access_user_id", length = 64)
    @Comment("访问用户ID")
    private String accessUserId;

    @Column(name = "access_user_type", length = 20)
    @Comment("访问用户类型")
    private String accessUserType;

    @Column(name = "access_ip", length = 45)
    @Comment("访问IP地址")
    private String accessIp;

    @Column(name = "access_user_agent", length = 500)
    @Comment("用户代理")
    private String accessUserAgent;

    @Column(name = "access_referer", length = 500)
    @Comment("来源页面")
    private String accessReferer;

    @Column(name = "access_type", nullable = false, length = 20)
    @Comment("访问类型")
    private String accessType;

    @Column(name = "access_time", nullable = false)
    @Comment("访问时间")
    private LocalDateTime accessTime;

    @PrePersist
    protected void onCreate() {
        if (accessTime == null) {
            accessTime = LocalDateTime.now();
        }
    }

    /**
     * 访问类型枚举
     */
    public enum AccessType {
        VIEW("view", "查看"),
        DOWNLOAD("download", "下载");

        private final String code;
        private final String name;

        AccessType(String code, String name) {
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
