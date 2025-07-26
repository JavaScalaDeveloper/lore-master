package com.lore.master.web.admin.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.entity.KnowledgePoint;
import com.lore.master.service.admin.KnowledgePointService;
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
 * 知识点管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/knowledge-points")
@RequiredArgsConstructor
public class KnowledgePointController {
    
    private final KnowledgePointService knowledgePointService;
    
    /**
     * 分页查询知识点
     */
    @PostMapping("/page")
    public Result<Page<KnowledgePoint>> getKnowledgePointPage(@RequestBody Map<String, Object> params) {
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
            Page<KnowledgePoint> result = knowledgePointService.getKnowledgePointPage(pageable, params);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID查询知识点
     */
    @GetMapping("/{id}")
    public Result<KnowledgePoint> getKnowledgePointById(@PathVariable Long id) {
        try {
            KnowledgePoint knowledgePoint = knowledgePointService.getById(id);
            return Result.success(knowledgePoint);
        } catch (Exception e) {
            log.error("查询知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据编码查询知识点
     */
    @GetMapping("/code/{code}")
    public Result<KnowledgePoint> getKnowledgePointByCode(@PathVariable String code) {
        try {
            KnowledgePoint knowledgePoint = knowledgePointService.getByCode(code);
            return Result.success(knowledgePoint);
        } catch (Exception e) {
            log.error("根据编码查询知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据学科ID获取知识点
     */
    @GetMapping("/subject/{subjectId}")
    public Result<List<KnowledgePoint>> getKnowledgePointsBySubject(@PathVariable Long subjectId) {
        try {
            List<KnowledgePoint> result = knowledgePointService.getBySubjectId(subjectId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("根据学科获取知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据父级ID获取子知识点
     */
    @GetMapping("/parent/{parentId}")
    public Result<List<KnowledgePoint>> getKnowledgePointsByParent(@PathVariable Long parentId) {
        try {
            List<KnowledgePoint> result = knowledgePointService.getByParentId(parentId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("根据父级获取知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建知识点
     */
    @PostMapping
    public Result<Boolean> createKnowledgePoint(@Valid @RequestBody KnowledgePoint knowledgePoint) {
        try {
            boolean success = knowledgePointService.createKnowledgePoint(knowledgePoint);
            return success ? Result.success(true) : Result.error("创建知识点失败");
        } catch (Exception e) {
            log.error("创建知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新知识点
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateKnowledgePoint(@PathVariable Long id, @Valid @RequestBody KnowledgePoint knowledgePoint) {
        try {
            knowledgePoint.setId(id);
            boolean success = knowledgePointService.updateKnowledgePoint(knowledgePoint);
            return success ? Result.success(true) : Result.error("更新知识点失败");
        } catch (Exception e) {
            log.error("更新知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除知识点
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteKnowledgePoint(@PathVariable Long id) {
        try {
            boolean success = knowledgePointService.deleteKnowledgePoint(id);
            return success ? Result.success(true) : Result.error("删除知识点失败");
        } catch (Exception e) {
            log.error("删除知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除知识点
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteKnowledgePoints(@RequestBody Long[] ids) {
        try {
            boolean success = knowledgePointService.batchDeleteKnowledgePoints(ids);
            return success ? Result.success(true) : Result.error("批量删除知识点失败");
        } catch (Exception e) {
            log.error("批量删除知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新知识点状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateKnowledgePointStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer status = (Integer) params.get("status");
            boolean success = knowledgePointService.updateStatus(id, status);
            return success ? Result.success(true) : Result.error("更新知识点状态失败");
        } catch (Exception e) {
            log.error("更新知识点状态失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据标签搜索知识点
     */
    @GetMapping("/search/tag/{tag}")
    public Result<List<KnowledgePoint>> searchKnowledgePointsByTag(@PathVariable String tag) {
        try {
            List<KnowledgePoint> result = knowledgePointService.searchByTag(tag);
            return Result.success(result);
        } catch (Exception e) {
            log.error("根据标签搜索知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 全文搜索知识点
     */
    @GetMapping("/search/keyword")
    public Result<Page<KnowledgePoint>> searchKnowledgePointsByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<KnowledgePoint> result = knowledgePointService.searchByKeyword(keyword, pageable);
            return Result.success(result);
        } catch (Exception e) {
            log.error("全文搜索知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取热门知识点
     */
    @GetMapping("/popular")
    public Result<Page<KnowledgePoint>> getPopularKnowledgePoints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<KnowledgePoint> result = knowledgePointService.getPopularKnowledgePoints(pageable);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取热门知识点失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 增加浏览次数
     */
    @PutMapping("/{id}/view")
    public Result<Boolean> incrementViewCount(@PathVariable Long id) {
        try {
            boolean success = knowledgePointService.incrementViewCount(id);
            return success ? Result.success(true) : Result.error("增加浏览次数失败");
        } catch (Exception e) {
            log.error("增加知识点浏览次数失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取知识点统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getKnowledgePointStatistics() {
        try {
            Map<String, Object> statistics = knowledgePointService.getKnowledgePointStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取知识点统计信息失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取知识点学科分布
     */
    @GetMapping("/subject-distribution")
    public Result<Map<String, Object>> getSubjectDistribution() {
        try {
            Map<String, Object> distribution = knowledgePointService.getSubjectDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取知识点学科分布失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取知识点难度分布
     */
    @GetMapping("/difficulty-distribution")
    public Result<Map<String, Object>> getDifficultyDistribution() {
        try {
            Map<String, Object> distribution = knowledgePointService.getDifficultyDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取知识点难度分布失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取知识点重要程度分布
     */
    @GetMapping("/importance-distribution")
    public Result<Map<String, Object>> getImportanceDistribution() {
        try {
            Map<String, Object> distribution = knowledgePointService.getImportanceDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取知识点重要程度分布失败: {}", e.getMessage(), e);
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
            boolean success = knowledgePointService.updateSortOrder(id, sortOrder);
            return success ? Result.success(true) : Result.error("调整排序失败");
        } catch (Exception e) {
            log.error("调整知识点排序失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
