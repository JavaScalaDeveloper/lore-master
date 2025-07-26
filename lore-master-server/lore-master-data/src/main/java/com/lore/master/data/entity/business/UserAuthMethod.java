package com.lore.master.data.entity.business;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户登录方式实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "user_auth_methods", catalog = "lore_business")
public class UserAuthMethod {
    
    /**
     * 自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, length = 32)
    private String userId;
    
    /**
     * 认证类型：username、email、phone、wechat、qq、github等
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
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 修改时间
     */
    @UpdateTimestamp
    @Column(name = "modify_time", nullable = false)
    private LocalDateTime modifyTime;
}
