package com.lore.master.data.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户学习目标响应VO
 */
@Data
public class UserLearningGoalResponse {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 技能编码
     */
    private String skillCode;

    /**
     * 技能名称
     */
    private String skillName;

    /**
     * 技能路径
     */
    private String skillPath;

    /**
     * 目标等级
     */
    private String targetLevel;

    /**
     * 当前进度百分比（0-100）
     */
    private Double currentProgress;

    /**
     * 开始学习日期
     */
    private LocalDate startDate;

    /**
     * 目标完成日期
     */
    private LocalDate targetDate;

    /**
     * 状态：active-进行中，completed-已完成，paused-暂停，abandoned-放弃
     */
    private String status;

    /**
     * 优先级：1-低，2-中，3-高
     */
    private Integer priority;

    /**
     * 学习笔记
     */
    private String notes;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}