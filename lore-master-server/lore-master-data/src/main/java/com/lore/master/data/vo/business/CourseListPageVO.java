package com.lore.master.data.vo.business;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程列表分页响应VO（轻量级）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseListPageVO {

    /**
     * 课程列表
     */
    private List<CourseListVO> courses;

    /**
     * 当前页码（从0开始）
     */
    private Integer currentPage;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 总记录数
     */
    private Long totalElements;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否是第一页
     */
    private Boolean isFirst;

    /**
     * 是否是最后一页
     */
    private Boolean isLast;

    /**
     * 统计信息
     */
    private CoursePageVO.CourseStatisticsVO statistics;
}
