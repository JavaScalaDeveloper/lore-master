package com.lore.master.web.admin.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.entity.CareerTarget;
import com.lore.master.service.admin.CareerTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 职业目标管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/career-targets")
@RequiredArgsConstructor
public class CareerTargetController {
    
    private final CareerTargetService careerTargetService;
    
    /**
     * 分页查询职业目标
     */
    @PostMapping("/page")
    public Result<Page<CareerTarget>> getCareerTargetPage(@RequestBody Map<String, Object> params) {
        try {
            // 分页参数
            int current = (int) params.getOrDefault("current", 1);
            int size = (int) params.getOrDefault("size", 10);
            String sortField = (String) params.getOrDefault("sortField", "sortOrder");
            String sortOrder = (String) params.getOrDefault("sortOrder", "asc");
            
            // 创建排序对象
            Sort sort = Sort.by(
                "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortField
            );
            Pageable pageable = PageRequest.of(current - 1, size, sort);
            
            // 查询数据
            Page<CareerTarget> result = careerTargetService.getCareerTargetPage(pageable, params);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询职业目标失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有启用的职业目标
     */
    @GetMapping("/active")
    public Result<List<CareerTarget>> getAllActiveCareerTargets() {
        try {
            List<CareerTarget> result = careerTargetService.getAllActive();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取启用职业目标失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID查询职业目标
     */
    @GetMapping("/{id}")
    public Result<CareerTarget> getCareerTargetById(@PathVariable Long id) {
        try {
            CareerTarget careerTarget = careerTargetService.getById(id);
            return Result.success(careerTarget);
        } catch (Exception e) {
            log.error("查询职业目标失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据编码查询职业目标
     */
    @GetMapping("/code/{code}")
    public Result<CareerTarget> getCareerTargetByCode(@PathVariable String code) {
        try {
            CareerTarget careerTarget = careerTargetService.getByCode(code);
            return Result.success(careerTarget);
        } catch (Exception e) {
            log.error("根据编码查询职业目标失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建职业目标
     */
    @PostMapping
    public Result<Boolean> createCareerTarget(@Valid @RequestBody CareerTarget careerTarget) {
        try {
            boolean success = careerTargetService.createCareerTarget(careerTarget);
            return success ? Result.success(true) : Result.error("创建职业目标失败");
        } catch (Exception e) {
            log.error("创建职业目标失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新职业目标
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateCareerTarget(@PathVariable Long id, @Valid @RequestBody CareerTarget careerTarget) {
        try {
            careerTarget.setId(id);
            boolean success = careerTargetService.updateCareerTarget(careerTarget);
            return success ? Result.success(true) : Result.error("更新职业目标失败");
        } catch (Exception e) {
            log.error("更新职业目标失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除职业目标
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCareerTarget(@PathVariable Long id) {
        try {
            boolean success = careerTargetService.deleteCareerTarget(id);
            return success ? Result.success(true) : Result.error("删除职业目标失败");
        } catch (Exception e) {
            log.error("删除职业目标失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除职业目标
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteCareerTargets(@RequestBody Long[] ids) {
        try {
            boolean success = careerTargetService.batchDeleteCareerTargets(ids);
            return success ? Result.success(true) : Result.error("批量删除职业目标失败");
        } catch (Exception e) {
            log.error("批量删除职业目标失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新职业目标状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateCareerTargetStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer status = (Integer) params.get("status");
            boolean success = careerTargetService.updateStatus(id, status);
            return success ? Result.success(true) : Result.error("更新职业目标状态失败");
        } catch (Exception e) {
            log.error("更新职业目标状态失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据分类获取职业目标
     */
    @GetMapping("/category/{category}")
    public Result<List<CareerTarget>> getCareerTargetsByCategory(@PathVariable String category) {
        try {
            List<CareerTarget> result = careerTargetService.getByCategory(category);
            return Result.success(result);
        } catch (Exception e) {
            log.error("根据分类获取职业目标失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据难度等级获取职业目标
     */
    @GetMapping("/difficulty/{difficultyLevel}")
    public Result<List<CareerTarget>> getCareerTargetsByDifficulty(@PathVariable Integer difficultyLevel) {
        try {
            List<CareerTarget> result = careerTargetService.getByDifficultyLevel(difficultyLevel);
            return Result.success(result);
        } catch (Exception e) {
            log.error("根据难度等级获取职业目标失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取职业目标统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getCareerTargetStatistics() {
        try {
            Map<String, Object> statistics = careerTargetService.getCareerTargetStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取职业目标统计信息失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取职业目标分类分布
     */
    @GetMapping("/category-distribution")
    public Result<Map<String, Object>> getCategoryDistribution() {
        try {
            Map<String, Object> distribution = careerTargetService.getCategoryDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取职业目标分类分布失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取职业目标难度分布
     */
    @GetMapping("/difficulty-distribution")
    public Result<Map<String, Object>> getDifficultyDistribution() {
        try {
            Map<String, Object> distribution = careerTargetService.getDifficultyDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取职业目标难度分布失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 调整排序权重
     */
    @PutMapping("/{id}/sort-order")
    public Result<Boolean> updateSortOrder(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer sortOrder = (Integer) params.get("sortOrder");
            boolean success = careerTargetService.updateSortOrder(id, sortOrder);
            return success ? Result.success(true) : Result.error("调整排序失败");
        } catch (Exception e) {
            log.error("调整职业目标排序失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
