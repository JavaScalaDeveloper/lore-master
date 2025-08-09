package com.lore.master.data.vo.business;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程分页响应VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoursePageVO {

    /**
     * 课程列表
     */
    private List<CourseVO> courses;

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
    private CourseStatisticsVO statistics;

    /**
     * 课程统计信息
     */
    @Data
    @Builder
    public static class CourseStatisticsVO {
        
        /**
         * 总课程数
         */
        private Long totalCourses;

        /**
         * 普通课程数
         */
        private Long normalCourses;

        /**
         * 合集数
         */
        private Long collections;

        /**
         * 各难度等级课程数
         */
        private DifficultyStatistics difficultyStats;

        /**
         * 难度等级统计
         */
        @Data
        @Builder
        public static class DifficultyStatistics {
            private Long l1Count;
            private Long l2Count;
            private Long l3Count;
            private Long l4Count;
            private Long l5Count;
        }
    }
}
