package com.lore.master.web.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/api/admin")
public class TestController {
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("message", "Lore Master Admin API is running");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Test API works!");
        result.put("data", "Hello from Lore Master Admin");
        return result;
    }
}
