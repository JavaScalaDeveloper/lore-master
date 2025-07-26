package com.lore.master.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 知识点实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "knowledge_points")
public class KnowledgePoint {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 知识点标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    /**
     * 知识点编码
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    
    /**
     * 知识点内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    /**
     * 知识点摘要
     */
    @Column(name = "summary", length = 500)
    private String summary;
    
    /**
     * 所属学科ID
     */
    @Column(name = "subject_id")
    private Long subjectId;
    
    /**
     * 所属学科名称
     */
    @Column(name = "subject_name", length = 100)
    private String subjectName;
    
    /**
     * 父级知识点ID
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 知识点层级
     */
    @Column(name = "level")
    private Integer level;
    
    /**
     * 难度等级：1-入门，2-初级，3-中级，4-高级，5-专家
     */
    @Column(name = "difficulty_level")
    private Integer difficultyLevel;
    
    /**
     * 重要程度：1-一般，2-重要，3-非常重要
     */
    @Column(name = "importance")
    private Integer importance;
    
    /**
     * 标签（JSON格式）
     */
    @Column(name = "tags", length = 500)
    private String tags;
    
    /**
     * 关键词（用于搜索）
     */
    @Column(name = "keywords", length = 500)
    private String keywords;
    
    /**
     * 相关链接（JSON格式）
     */
    @Column(name = "related_links", columnDefinition = "TEXT")
    private String relatedLinks;
    
    /**
     * 排序权重
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    /**
     * 状态：1-发布，2-草稿，0-禁用
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 浏览次数
     */
    @Column(name = "view_count")
    private Integer viewCount;
    
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
