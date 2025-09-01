package com.lore.master.data.dto.admin;

import lombok.Data;

/**
 * 轮播图排序请求
 */
@Data
public class CarouselBannerSortRequest {
    private String bannerId;
    private Integer sortOrder;
}
