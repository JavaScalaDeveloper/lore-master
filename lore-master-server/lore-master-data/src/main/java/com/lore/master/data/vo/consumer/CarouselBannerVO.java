package com.lore.master.data.vo.consumer;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 轮播图展示VO
 */
@Data
public class CarouselBannerVO {

    /**
     * 轮播图唯一标识
     */
    private String bannerId;

    /**
     * 轮播图标题
     */
    private String title;

    /**
     * 副标题/简介
     */
    private String subtitle;

    /**
     * 封面图片URL
     */
    private String coverImageUrl;

    /**
     * 跳转链接(可选)
     */
    private String jumpUrl;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 查看次数
     */
    private Long viewCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
