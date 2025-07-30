package com.lore.master.data.vo.admin;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习技能目录VO
 */
@Data
@Accessors(chain = true)
public class LearningSkillCatalogVO {

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
     * 层级：1-一级分类，2-二级分类，3-三级目标
     */
    private Integer level;

    /**
     * 父级技能编码
     */
    private String parentCode;

    /**
     * 父级技能名称
     */
    private String parentName;

    /**
     * 图标标识
     */
    private String icon;

    /**
     * 技能描述
     */
    private String description;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 难度等级
     */
    private String difficultyLevel;

    /**
     * 难度等级显示名称
     */
    private String difficultyLevelName;

    /**
     * 预估学习时长（小时）
     */
    private Integer estimatedHours;

    /**
     * 标签列表
     */
    private List<String> tagList;

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
     * 子技能列表（树形结构）
     */
    private List<LearningSkillCatalogVO> children;

    /**
     * 子技能数量
     */
    private Long childrenCount;

    /**
     * 是否有子技能
     */
    private Boolean hasChildren;

    /**
     * 完整路径名称（如：外语 > 英语 > CET-4）
     */
    private String fullPathName;

    /**
     * 层级显示名称
     */
    private String levelName;

    /**
     * 状态显示名称
     */
    private String statusName;
}
