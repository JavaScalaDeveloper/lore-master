package com.lore.master.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户学习目标请求DTO
 */
@Data
public class UserLearningGoalRequest {

    /**
     * 技能编码
     */
    @NotBlank(message = "技能编码不能为空")
    private String skillCode;

    /**
     * 技能名称
     */
    @NotBlank(message = "技能名称不能为空")
    private String skillName;

    /**
     * 技能路径
     */
    @NotBlank(message = "技能路径不能为空")
    private String skillPath;

    /**
     * 目标等级
     */
    private String targetLevel;

    /**
     * 开始学习日期
     */
    private LocalDate startDate;

    /**
     * 目标完成日期
     */
    private LocalDate targetDate;

    /**
     * 优先级：1-低，2-中，3-高
     */
    private Integer priority;

    /**
     * 学习笔记
     */
    private String notes;

    /**
     * 目标ID（用于更新操作）
     */
    private Long goalId;
}