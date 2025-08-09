package com.lore.master.data.dto.business;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

/**
 * 知识点路径查询请求DTO
 */
@Data
public class KnowledgePathQueryDTO {

    /**
     * 知识点路径
     */
    @NotBlank(message = "知识点路径不能为空")
    private String knowledgeNodePath;

    /**
     * 页码（从0开始）
     */
    @Min(value = 0, message = "页码不能小于0")
    private Integer page = 0;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小不能小于1")
    private Integer size = 20;

    /**
     * 用户ID（可选）
     */
    private String userId;
}
