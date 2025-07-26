package com.lore.master.webadmin.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.entity.Subject;
import com.lore.master.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 学科管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    /**
     * 分页查询学科
     */
    @PostMapping("/page")
    public Result<Page<Subject>> getSubjectPage(@RequestBody Map<String, Object> params) {
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
            Page<Subject> result = subjectService.getSubjectPage(pageable, params);

            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询学科失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID查询学科
     */
    @GetMapping("/{id}")
    public Result<Subject> getSubjectById(@PathVariable Long id) {
        try {
            Subject subject = subjectService.getById(id);
            return Result.success(subject);
        } catch (Exception e) {
            log.error("查询学科失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建学科
     */
    @PostMapping
    public Result<Boolean> createSubject(@Valid @RequestBody Subject subject) {
        try {
            boolean success = subjectService.createSubject(subject);
            return success ? Result.success(true) : Result.error("创建学科失败");
        } catch (Exception e) {
            log.error("创建学科失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新学科
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateSubject(@PathVariable Long id, @Valid @RequestBody Subject subject) {
        try {
            subject.setId(id);
            boolean success = subjectService.updateSubject(subject);
            return success ? Result.success(true) : Result.error("更新学科失败");
        } catch (Exception e) {
            log.error("更新学科失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除学科
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSubject(@PathVariable Long id) {
        try {
            boolean success = subjectService.deleteSubject(id);
            return success ? Result.success(true) : Result.error("删除学科失败");
        } catch (Exception e) {
            log.error("删除学科失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除学科
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteSubjects(@RequestBody Long[] ids) {
        try {
            boolean success = subjectService.batchDeleteSubjects(ids);
            return success ? Result.success(true) : Result.error("批量删除学科失败");
        } catch (Exception e) {
            log.error("批量删除学科失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新学科状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateSubjectStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer status = (Integer) params.get("status");
            boolean success = subjectService.updateStatus(id, status);
            return success ? Result.success(true) : Result.error("更新学科状态失败");
        } catch (Exception e) {
            log.error("更新学科状态失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取学科统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getSubjectStatistics() {
        try {
            Map<String, Object> statistics = subjectService.getSubjectStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取学科统计信息失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
