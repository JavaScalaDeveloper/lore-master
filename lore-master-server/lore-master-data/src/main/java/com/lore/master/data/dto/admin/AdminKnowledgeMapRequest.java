package com.lore.master.data.dto.admin;

import lombok.Data;

/**
 * 知识图谱节点请求DTO
 */
@Data
public class AdminKnowledgeMapRequest {
    
    /**
     * 节点编码
     */
    private String nodeCode;
    
    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点名称字符串（前端传递的显示字段）
     */
    private String nodeNameStr;

    /**
     * 节点类型：ROOT-根节点，LEVEL-层级节点，BRANCH-分支节点，LEAF-叶子节点
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
     * 难度等级：BEGINNER-初级，INTERMEDIATE-中级，ADVANCED-高级，EXPERT-专家
     */
    private String difficultyLevel;
    
    /**
     * 预估学习时长（小时）
     */
    private Integer estimatedHours;
    
    /**
     * 状态：ACTIVE-启用，INACTIVE-禁用
     */
    private String status;

    @Override
    public String toString() {
        return "AdminKnowledgeMapRequest{" +
                "nodeCode='" + nodeCode + '\'' +
                ", nodeName='" + nodeName + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", parentCode='" + parentCode + '\'' +
                ", rootCode='" + rootCode + '\'' +
                '}';
    }
}
