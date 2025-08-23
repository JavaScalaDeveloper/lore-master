package com.lore.master.data.entity.consumer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 消费端轮播图实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "consumer_carousel_banner")
@EntityListeners(AuditingEntityListener.class)
public class ConsumerCarouselBanner {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 轮播图唯一标识
     */
    @Column(name = "banner_id", nullable = false, unique = true, length = 32)
    private String bannerId;

    /**
     * 轮播图标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 副标题/简介
     */
    @Column(name = "subtitle", length = 500)
    private String subtitle;

    /**
     * 封面图片URL
     */
    @Column(name = "cover_image_url", nullable = false, length = 1000)
    private String coverImageUrl;

    /**
     * 详情内容(Markdown格式)
     */
    @Lob
    @Column(name = "content_markdown")
    private String contentMarkdown;

    /**
     * 详情内容(HTML格式，由Markdown转换)
     */
    @Lob
    @Column(name = "content_html")
    private String contentHtml;

    /**
     * 跳转链接(可选)
     */
    @Column(name = "jump_url", length = 1000)
    private String jumpUrl;

    /**
     * 排序顺序，数字越小越靠前
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 状态：ACTIVE-启用，INACTIVE-禁用
     */
    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    /**
     * 查看次数
     */
    @Column(name = "view_count")
    private Long viewCount = 0L;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 轮播图状态枚举
     */
    public enum Status {
        ACTIVE("ACTIVE", "启用"),
        INACTIVE("INACTIVE", "禁用");

        private final String code;
        private final String description;

        Status(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
