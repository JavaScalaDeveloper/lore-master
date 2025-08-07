package com.lore.master.data.entity.admin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 管理端知识图谱实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "admin_knowledge_map")
public class AdminKnowledgeMap {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 节点编码（唯一标识）
     */
    @Column(name = "node_code", nullable = false, unique = true, length = 100)
    private String nodeCode;
    
    /**
     * 节点名称
     */
    @Column(name = "node_name", nullable = false, length = 200)
    private String nodeName;
    
    /**
     * 节点类型：ROOT-根节点，LEVEL-层级节点，BRANCH-分支节点，LEAF-叶子节点
     */
    @Column(name = "node_type", nullable = false, length = 20)
    private String nodeType;
    
    /**
     * 父节点编码
     */
    @Column(name = "parent_code", length = 100)
    private String parentCode;
    
    /**
     * 根节点编码
     */
    @Column(name = "root_code", nullable = false, length = 100)
    private String rootCode;
    
    /**
     * 节点路径（用/分隔的完整路径）
     */
    @Column(name = "node_path", nullable = false, length = 500)
    private String nodePath;
    
    /**
     * 层级深度（从1开始）
     */
    @Column(name = "level_depth", nullable = false)
    private Integer levelDepth;
    
    /**
     * 层级类型（如L1、L2等）
     */
    @Column(name = "level_type", length = 20)
    private String levelType;
    
    /**
     * 排序序号
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    
    /**
     * 技能目录编码
     */
    @Column(name = "skill_catalog_code", length = 100)
    private String skillCatalogCode;
    
    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * 难度等级：BEGINNER-初级，INTERMEDIATE-中级，ADVANCED-高级，EXPERT-专家
     */
    @Column(name = "difficulty_level", nullable = false, length = 20)
    private String difficultyLevel;
    
    /**
     * 预估学习时长（小时）
     */
    @Column(name = "estimated_hours", nullable = false)
    private Integer estimatedHours;
    
    /**
     * 状态：ACTIVE-启用，INACTIVE-禁用
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    /**
     * 创建人
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    /**
     * 创建时间
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    /**
     * 创建时自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
        if (updatedTime == null) {
            updatedTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (estimatedHours == null) {
            estimatedHours = 0;
        }
    }
    
    /**
     * 更新时自动设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
