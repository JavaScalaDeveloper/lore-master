package com.lore.master.data.entity.admin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 学习技能目录实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "learning_skill_catalog", indexes = {
    @Index(name = "idx_skill_code", columnList = "skillCode", unique = true),
    @Index(name = "idx_skill_path", columnList = "skillPath"),
    @Index(name = "idx_level", columnList = "level"),
    @Index(name = "idx_parent_code", columnList = "parentCode"),
    @Index(name = "idx_sort_order", columnList = "sortOrder"),
    @Index(name = "idx_is_active", columnList = "isActive")
})
public class LearningSkillCatalog {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 技能编码，唯一标识
     */
    @Column(name = "skill_code", nullable = false, length = 50, unique = true)
    private String skillCode;

    /**
     * 技能名称
     */
    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    /**
     * 技能路径，用/分隔，如：foreign_language/english/cet4
     */
    @Column(name = "skill_path", nullable = false, length = 200)
    private String skillPath;

    /**
     * 层级：1-一级分类，2-二级分类，3-三级目标
     */
    @Column(name = "level", nullable = false)
    private Integer level;

    /**
     * 父级技能编码
     */
    @Column(name = "parent_code", length = 50)
    private String parentCode;

    /**
     * 图标标识
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * 技能描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 排序序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 是否启用：1-启用，0-禁用
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * 难度等级：beginner/intermediate/advanced
     */
    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;

    /**
     * 预估学习时长（小时）
     */
    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    /**
     * 标签，JSON格式存储
     */
    @Column(name = "tags", length = 500)
    private String tags;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy = "system";

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy = "system";

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdTime = now;
        this.updatedTime = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
}
