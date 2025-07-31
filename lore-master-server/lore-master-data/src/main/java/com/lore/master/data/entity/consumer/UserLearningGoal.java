package com.lore.master.data.entity.consumer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户学习目标实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "user_learning_goals")
public class UserLearningGoal {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    /**
     * 技能编码
     */
    @Column(name = "skill_code", nullable = false, length = 50)
    private String skillCode;

    /**
     * 技能名称（冗余存储）
     */
    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    /**
     * 技能路径（冗余存储）
     */
    @Column(name = "skill_path", nullable = false, length = 200)
    private String skillPath;

    /**
     * 目标等级
     */
    @Column(name = "target_level", length = 20)
    private String targetLevel;

    /**
     * 当前进度百分比（0-100）
     */
    @Column(name = "current_progress", precision = 5, scale = 2)
    private BigDecimal currentProgress;

    /**
     * 开始学习日期
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 目标完成日期
     */
    @Column(name = "target_date")
    private LocalDate targetDate;

    /**
     * 状态：active-进行中，completed-已完成，paused-暂停，abandoned-放弃
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 优先级：1-低，2-中，3-高
     */
    @Column(name = "priority")
    private Integer priority;

    /**
     * 学习笔记
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}