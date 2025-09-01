package com.lore.master.data.dto.admin;

import lombok.Data;

/**
 * 轮播图创建请求
 */
@Data
public class CarouselBannerRequest {
    private String title;
    private String subtitle;
    private String coverImageUrl;
    private String contentMarkdown;
    private String contentHtml;
    private String jumpUrl;
    private Integer sortOrder;
    private String status;
}
