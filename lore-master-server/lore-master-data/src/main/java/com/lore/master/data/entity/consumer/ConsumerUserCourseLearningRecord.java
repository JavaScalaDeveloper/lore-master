package com.lore.master.data.entity.consumer;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户课程学习记录实体类
 * 对应 consumer_user_course_learning_records 表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "consumer_user_course_learning_records", schema = "lore_consumer")
public class ConsumerUserCourseLearningRecord {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    /**
     * 课程编码
     */
    @Column(name = "course_code", nullable = false, length = 64)
    private String courseCode;

    /**
     * 学习时长（秒）
     */
    @Column(name = "learning_duration")
    private Integer learningDuration = 0;

    /**
     * 学习进度百分比（0-100）
     */
    @Column(name = "progress_percent")
    private Integer progressPercent = 0;

    /**
     * 是否完成：1-是，0-否
     */
    @Column(name = "is_completed")
    private Integer isCompleted = 0;

    /**
     * 学习日期
     */
    @Column(name = "learning_date", nullable = false)
    private LocalDate learningDate;

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
