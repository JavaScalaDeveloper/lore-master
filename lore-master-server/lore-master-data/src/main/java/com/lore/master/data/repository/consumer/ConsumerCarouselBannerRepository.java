package com.lore.master.data.repository.consumer;

import com.lore.master.data.entity.consumer.ConsumerCarouselBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 消费端轮播图Repository
 */
@Repository
public interface ConsumerCarouselBannerRepository extends JpaRepository<ConsumerCarouselBanner, Long> {

    /**
     * 根据bannerId查找轮播图
     */
    Optional<ConsumerCarouselBanner> findByBannerId(String bannerId);

    /**
     * 查找启用状态的轮播图，按排序顺序排列
     */
    List<ConsumerCarouselBanner> findByStatusOrderBySortOrderAsc(String status);

    /**
     * 查找启用状态的轮播图，按排序顺序排列（限制数量）
     */
    @Query("SELECT b FROM ConsumerCarouselBanner b WHERE b.status = :status ORDER BY b.sortOrder ASC")
    List<ConsumerCarouselBanner> findActiveCarouselBanners(@Param("status") String status);

    /**
     * 增加查看次数
     */
    @Modifying
    @Query("UPDATE ConsumerCarouselBanner b SET b.viewCount = b.viewCount + 1 WHERE b.bannerId = :bannerId")
    int incrementViewCount(@Param("bannerId") String bannerId);

    /**
     * 检查bannerId是否存在
     */
    boolean existsByBannerId(String bannerId);

    /**
     * 获取最大排序顺序
     */
    @Query("SELECT COALESCE(MAX(b.sortOrder), 0) FROM ConsumerCarouselBanner b")
    Integer getMaxSortOrder();
}
