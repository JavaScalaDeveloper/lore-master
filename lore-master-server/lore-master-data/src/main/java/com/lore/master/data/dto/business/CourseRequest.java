package com.lore.master.data.dto.business;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 课程请求DTO
 */
@Data
public class CourseRequest {

    /**
     * 课程ID（编辑时必填）
     */
    private Long id;

    /**
     * 课程编码，唯一标识
     */
    @NotBlank(message = "课程编码不能为空")
    @Size(max = 64, message = "课程编码长度不能超过64个字符")
    private String courseCode;

    /**
     * 课程标题
     */
    @NotBlank(message = "课程标题不能为空")
    @Size(max = 200, message = "课程标题长度不能超过200个字符")
    private String title;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 课程作者
     */
    @NotBlank(message = "课程作者不能为空")
    @Size(max = 100, message = "作者名称长度不能超过100个字符")
    private String author;

    /**
     * 课程类型：NORMAL-普通课程，COLLECTION-合集
     */
    @NotBlank(message = "课程类型不能为空")
    private String courseType;

    /**
     * 内容类型：ARTICLE-图文，VIDEO-视频（仅普通课程有效）
     */
    private String contentType;

    /**
     * 课程难度等级（普通课程必填，合集自动计算）
     */
    private String difficultyLevel;

    /**
     * 包含的难度等级（合集专用，如：L1,L2,L3）
     */
    private String difficultyLevels;

    /**
     * 父课程ID（普通课程属于某个合集时填写）
     */
    private Long parentCourseId;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 课程状态：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档
     */
    @NotBlank(message = "课程状态不能为空")
    private String status;

    /**
     * 关联的技能目标编码列表（JSON格式存储）
     */
    private List<String> skillTargetCodes;

    /**
     * 课程标签（用逗号分隔）
     */
    private String tags;

    /**
     * 课程时长（分钟，视频课程专用）
     */
    private Integer durationMinutes;

    /**
     * 内容链接（图文链接或视频链接）
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
     * 内容中引用的文件ID列表（JSON格式）
     */
    private String contentFileIds;

    /**
     * 子课程ID列表（仅合集类型有效）
     */
    private List<Long> subCourseIds;

    @Override
    public String toString() {
        return "CourseRequest{" +
                "id=" + id +
                ", courseCode='" + courseCode + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", courseType='" + courseType + '\'' +
                ", status='" + status + '\'' +
                ", subCourseIds=" + subCourseIds +
                '}';
    }
}
