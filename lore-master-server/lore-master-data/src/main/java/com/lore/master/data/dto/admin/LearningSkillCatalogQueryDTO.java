package com.lore.master.data.dto.admin;

import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * 学习技能目录查询DTO
 */
@Data
@Accessors(chain = true)
public class LearningSkillCatalogQueryDTO {

    /**
     * 技能编码（精确匹配）
     */
    private String skillCode;

    /**
     * 技能名称（模糊匹配）
     */
    private String skillName;

    /**
     * 技能路径前缀（用于查询某个分类下的所有技能）
     */
    private String skillPathPrefix;

    /**
     * 层级：1-一级分类，2-二级分类，3-三级目标
     */
    @Min(value = 1, message = "层级最小值为1")
    @Max(value = 3, message = "层级最大值为3")
    private Integer level;

    /**
     * 父级技能编码
     */
    private String parentCode;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 难度等级
     */
    private String difficultyLevel;

    /**
     * 预估学习时长范围 - 最小值
     */
    @Min(value = 0, message = "最小学习时长不能为负数")
    private Integer minEstimatedHours;

    /**
     * 预估学习时长范围 - 最大值
     */
    @Min(value = 0, message = "最大学习时长不能为负数")
    private Integer maxEstimatedHours;

    /**
     * 标签（模糊匹配）
     */
    private String tag;

    /**
     * 标签列表（包含任一标签）
     */
    private List<String> tags;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 排序字段
     */
    private String sortField = "sortOrder";

    /**
     * 排序方向：asc/desc
     */
    private String sortDirection = "asc";

    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码最小值为1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小最小值为1")
    @Max(value = 100, message = "每页大小最大值为100")
    private Integer pageSize = 20;

    /**
     * 是否返回树形结构
     */
    private Boolean treeStructure = false;

    /**
     * 是否包含子技能数量
     */
    private Boolean includeChildrenCount = false;

    /**
     * 是否只查询叶子节点（没有子技能的节点）
     */
    private Boolean leafNodesOnly = false;

    /**
     * 是否只查询根节点（一级分类）
     */
    private Boolean rootNodesOnly = false;
}
