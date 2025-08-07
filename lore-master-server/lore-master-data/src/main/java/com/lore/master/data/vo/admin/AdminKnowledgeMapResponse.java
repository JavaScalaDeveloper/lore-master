package com.lore.master.data.vo.admin;

import lombok.Data;

/**
 * 知识图谱节点响应DTO
 */
@Data
public class AdminKnowledgeMapResponse {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 节点编码
     */
    private String nodeCode;
    
    /**
     * 节点名称
     */
    private String nodeName;
    
    /**
     * 节点类型
     */
    private String nodeType;
    
    /**
     * 父节点编码
     */
    private String parentCode;
    
    /**
     * 根节点编码
     */
    private String rootCode;
    
    /**
     * 节点路径
     */
    private String nodePath;
    
    /**
     * 层级深度
     */
    private Integer levelDepth;
    
    /**
     * 层级类型
     */
    private String levelType;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
    
    /**
     * 技能目录编码
     */
    private String skillCatalogCode;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 难度等级
     */
    private String difficultyLevel;
    
    /**
     * 预估学习时长（小时）
     */
    private Integer estimatedHours;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 创建时间
     */
    private String createdTime;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 更新时间
     */
    private String updatedTime;
}
