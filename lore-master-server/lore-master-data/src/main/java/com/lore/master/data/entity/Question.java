package com.lore.master.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 题目实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "questions")
public class Question {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 题目标题
     */
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    
    /**
     * 题目内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    /**
     * 题目类型：1-单选，2-多选，3-判断，4-填空，5-简答，6-编程
     */
    @Column(name = "type", nullable = false)
    private Integer type;
    
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
     * 关联知识点ID
     */
    @Column(name = "knowledge_point_id")
    private Long knowledgePointId;
    
    /**
     * 关联知识点名称
     */
    @Column(name = "knowledge_point_name", length = 200)
    private String knowledgePointName;
    
    /**
     * 难度等级：1-入门，2-初级，3-中级，4-高级，5-专家
     */
    @Column(name = "difficulty_level")
    private Integer difficultyLevel;
    
    /**
     * 题目选项（JSON格式）
     */
    @Column(name = "options", columnDefinition = "TEXT")
    private String options;
    
    /**
     * 正确答案
     */
    @Column(name = "correct_answer", length = 1000)
    private String correctAnswer;
    
    /**
     * 答案解析
     */
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;
    
    /**
     * 标签（JSON格式）
     */
    @Column(name = "tags", length = 500)
    private String tags;
    
    /**
     * 分值
     */
    @Column(name = "score")
    private Integer score;
    
    /**
     * 预计用时（秒）
     */
    @Column(name = "estimated_time")
    private Integer estimatedTime;
    
    /**
     * 使用次数
     */
    @Column(name = "usage_count")
    private Integer usageCount;
    
    /**
     * 正确率（百分比）
     */
    @Column(name = "accuracy_rate")
    private Double accuracyRate;
    
    /**
     * 状态：1-发布，2-草稿，0-禁用
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
