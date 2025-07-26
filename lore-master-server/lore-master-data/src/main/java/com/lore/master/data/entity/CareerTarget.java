package com.lore.master.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 职业目标实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "career_targets")
public class CareerTarget {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 职业目标名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 职业目标编码
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    
    /**
     * 描述
     */
    @Column(name = "description", length = 1000)
    private String description;
    
    /**
     * 分类
     */
    @Column(name = "category", length = 50)
    private String category;
    
    /**
     * 难度等级：1-入门，2-初级，3-中级，4-高级，5-专家
     */
    @Column(name = "difficulty_level")
    private Integer difficultyLevel;
    
    /**
     * 预计学习时长（小时）
     */
    @Column(name = "estimated_hours")
    private Integer estimatedHours;
    
    /**
     * 所需技能标签（JSON格式）
     */
    @Column(name = "required_skills", length = 1000)
    private String requiredSkills;
    
    /**
     * 学习路径（JSON格式）
     */
    @Column(name = "learning_path", columnDefinition = "TEXT")
    private String learningPath;
    
    /**
     * 推荐资源（JSON格式）
     */
    @Column(name = "recommended_resources", columnDefinition = "TEXT")
    private String recommendedResources;
    
    /**
     * 排序权重
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    /**
     * 状态：1-启用，0-禁用
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 创建人ID
     */
    @Column(name = "creator_id")
    private Long creatorId;
    
    /**
     * 创建人姓名
     */
    @Column(name = "creator_name", length = 100)
    private String creatorName;
    
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
