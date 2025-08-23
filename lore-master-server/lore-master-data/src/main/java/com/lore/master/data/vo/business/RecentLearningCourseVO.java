package com.lore.master.data.vo.business;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 最近学习课程响应VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentLearningCourseVO {

    /**
     * 课程ID
     */
    private Long id;

    /**
     * 课程编码
     */
    private String courseCode;

    /**
     * 课程标题
     */
    private String title;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 课程作者
     */
    private String author;

    /**
     * 课程类型：NORMAL-普通课程，COLLECTION-合集
     */
    private String courseType;

    /**
     * 课程封面图片URL
     */
    private String coverImageUrl;

    /**
     * 课程难度等级
     */
    private String difficultyLevel;

    /**
     * 预估学习时长（分钟）
     */
    private Integer estimatedMinutes;

    /**
     * 观看次数
     */
    private Long viewCount;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 收藏数
     */
    private Long collectCount;

    /**
     * 课程状态：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档
     */
    private String status;

    /**
     * 学习时长（秒）
     */
    private Integer learningDuration;

    /**
     * 学习进度百分比（0-100）
     */
    private Integer progressPercent;

    /**
     * 是否完成：1-是，0-否
     */
    private Integer isCompleted;

    /**
     * 最近学习日期
     */
    private LocalDate lastLearningDate;

    /**
     * 学习记录创建时间
     */
    private LocalDateTime learningRecordCreatedTime;

    /**
     * 学习记录更新时间
     */
    private LocalDateTime learningRecordUpdatedTime;
}
