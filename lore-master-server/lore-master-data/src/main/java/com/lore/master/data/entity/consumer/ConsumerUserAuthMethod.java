package com.lore.master.data.entity.consumer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * C端用户认证方式实体
 */
@Data
@Entity
@Table(name = "user_auth_methods", schema = "lore_consumer")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class ConsumerUserAuthMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID（关联users.user_id）
     */
    @Column(name = "user_id", nullable = false, length = 12)
    private String userId;

    /**
     * 认证类型：username、email、phone、qq、wechat、weibo等
     */
    @Column(name = "auth_type", nullable = false, length = 20)
    private String authType;

    /**
     * 认证标识（用户名、邮箱、手机号、第三方openid等）
     */
    @Column(name = "auth_key", nullable = false, length = 255)
    private String authKey;

    /**
     * 认证密钥（密码hash、token等，第三方登录可为空）
     */
    @Column(name = "auth_secret", length = 255)
    private String authSecret;

    /**
     * 第三方平台用户ID
     */
    @Column(name = "third_party_id", length = 255)
    private String thirdPartyId;

    /**
     * 第三方平台UnionID（微信等）
     */
    @Column(name = "union_id", length = 255)
    private String unionId;



    /**
     * 是否已验证：1已验证 0未验证
     */
    @Column(name = "is_verified")
    private Integer isVerified;

    /**
     * 验证时间
     */
    @Column(name = "verified_time")
    private LocalDateTime verifiedTime;

    /**
     * 是否为主要登录方式：1是 0否
     */
    @Column(name = "is_primary")
    private Integer isPrimary;

    /**
     * 状态：1正常 0禁用
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 最后使用时间
     */
    @Column(name = "last_used_time")
    private LocalDateTime lastUsedTime;

    /**
     * 扩展信息（存储第三方平台返回的其他信息）
     */
    @Column(name = "extra_info", columnDefinition = "JSON")
    private String extraInfo;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @LastModifiedDate
    @Column(name = "modify_time", nullable = false)
    private LocalDateTime modifyTime;
}
