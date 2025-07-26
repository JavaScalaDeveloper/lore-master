package com.lore.master.webadmin.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.entity.LevelConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 等级配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/levels")
@RequiredArgsConstructor
public class LevelConfigController {
    
    /**
     * 分页查询等级配置
     */
    @PostMapping("/page")
    public Result<Page<LevelConfig>> getLevelConfigPage(@RequestBody Map<String, Object> params) {
        try {
            // 模拟分页数据
            List<LevelConfig> levelConfigs = createMockLevelConfigs();
            
            int current = (int) params.getOrDefault("current", 1);
            int size = (int) params.getOrDefault("size", 10);
            
            // 简单的分页模拟
            int start = (current - 1) * size;
            int end = Math.min(start + size, levelConfigs.size());
            List<LevelConfig> pageContent = levelConfigs.subList(start, end);
            
            // 创建模拟的Page对象
            Page<LevelConfig> page = new org.springframework.data.domain.PageImpl<>(
                pageContent, 
                PageRequest.of(current - 1, size), 
                levelConfigs.size()
            );
            
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页查询等级配置失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID查询等级配置
     */
    @GetMapping("/{id}")
    public Result<LevelConfig> getLevelConfigById(@PathVariable Long id) {
        try {
            LevelConfig levelConfig = createMockLevelConfig(id);
            return Result.success(levelConfig);
        } catch (Exception e) {
            log.error("查询等级配置失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建等级配置
     */
    @PostMapping
    public Result<Boolean> createLevelConfig(@Valid @RequestBody LevelConfig levelConfig) {
        try {
            log.info("创建等级配置: {}", levelConfig.getName());
            return Result.success(true);
        } catch (Exception e) {
            log.error("创建等级配置失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新等级配置
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateLevelConfig(@PathVariable Long id, @Valid @RequestBody LevelConfig levelConfig) {
        try {
            log.info("更新等级配置: id={}, name={}", id, levelConfig.getName());
            return Result.success(true);
        } catch (Exception e) {
            log.error("更新等级配置失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除等级配置
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteLevelConfig(@PathVariable Long id) {
        try {
            log.info("删除等级配置: id={}", id);
            return Result.success(true);
        } catch (Exception e) {
            log.error("删除等级配置失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除等级配置
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteLevelConfigs(@RequestBody Long[] ids) {
        try {
            log.info("批量删除等级配置: ids={}", (Object) ids);
            return Result.success(true);
        } catch (Exception e) {
            log.error("批量删除等级配置失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新等级配置状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateLevelConfigStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer status = (Integer) params.get("status");
            log.info("更新等级配置状态: id={}, status={}", id, status);
            return Result.success(true);
        } catch (Exception e) {
            log.error("更新等级配置状态失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取等级配置统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getLevelConfigStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", 10);
            statistics.put("activeCount", 8);
            statistics.put("inactiveCount", 2);
            statistics.put("maxLevel", 10);
            statistics.put("minLevel", 1);
            statistics.put("avgMinScore", 1250);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取等级配置统计信息失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建模拟等级配置数据
     */
    private List<LevelConfig> createMockLevelConfigs() {
        List<LevelConfig> levelConfigs = new ArrayList<>();
        
        String[] levelNames = {
            "新手", "初学者", "入门", "进阶", "中级",
            "高级", "专家", "大师", "宗师", "传奇"
        };
        
        String[] colors = {
            "#52c41a", "#1890ff", "#722ed1", "#eb2f96", "#fa541c",
            "#faad14", "#13c2c2", "#f5222d", "#fa8c16", "#a0d911"
        };
        
        for (int i = 0; i < levelNames.length; i++) {
            LevelConfig levelConfig = new LevelConfig();
            levelConfig.setId((long) (i + 1));
            levelConfig.setLevel(i + 1);
            levelConfig.setName(levelNames[i]);
            levelConfig.setDescription("等级" + (i + 1) + "：" + levelNames[i] + "级别");
            levelConfig.setIcon("icon-level-" + (i + 1));
            levelConfig.setColor(colors[i]);
            levelConfig.setMinScore(i * 1000);
            levelConfig.setMaxScore(i == levelNames.length - 1 ? -1 : (i + 1) * 1000 - 1);
            levelConfig.setRewardScore(100 + i * 50);
            levelConfig.setPrivileges("[\"特权1\", \"特权2\"]");
            levelConfig.setStatus(1);
            levelConfig.setCreatorId(1L);
            levelConfig.setCreatorName("系统管理员");
            levelConfigs.add(levelConfig);
        }
        
        return levelConfigs;
    }
    
    /**
     * 创建单个模拟等级配置
     */
    private LevelConfig createMockLevelConfig(Long id) {
        LevelConfig levelConfig = new LevelConfig();
        levelConfig.setId(id);
        levelConfig.setLevel(1);
        levelConfig.setName("新手");
        levelConfig.setDescription("等级1：新手级别");
        levelConfig.setIcon("icon-level-1");
        levelConfig.setColor("#52c41a");
        levelConfig.setMinScore(0);
        levelConfig.setMaxScore(999);
        levelConfig.setRewardScore(100);
        levelConfig.setPrivileges("[\"基础功能\", \"学习记录\"]");
        levelConfig.setStatus(1);
        levelConfig.setCreatorId(1L);
        levelConfig.setCreatorName("系统管理员");
        return levelConfig;
    }
}
