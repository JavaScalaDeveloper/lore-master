package com.lore.master.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 管理员用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "admin_users")
public class AdminUser {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 用户名
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * 密码（BCrypt加密）
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    /**
     * 真实姓名
     */
    @Column(name = "real_name", length = 100)
    private String realName;
    
    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;
    
    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;
    
    /**
     * 头像URL
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    /**
     * 角色：super_admin-超级管理员，admin-管理员，operator-操作员，viewer-查看者
     */
    @Column(name = "role", nullable = false, length = 20)
    private String role;
    
    /**
     * 状态：1-启用，0-禁用
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 登录次数
     */
    @Column(name = "login_count")
    private Integer loginCount;
    
    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
    
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
