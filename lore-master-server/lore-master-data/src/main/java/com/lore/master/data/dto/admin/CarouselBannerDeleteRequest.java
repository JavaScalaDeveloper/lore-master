package com.lore.master.data.dto.admin;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 轮播图删除请求
 */
@Data
public class CarouselBannerDeleteRequest {
    
    @NotBlank(message = "轮播图ID不能为空")
    private String bannerId;
}
