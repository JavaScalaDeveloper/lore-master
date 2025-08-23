package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.vo.consumer.CarouselBannerVO;
import com.lore.master.data.vo.consumer.CarouselBannerDetailVO;
import com.lore.master.service.consumer.CarouselBannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/carousel")
@RequiredArgsConstructor
public class CarouselBannerController {

    private final CarouselBannerService carouselBannerService;

    /**
     * 获取轮播图列表
     */
    @GetMapping("/list")
    public Result<List<CarouselBannerVO>> getCarouselBanners() {
        try {
            log.info("获取轮播图列表");
            List<CarouselBannerVO> banners = carouselBannerService.getActiveCarouselBanners();
            return Result.success("获取轮播图列表成功", banners);
        } catch (Exception e) {
            log.error("获取轮播图列表失败", e);
            return Result.error("获取轮播图列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取轮播图详情
     */
    @GetMapping("/detail")
    public Result<CarouselBannerDetailVO> getCarouselBannerDetail(@RequestParam("bannerId") String bannerId) {
        try {
            log.info("获取轮播图详情，bannerId: {}", bannerId);
            CarouselBannerDetailVO detail = carouselBannerService.getCarouselBannerDetail(bannerId);
            return Result.success("获取轮播图详情成功", detail);
        } catch (IllegalArgumentException e) {
            log.warn("获取轮播图详情失败，参数错误: {}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (RuntimeException e) {
            log.warn("获取轮播图详情失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取轮播图详情失败，bannerId: {}", bannerId, e);
            return Result.error("获取轮播图详情失败: " + e.getMessage());
        }
    }

    /**
     * 增加轮播图查看次数
     */
    @PostMapping("/view")
    public Result<String> incrementViewCount(@RequestParam("bannerId") String bannerId) {
        try {
            log.info("增加轮播图查看次数，bannerId: {}", bannerId);
            carouselBannerService.incrementViewCount(bannerId);
            return Result.success("查看次数更新成功");
        } catch (Exception e) {
            log.error("更新查看次数失败，bannerId: {}", bannerId, e);
            return Result.error("更新查看次数失败: " + e.getMessage());
        }
    }
}
