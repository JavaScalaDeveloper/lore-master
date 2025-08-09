package com.lore.master.data.dto.business;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

/**
 * 课程详情查询请求DTO
 */
@Data
public class CourseDetailQueryDTO {

    /**
     * 课程编码
     */
    @NotBlank(message = "课程编码不能为空")
    private String courseCode;

    /**
     * 课程ID
     */
    @Min(value = 1, message = "课程ID必须大于0")
    private Long courseId;

    /**
     * 用户ID（可选，用于获取用户相关状态）
     */
    private String userId;

    /**
     * 是否包含子课程（仅对合集有效）
     */
    private Boolean includeSubCourses = true;
}
