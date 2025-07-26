package com.lore.master.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求DTO
 */
@Data
public class UserRegisterRequest {
    
    /**
     * 注册类型：username、email、phone
     */
    @NotBlank(message = "注册类型不能为空")
    @Pattern(regexp = "^(username|email|phone)$", message = "注册类型只能是username、email或phone")
    private String registerType;
    
    /**
     * 注册标识（用户名、邮箱、手机号）
     */
    @NotBlank(message = "注册标识不能为空")
    @Size(max = 255, message = "注册标识长度不能超过255个字符")
    private String registerKey;
    
    /**
     * 密码（用户名注册时必填，前端传输时已加密）
     */
    @Size(max = 500, message = "密码格式错误")
    private String password;
    
    /**
     * 验证码（邮箱、手机注册时必填）
     */
    @Size(min = 4, max = 6, message = "验证码长度必须在4-6个字符之间")
    private String verifyCode;
    
    /**
     * 昵称
     */
    @Size(max = 100, message = "昵称长度不能超过100个字符")
    private String nickname;
    
    /**
     * 性别：1男 2女 0未知
     */
    @Pattern(regexp = "^[012]$", message = "性别只能是0、1或2")
    private String gender;
    
    /**
     * 邀请码（可选）
     */
    @Size(max = 32, message = "邀请码长度不能超过32个字符")
    private String inviteCode;
    
    /**
     * 设备类型
     */
    private String deviceType;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 客户端IP
     */
    private String clientIp;
    
    /**
     * 用户代理
     */
    private String userAgent;
}
