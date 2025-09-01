package com.lore.master.data.dto.admin;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 轮播图更新请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CarouselBannerUpdateRequest extends CarouselBannerRequest {
    private String bannerId;
}
