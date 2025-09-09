package com.lore.master.data.entity.business;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 课程实体类
 * 对应 business_course 表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "business_course")
public class BusinessCourse {

    /**
     * 课程ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 课程编码，唯一标识
     */
    @Column(name = "course_code", nullable = false, unique = true, length = 64)
    private String courseCode;

    /**
     * 课程标题
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * 课程描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 课程作者
     */
    @Column(name = "author", nullable = false, length = 100)
    private String author;

    /**
     * 课程类型：NORMAL-普通课程，COLLECTION-合集
     */
    @Column(name = "course_type", nullable = false, length = 20)
    private String courseType;

    /**
     * 内容类型：ARTICLE-图文，VIDEO-视频（仅普通课程有效）
     */
    @Column(name = "content_type", length = 20)
    private String contentType;

    /**
     * 课程难度等级（普通课程必填，合集自动计算）
     */
    @Column(name = "difficulty_level", length = 10)
    private String difficultyLevel;

    /**
     * 包含的难度等级（合集专用，如：L1,L2,L3）
     */
    @Column(name = "difficulty_levels", length = 50)
    private String difficultyLevels;

    /**
     * 父课程ID（普通课程属于某个合集时填写）
     */
    @Column(name = "parent_course_id")
    private Long parentCourseId;

    /**
     * 排序顺序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 课程状态：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 关联的技能目标编码列表（JSON格式存储）
     */
    @Column(name = "skill_target_codes", columnDefinition = "TEXT")
    private String skillTargetCodes;

    /**
     * 课程标签（用逗号分隔）
     */
    @Column(name = "tags", length = 500)
    private String tags;

    /**
     * 课程时长（分钟，视频课程专用）
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * 观看次数
     */
    @Column(name = "view_count")
    private Long viewCount = 0L;

    /**
     * 点赞数
     */
    @Column(name = "like_count")
    private Long likeCount = 0L;

    /**
     * 收藏数
     */
    @Column(name = "collect_count")
    private Long collectCount = 0L;

    /**
     * 内容链接（图文链接或视频链接）
     */
    @Column(name = "content_url", length = 500)
    private String contentUrl;

    /**
     * 封面图片链接
     */
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    /**
     * 缩略图链接
     */
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    /**
     * 课程Markdown格式内容详情
     */
    @Column(name = "content_markdown", columnDefinition = "LONGTEXT")
    private String contentMarkdown;

    /**
     * 课程HTML格式内容（由Markdown转换而来）
     */
    @Column(name = "content_html", columnDefinition = "LONGTEXT")
    private String contentHtml;

    /**
     * 内容最后更新时间
     */
    @Column(name = "content_updated_time")
    private LocalDateTime contentUpdatedTime;

    /**
     * 内容中引用的文件ID列表（JSON格式）
     */
    @Column(name = "content_file_ids", columnDefinition = "TEXT")
    private String contentFileIds;

    /**
     * 发布时间
     */
    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdTime == null) {
            createdTime = now;
        }
        if (updatedTime == null) {
            updatedTime = now;
        }
        if (status == null) {
            status = "DRAFT";
        }
        if (courseType == null) {
            courseType = "NORMAL";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    // 课程类型常量
    public static final String COURSE_TYPE_NORMAL = "NORMAL";
    public static final String COURSE_TYPE_COLLECTION = "COLLECTION";

    // 内容类型常量
    public static final String CONTENT_TYPE_ARTICLE = "ARTICLE";
    public static final String CONTENT_TYPE_VIDEO = "VIDEO";

    // 状态常量
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    // 难度等级常量
    public static final String DIFFICULTY_L1 = "L1";
    public static final String DIFFICULTY_L2 = "L2";
    public static final String DIFFICULTY_L3 = "L3";
    public static final String DIFFICULTY_L4 = "L4";
    public static final String DIFFICULTY_L5 = "L5";
}
