package com.lore.master.data.vo.business;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课程响应VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseVO {

    /**
     * 课程ID
     */
    private Long id;

    /**
     * 课程编码
     */
    private String courseCode;

    /**
     * 课程标题
     */
    private String title;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 课程作者
     */
    private String author;

    /**
     * 课程类型：NORMAL-普通课程，COLLECTION-合集
     */
    private String courseType;

    /**
     * 内容类型：ARTICLE-图文，VIDEO-视频
     */
    private String contentType;

    /**
     * 课程难度等级
     */
    private String difficultyLevel;

    /**
     * 包含的难度等级（合集专用）
     */
    private String difficultyLevels;

    /**
     * 难度等级列表（解析后的）
     */
    private List<String> difficultyLevelList;

    /**
     * 父课程ID
     */
    private Long parentCourseId;

    /**
     * 父课程标题
     */
    private String parentCourseTitle;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 课程状态
     */
    private String status;

    /**
     * 关联的技能目标编码列表
     */
    private List<String> skillTargetCodes;

    /**
     * 课程标签
     */
    private String tags;

    /**
     * 标签列表（解析后的）
     */
    private List<String> tagList;

    /**
     * 课程时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 格式化的时长（如：1小时30分钟）
     */
    private String formattedDuration;

    /**
     * 观看次数
     */
    private Long viewCount;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 收藏数
     */
    private Long collectCount;

    /**
     * 内容链接
     */
    private String contentUrl;

    /**
     * 封面图片链接
     */
    private String coverImageUrl;

    /**
     * 缩略图链接
     */
    private String thumbnailUrl;

    /**
     * 课程Markdown格式内容详情
     */
    private String contentMarkdown;

    /**
     * 课程HTML格式内容（由Markdown转换而来）
     */
    private String contentHtml;

    /**
     * 内容最后更新时间
     */
    private LocalDateTime contentUpdatedTime;

    /**
     * 内容中引用的文件ID列表（JSON格式）
     */
    private String contentFileIds;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 子课程数量（仅合集有效）
     */
    private Integer subCourseCount;

    /**
     * 子课程列表（仅合集有效，可选）
     */
    private List<CourseVO> subCourses;

    /**
     * 是否已收藏（需要用户登录状态）
     */
    private Boolean isCollected;

    /**
     * 是否已点赞（需要用户登录状态）
     */
    private Boolean isLiked;

    /**
     * 学习进度百分比（需要用户登录状态）
     */
    private Integer progressPercent;
}
