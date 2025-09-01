package com.lore.master.data.dto.admin;

import lombok.Data;

/**
 * 轮播图查询请求
 */
@Data
public class CarouselBannerQueryRequest {
    private int page = 0;
    private int size = 10;
    private String status;
    private String title;
}
