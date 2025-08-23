package com.lore.master.service.consumer;

import com.lore.master.data.vo.consumer.CarouselBannerVO;
import com.lore.master.data.vo.consumer.CarouselBannerDetailVO;

import java.util.List;

/**
 * 轮播图服务接口
 */
public interface CarouselBannerService {

    /**
     * 获取启用的轮播图列表
     */
    List<CarouselBannerVO> getActiveCarouselBanners();

    /**
     * 根据bannerId获取轮播图详情
     */
    CarouselBannerDetailVO getCarouselBannerDetail(String bannerId);

    /**
     * 增加轮播图查看次数
     */
    void incrementViewCount(String bannerId);
}
