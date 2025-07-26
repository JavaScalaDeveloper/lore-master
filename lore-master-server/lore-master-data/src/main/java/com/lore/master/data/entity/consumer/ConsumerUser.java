package com.lore.master.data.entity.consumer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * C端用户实体
 */
@Data
@Entity
@Table(name = "users", schema = "lore_consumer")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class ConsumerUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户唯一ID（对外展示，格式：YYYYXXXXXXXX）
     */
    @Column(name = "user_id", nullable = false, unique = true, length = 12)
    private String userId;

    /**
     * 昵称
     */
    @Column(name = "nickname", length = 100)
    private String nickname;

    /**
     * 真实姓名
     */
    @Column(name = "real_name", length = 100)
    private String realName;

    /**
     * 头像链接
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * 性别：1男 2女 0未知
     */
    @Column(name = "gender")
    private Integer gender;

    /**
     * 出生日期
     */
    @Column(name = "birth_date")
    private java.sql.Date birthDate;

    /**
     * 个人简介
     */
    @Column(name = "bio", length = 500)
    private String bio;

    /**
     * 当前等级
     */
    @Column(name = "current_level")
    private Integer currentLevel;

    /**
     * 总积分
     */
    @Column(name = "total_score")
    private Integer totalScore;

    /**
     * 学习天数
     */
    @Column(name = "study_days")
    private Integer studyDays;



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
     * 登录次数
     */
    @Column(name = "login_count")
    private Integer loginCount;

    /**
     * 状态：1正常 0禁用 2待激活
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 是否已验证：1已验证 0未验证
     */
    @Column(name = "is_verified")
    private Integer isVerified;

    /**
     * 扩展信息（JSON格式）
     */
    @Column(name = "extra_info", columnDefinition = "JSON")
    private String extraInfo;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

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
