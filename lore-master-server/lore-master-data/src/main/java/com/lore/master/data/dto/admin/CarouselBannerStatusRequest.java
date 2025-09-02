package com.lore.master.data.dto.admin;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 轮播图状态更新请求
 */
@Data
public class CarouselBannerStatusRequest {
    
    @NotBlank(message = "轮播图ID不能为空")
    private String bannerId;
    
    @NotBlank(message = "状态不能为空")
    private String status;
}
