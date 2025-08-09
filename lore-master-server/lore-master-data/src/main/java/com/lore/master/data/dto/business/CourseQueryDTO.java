package com.lore.master.data.dto.business;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 课程查询请求DTO
 */
@Data
public class CourseQueryDTO {

    /**
     * 课程类型：NORMAL-普通课程，COLLECTION-合集
     */
    @Pattern(regexp = "^(NORMAL|COLLECTION)$", message = "课程类型只能是NORMAL或COLLECTION")
    private String courseType;

    /**
     * 课程状态：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档
     */
    @Pattern(regexp = "^(DRAFT|PUBLISHED|ARCHIVED)$", message = "课程状态只能是DRAFT、PUBLISHED或ARCHIVED")
    private String status;

    /**
     * 难度等级：L1-L5
     */
    @Pattern(regexp = "^(L[1-5])$", message = "难度等级只能是L1-L5")
    private String difficultyLevel;

    /**
     * 课程作者
     */
    private String author;

    /**
     * 知识点路径（支持模糊匹配）
     */
    private String knowledgeNodePath;

    /**
     * 搜索关键词（搜索标题、描述、标签、知识点名称路径）
     */
    private String keyword;

    /**
     * 父课程ID（查询某个合集下的子课程）
     */
    private Long parentCourseId;

    /**
     * 排序方式：publish_time_desc-发布时间倒序，view_count_desc-观看次数倒序，
     * created_time_desc-创建时间倒序，sort_order_asc-排序顺序正序
     */
    @Pattern(regexp = "^(publish_time_desc|view_count_desc|created_time_desc|sort_order_asc)$", 
             message = "排序方式不正确")
    private String sortBy = "publish_time_desc";

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
     * 是否只查询已发布的课程
     */
    private Boolean publishedOnly = true;

    /**
     * 是否包含子课程统计（仅对合集有效）
     */
    private Boolean includeSubCourseCount = false;
}
