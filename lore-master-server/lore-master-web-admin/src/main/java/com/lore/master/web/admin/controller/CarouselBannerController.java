package com.lore.master.web.admin.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.dto.admin.CarouselBannerQueryRequest;
import com.lore.master.data.dto.admin.CarouselBannerRequest;
import com.lore.master.data.dto.admin.CarouselBannerUpdateRequest;
import com.lore.master.data.dto.admin.CarouselBannerSortRequest;
import com.lore.master.data.dto.admin.CarouselBannerDetailRequest;
import com.lore.master.data.dto.admin.CarouselBannerDeleteRequest;
import com.lore.master.data.dto.admin.CarouselBannerStatusRequest;
import com.lore.master.data.entity.consumer.ConsumerCarouselBanner;
import com.lore.master.data.repository.consumer.ConsumerCarouselBannerRepository;
import com.lore.master.data.vo.consumer.CarouselBannerVO;
import com.lore.master.data.vo.consumer.CarouselBannerDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 管理端轮播图控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/carousel")
@RequiredArgsConstructor
@Validated
public class CarouselBannerController {

    private final ConsumerCarouselBannerRepository carouselBannerRepository;

    /**
     * 分页查询轮播图列表
     */
    @PostMapping("/list")
    public Result<Page<CarouselBannerVO>> getCarouselBannerList(
            @RequestBody CarouselBannerQueryRequest request) {
        try {
            Pageable pageable = PageRequest.of(
                    request.getPage(),
                    request.getSize(),
                    Sort.by(Sort.Direction.ASC, "sortOrder")
                            .and(Sort.by(Sort.Direction.DESC, "createdTime"))
            );

            Page<ConsumerCarouselBanner> bannerPage = carouselBannerRepository.findAll(pageable);

            Page<CarouselBannerVO> voPage = bannerPage.map(CarouselBannerVO::convertToVO);

            return Result.success(voPage);
        } catch (Exception e) {
            log.error("查询轮播图列表失败", e);
            return Result.error("查询轮播图列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取轮播图详情
     */
    @PostMapping("/detail")
    public Result<CarouselBannerDetailVO> getCarouselBannerDetail(@Valid @RequestBody CarouselBannerDetailRequest request) {
        try {
            Optional<ConsumerCarouselBanner> bannerOpt = carouselBannerRepository.findByBannerId(request.getBannerId());
            if (bannerOpt.isEmpty()) {
                return Result.error("轮播图不存在");
            }

            CarouselBannerDetailVO detailVO = CarouselBannerDetailVO.convertToDetailVO(bannerOpt.get());
            return Result.success(detailVO);
        } catch (Exception e) {
            log.error("获取轮播图详情失败", e);
            return Result.error("获取轮播图详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建轮播图
     */
    @PostMapping("/create")
    public Result<String> createCarouselBanner(@Valid @RequestBody CarouselBannerRequest request) {
        try {
            ConsumerCarouselBanner banner = new ConsumerCarouselBanner();
            banner.setBannerId(UUID.randomUUID().toString().replace("-", ""));
            banner.setTitle(request.getTitle());
            banner.setSubtitle(request.getSubtitle());
            banner.setCoverImageUrl(request.getCoverImageUrl());
            banner.setContentMarkdown(request.getContentMarkdown());
            banner.setContentHtml(request.getContentHtml());
            banner.setJumpUrl(request.getJumpUrl());
            banner.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
            banner.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
            banner.setCreatedBy("admin"); // TODO: 从当前登录用户获取
            banner.setUpdatedBy("admin");

            carouselBannerRepository.save(banner);

            return Result.success("创建成功");
        } catch (Exception e) {
            log.error("创建轮播图失败", e);
            return Result.error("创建轮播图失败: " + e.getMessage());
        }
    }

    /**
     * 更新轮播图
     */
    @PostMapping("/update")
    public Result<String> updateCarouselBanner(@Valid @RequestBody CarouselBannerUpdateRequest request) {
        try {
            Optional<ConsumerCarouselBanner> bannerOpt = carouselBannerRepository.findByBannerId(request.getBannerId());
            if (bannerOpt.isEmpty()) {
                return Result.error("轮播图不存在");
            }

            ConsumerCarouselBanner banner = bannerOpt.get();
            banner.setTitle(request.getTitle());
            banner.setSubtitle(request.getSubtitle());
            banner.setCoverImageUrl(request.getCoverImageUrl());
            banner.setContentMarkdown(request.getContentMarkdown());
            banner.setContentHtml(request.getContentHtml());
            banner.setJumpUrl(request.getJumpUrl());
            banner.setSortOrder(request.getSortOrder());
            banner.setStatus(request.getStatus());
            banner.setUpdatedBy("admin"); // TODO: 从当前登录用户获取
            banner.setUpdatedTime(LocalDateTime.now());

            carouselBannerRepository.save(banner);

            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新轮播图失败", e);
            return Result.error("更新轮播图失败: " + e.getMessage());
        }
    }

    /**
     * 删除轮播图
     */
    @PostMapping("/delete")
    public Result<String> deleteCarouselBanner(@Valid @RequestBody CarouselBannerDeleteRequest request) {
        try {
            Optional<ConsumerCarouselBanner> bannerOpt = carouselBannerRepository.findByBannerId(request.getBannerId());
            if (bannerOpt.isEmpty()) {
                return Result.error("轮播图不存在");
            }

            carouselBannerRepository.delete(bannerOpt.get());

            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除轮播图失败", e);
            return Result.error("删除轮播图失败: " + e.getMessage());
        }
    }

    /**
     * 更新轮播图状态
     */
    @PostMapping("/updateStatus")
    public Result<String> updateCarouselBannerStatus(@Valid @RequestBody CarouselBannerStatusRequest request) {
        try {
            Optional<ConsumerCarouselBanner> bannerOpt = carouselBannerRepository.findByBannerId(request.getBannerId());
            if (bannerOpt.isEmpty()) {
                return Result.error("轮播图不存在");
            }

            ConsumerCarouselBanner banner = bannerOpt.get();
            banner.setStatus(request.getStatus());
            banner.setUpdatedBy("admin"); // TODO: 从当前登录用户获取
            banner.setUpdatedTime(LocalDateTime.now());

            carouselBannerRepository.save(banner);

            return Result.success("状态更新成功");
        } catch (Exception e) {
            log.error("更新轮播图状态失败", e);
            return Result.error("更新轮播图状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新排序
     */
    @PostMapping("/updateSort")
    public Result<String> updateCarouselBannerSort(@RequestBody List<CarouselBannerSortRequest> requests) {
        try {
            for (CarouselBannerSortRequest request : requests) {
                Optional<ConsumerCarouselBanner> bannerOpt = carouselBannerRepository.findByBannerId(request.getBannerId());
                if (bannerOpt.isPresent()) {
                    ConsumerCarouselBanner banner = bannerOpt.get();
                    banner.setSortOrder(request.getSortOrder());
                    banner.setUpdatedBy("admin"); // TODO: 从当前登录用户获取
                    banner.setUpdatedTime(LocalDateTime.now());
                    carouselBannerRepository.save(banner);
                }
            }

            return Result.success("排序更新成功");
        } catch (Exception e) {
            log.error("更新轮播图排序失败", e);
            return Result.error("更新轮播图排序失败: " + e.getMessage());
        }
    }


}
