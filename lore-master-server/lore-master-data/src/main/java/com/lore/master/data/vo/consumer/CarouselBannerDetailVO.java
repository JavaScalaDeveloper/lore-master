package com.lore.master.data.vo.consumer;

import com.lore.master.data.entity.consumer.ConsumerCarouselBanner;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * 轮播图详情VO
 */
@Data
public class CarouselBannerDetailVO {

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
     * 详情内容(Markdown格式)
     */
    private String contentMarkdown;

    /**
     * 详情内容(HTML格式)
     */
    private String contentHtml;

    /**
     * 跳转链接(可选)
     */
    private String jumpUrl;

    /**
     * 查看次数
     */
    private Long viewCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 转换为详情VO对象
     */
    public static CarouselBannerDetailVO convertToDetailVO(ConsumerCarouselBanner banner) {
        CarouselBannerDetailVO vo = new CarouselBannerDetailVO();
        BeanUtils.copyProperties(banner, vo);
        return vo;
    }
}
