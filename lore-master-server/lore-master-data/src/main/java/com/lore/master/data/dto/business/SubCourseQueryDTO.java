package com.lore.master.data.dto.business;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 子课程查询请求DTO
 */
@Data
public class SubCourseQueryDTO {

    /**
     * 父课程ID
     */
    @NotNull(message = "父课程ID不能为空")
    @Min(value = 1, message = "父课程ID必须大于0")
    private Long parentCourseId;

    /**
     * 用户ID（可选）
     */
    private String userId;
}
