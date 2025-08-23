package com.lore.master.service.consumer.impl;

import com.lore.master.data.entity.consumer.ConsumerCarouselBanner;
import com.lore.master.data.repository.consumer.ConsumerCarouselBannerRepository;
import com.lore.master.data.vo.consumer.CarouselBannerVO;
import com.lore.master.data.vo.consumer.CarouselBannerDetailVO;
import com.lore.master.service.business.MarkdownProcessingService;
import com.lore.master.service.consumer.CarouselBannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 轮播图服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CarouselBannerServiceImpl implements CarouselBannerService {

    private final ConsumerCarouselBannerRepository carouselBannerRepository;
    private final MarkdownProcessingService markdownProcessingService;

    @Override
    public List<CarouselBannerVO> getActiveCarouselBanners() {
        log.info("获取启用的轮播图列表");
        
        List<ConsumerCarouselBanner> banners = carouselBannerRepository
                .findByStatusOrderBySortOrderAsc(ConsumerCarouselBanner.Status.ACTIVE.getCode());
        
        return banners.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public CarouselBannerDetailVO getCarouselBannerDetail(String bannerId) {
        log.info("获取轮播图详情，bannerId: {}", bannerId);
        
        if (!StringUtils.hasText(bannerId)) {
            throw new IllegalArgumentException("轮播图ID不能为空");
        }
        
        Optional<ConsumerCarouselBanner> bannerOpt = carouselBannerRepository.findByBannerId(bannerId);
        if (bannerOpt.isEmpty()) {
            throw new RuntimeException("轮播图不存在: " + bannerId);
        }
        
        ConsumerCarouselBanner banner = bannerOpt.get();
        
        // 检查状态
        if (!ConsumerCarouselBanner.Status.ACTIVE.getCode().equals(banner.getStatus())) {
            throw new RuntimeException("轮播图已禁用: " + bannerId);
        }
        
        return convertToDetailVO(banner);
    }

    @Override
    @Transactional
    public void incrementViewCount(String bannerId) {
        log.info("增加轮播图查看次数，bannerId: {}", bannerId);
        
        if (!StringUtils.hasText(bannerId)) {
            return;
        }
        
        try {
            int updated = carouselBannerRepository.incrementViewCount(bannerId);
            if (updated > 0) {
                log.info("轮播图查看次数更新成功，bannerId: {}", bannerId);
            } else {
                log.warn("轮播图查看次数更新失败，可能不存在，bannerId: {}", bannerId);
            }
        } catch (Exception e) {
            log.error("更新轮播图查看次数失败，bannerId: {}", bannerId, e);
        }
    }

    /**
     * 转换为VO
     */
    private CarouselBannerVO convertToVO(ConsumerCarouselBanner banner) {
        CarouselBannerVO vo = new CarouselBannerVO();
        BeanUtils.copyProperties(banner, vo);
        return vo;
    }

    /**
     * 转换为详情VO
     */
    private CarouselBannerDetailVO convertToDetailVO(ConsumerCarouselBanner banner) {
        CarouselBannerDetailVO vo = new CarouselBannerDetailVO();
        BeanUtils.copyProperties(banner, vo);

        // 如果HTML内容为空但Markdown内容存在，则转换Markdown为HTML
        if (!StringUtils.hasText(vo.getContentHtml()) && StringUtils.hasText(vo.getContentMarkdown())) {
            String htmlContent = markdownProcessingService.convertMarkdownToHtml(vo.getContentMarkdown());
            vo.setContentHtml(htmlContent);
        }

        return vo;
    }
}
