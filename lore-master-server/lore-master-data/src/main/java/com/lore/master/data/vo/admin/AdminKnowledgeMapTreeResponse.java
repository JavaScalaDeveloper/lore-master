package com.lore.master.data.vo.admin;

import lombok.Data;

import java.util.List;

/**
 * 技能树结构响应DTO
 */
@Data
public class AdminKnowledgeMapTreeResponse {
    
    /**
     * 根节点编码
     */
    private String rootCode;
    
    /**
     * 根节点名称
     */
    private String rootName;
    
    /**
     * 子节点列表
     */
    private List<TreeNode> children;

    /**
     * 树节点DTO
     */
    @Data
    public static class TreeNode {
        
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
         * 难度等级
         */
        private String difficultyLevel;
        
        /**
         * 预估学习时长（小时）
         */
        private Integer estimatedHours;
        
        /**
         * 描述
         */
        private String description;
        
        /**
         * 子节点列表
         */
        private List<TreeNode> children;
    }
}
