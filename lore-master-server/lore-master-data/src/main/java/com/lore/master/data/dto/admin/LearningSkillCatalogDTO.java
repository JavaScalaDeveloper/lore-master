package com.lore.master.data.dto.admin;

import com.lore.master.data.validation.CreateGroup;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习技能目录DTO
 */
@Data
@Accessors(chain = true)
public class LearningSkillCatalogDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 技能编码，唯一标识
     */
    @NotBlank(message = "技能编码不能为空")
    @Size(max = 50, message = "技能编码长度不能超过50个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "技能编码只能包含字母、数字和下划线")
    private String skillCode;

    /**
     * 技能名称
     */
    @NotBlank(message = "技能名称不能为空")
    @Size(max = 100, message = "技能名称长度不能超过100个字符")
    private String skillName;

    /**
     * 技能路径，用/分隔
     * 创建时必填，更新时可选（由后端自动生成）
     */
    @NotBlank(message = "技能路径不能为空", groups = CreateGroup.class)
    @Size(max = 200, message = "技能路径长度不能超过200个字符")
    private String skillPath;

    /**
     * 层级：1-一级分类，2-二级分类，3-三级目标
     */
    @NotNull(message = "层级不能为空")
    @Min(value = 1, message = "层级最小值为1")
    @Max(value = 3, message = "层级最大值为3")
    private Integer level;

    /**
     * 父级技能编码
     */
    @Size(max = 50, message = "父级技能编码长度不能超过50个字符")
    private String parentCode;

    /**
     * 图标标识
     */
    @Size(max = 50, message = "图标标识长度不能超过50个字符")
    private String icon;

    /**
     * 技能描述
     */
    @Size(max = 1000, message = "技能描述长度不能超过1000个字符")
    private String description;

    /**
     * 排序序号
     */
    @Min(value = 0, message = "排序序号不能为负数")
    private Integer sortOrder = 0;

    /**
     * 是否启用
     */
    private Boolean isActive = true;

    /**
     * 难度等级：beginner/intermediate/advanced
     */
    @Pattern(regexp = "^(beginner|intermediate|advanced)$", message = "难度等级只能是beginner、intermediate或advanced")
    private String difficultyLevel;

    /**
     * 预估学习时长（小时）
     */
    @Min(value = 0, message = "预估学习时长不能为负数")
    private Integer estimatedHours;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 标签，JSON格式存储
     */
    private String tags;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 子技能列表（用于树形结构展示）
     */
    private List<LearningSkillCatalogDTO> children;

    /**
     * 父级技能名称（用于展示）
     */
    private String parentName;

    /**
     * 子技能数量
     */
    private Long childrenCount;

    /**
     * 是否有子技能
     */
    private Boolean hasChildren;

    /**
     * 完整路径名称（用于展示，如：外语 > 英语 > CET-4）
     */
    private String fullPathName;
}
