package com.lore.master.data.dto.admin;

import lombok.Data;

/**
 * 知识图谱查询请求DTO
 */
@Data
public class AdminKnowledgeMapQueryRequest {
    
    /**
     * 根节点编码
     */
    private String rootCode;
    
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
     * 难度等级
     */
    private String difficultyLevel;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 20;

    @Override
    public String toString() {
        return "AdminKnowledgeMapQueryRequest{" +
                "rootCode='" + rootCode + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", levelDepth=" + levelDepth +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}
