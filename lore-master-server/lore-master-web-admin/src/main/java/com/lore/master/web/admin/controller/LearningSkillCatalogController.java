package com.lore.master.web.admin.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.dto.admin.LearningSkillCatalogDTO;
import com.lore.master.data.dto.admin.LearningSkillCatalogQueryDTO;
import com.lore.master.data.validation.CreateGroup;
import com.lore.master.data.validation.UpdateGroup;
import com.lore.master.data.vo.admin.LearningSkillCatalogVO;
import com.lore.master.service.admin.LearningSkillCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 学习技能目录管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/skill-catalog")
@RequiredArgsConstructor
@Validated
public class LearningSkillCatalogController {

    private final LearningSkillCatalogService skillCatalogService;

    /**
     * 创建技能目录
     */
    @PostMapping("/create")
    public Result<LearningSkillCatalogVO> createSkillCatalog(@Validated(CreateGroup.class) @RequestBody LearningSkillCatalogDTO dto) {
        log.info("创建技能目录: {}", dto.getSkillCode());
        try {
            LearningSkillCatalogVO result = skillCatalogService.createSkillCatalog(dto);
            return Result.success("创建成功", result);
        } catch (Exception e) {
            log.error("创建技能目录失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新技能目录
     */
    @PutMapping("/update/{id}")
    public Result<LearningSkillCatalogVO> updateSkillCatalog(
            @PathVariable @NotNull Long id,
            @Validated(UpdateGroup.class) @RequestBody LearningSkillCatalogDTO dto) {
        log.info("更新技能目录: id={}, skillCode={}, skillPath={}", id, dto.getSkillCode(), dto.getSkillPath());
        try {
            LearningSkillCatalogVO result = skillCatalogService.updateSkillCatalog(id, dto);
            return Result.success("更新成功", result);
        } catch (Exception e) {
            log.error("更新技能目录失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除技能目录
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteSkillCatalog(@PathVariable @NotNull Long id) {
        log.info("删除技能目录: id={}", id);
        try {
            skillCatalogService.deleteSkillCatalog(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除技能目录失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除技能目录
     */
    @DeleteMapping("/batch-delete")
    public Result<String> batchDeleteSkillCatalog(@RequestBody @NotEmpty List<Long> ids) {
        log.info("批量删除技能目录: ids={}", ids);
        try {
            skillCatalogService.batchDeleteSkillCatalog(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除技能目录失败", e);
            return Result.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询技能目录
     */
    @GetMapping("/get/{id}")
    public Result<LearningSkillCatalogVO> getSkillCatalogById(@PathVariable @NotNull Long id) {
        try {
            LearningSkillCatalogVO result = skillCatalogService.getSkillCatalogById(id);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询技能目录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据技能编码查询技能目录
     */
    @GetMapping("/get-by-code/{skillCode}")
    public Result<LearningSkillCatalogVO> getSkillCatalogByCode(@PathVariable String skillCode) {
        try {
            LearningSkillCatalogVO result = skillCatalogService.getSkillCatalogByCode(skillCode);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询技能目录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询技能目录
     */
    @PostMapping("/page")
    public Result<Page<LearningSkillCatalogVO>> querySkillCatalogPage(@RequestBody LearningSkillCatalogQueryDTO queryDTO) {
        try {
            Page<LearningSkillCatalogVO> result = skillCatalogService.querySkillCatalogPage(queryDTO);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("分页查询技能目录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询技能目录列表
     */
    @PostMapping("/list")
    public Result<List<LearningSkillCatalogVO>> querySkillCatalogList(@RequestBody LearningSkillCatalogQueryDTO queryDTO) {
        try {
            List<LearningSkillCatalogVO> result = skillCatalogService.querySkillCatalogList(queryDTO);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询技能目录列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询技能目录树形结构
     */
    @PostMapping("/tree")
    public Result<List<LearningSkillCatalogVO>> querySkillCatalogTree(@RequestBody LearningSkillCatalogQueryDTO queryDTO) {
        try {
            List<LearningSkillCatalogVO> result = skillCatalogService.querySkillCatalogTree(queryDTO);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询技能目录树失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有一级分类
     */
    @GetMapping("/first-level")
    public Result<List<LearningSkillCatalogVO>> queryFirstLevelCategories() {
        try {
            List<LearningSkillCatalogVO> result = skillCatalogService.queryFirstLevelCategories();
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询一级分类失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定父级下的子分类
     */
    @GetMapping("/children/{parentCode}")
    public Result<List<LearningSkillCatalogVO>> queryChildrenByParentCode(@PathVariable String parentCode) {
        try {
            List<LearningSkillCatalogVO> result = skillCatalogService.queryChildrenByParentCode(parentCode);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询子分类失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定层级的所有技能
     */
    @GetMapping("/level/{level}")
    public Result<List<LearningSkillCatalogVO>> querySkillsByLevel(@PathVariable Integer level) {
        try {
            List<LearningSkillCatalogVO> result = skillCatalogService.querySkillsByLevel(level);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询指定层级技能失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用技能目录
     */
    @PutMapping("/toggle-status/{id}")
    public Result<String> toggleSkillCatalogStatus(
            @PathVariable @NotNull Long id,
            @RequestParam Boolean isActive) {
        log.info("切换技能目录状态: id={}, isActive={}", id, isActive);
        try {
            skillCatalogService.toggleSkillCatalogStatus(id, isActive);
            return Result.success("状态切换成功");
        } catch (Exception e) {
            log.error("切换技能目录状态失败", e);
            return Result.error("状态切换失败: " + e.getMessage());
        }
    }

    /**
     * 批量启用/禁用技能目录
     */
    @PutMapping("/batch-toggle-status")
    public Result<String> batchToggleSkillCatalogStatus(
            @RequestBody @NotEmpty List<Long> ids,
            @RequestParam Boolean isActive) {
        log.info("批量切换技能目录状态: ids={}, isActive={}", ids, isActive);
        try {
            skillCatalogService.batchToggleSkillCatalogStatus(ids, isActive);
            return Result.success("批量状态切换成功");
        } catch (Exception e) {
            log.error("批量切换技能目录状态失败", e);
            return Result.error("批量状态切换失败: " + e.getMessage());
        }
    }

    /**
     * 调整技能目录排序
     */
    @PutMapping("/adjust-sort/{id}")
    public Result<String> adjustSortOrder(
            @PathVariable @NotNull Long id,
            @RequestParam Integer newSortOrder) {
        log.info("调整技能目录排序: id={}, newSortOrder={}", id, newSortOrder);
        try {
            skillCatalogService.adjustSortOrder(id, newSortOrder);
            return Result.success("排序调整成功");
        } catch (Exception e) {
            log.error("调整技能目录排序失败", e);
            return Result.error("排序调整失败: " + e.getMessage());
        }
    }

    /**
     * 移动技能目录到新的父级
     */
    @PutMapping("/move/{id}")
    public Result<String> moveSkillCatalog(
            @PathVariable @NotNull Long id,
            @RequestParam String newParentCode) {
        log.info("移动技能目录: id={}, newParentCode={}", id, newParentCode);
        try {
            skillCatalogService.moveSkillCatalog(id, newParentCode);
            return Result.success("移动成功");
        } catch (Exception e) {
            log.error("移动技能目录失败", e);
            return Result.error("移动失败: " + e.getMessage());
        }
    }

    /**
     * 复制技能目录
     */
    @PostMapping("/copy/{id}")
    public Result<LearningSkillCatalogVO> copySkillCatalog(
            @PathVariable @NotNull Long id,
            @RequestParam String newSkillCode,
            @RequestParam String newSkillName) {
        log.info("复制技能目录: id={}, newSkillCode={}, newSkillName={}", id, newSkillCode, newSkillName);
        try {
            LearningSkillCatalogVO result = skillCatalogService.copySkillCatalog(id, newSkillCode, newSkillName);
            return Result.success("复制成功", result);
        } catch (Exception e) {
            log.error("复制技能目录失败", e);
            return Result.error("复制失败: " + e.getMessage());
        }
    }

    /**
     * 检查技能编码是否存在
     */
    @GetMapping("/exists-code/{skillCode}")
    public Result<Boolean> existsBySkillCode(@PathVariable String skillCode) {
        try {
            boolean exists = skillCatalogService.existsBySkillCode(skillCode);
            return Result.success("查询成功", exists);
        } catch (Exception e) {
            log.error("检查技能编码失败", e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取技能目录统计信息
     */
    @GetMapping("/statistics")
    public Result<Object> getSkillCatalogStatistics() {
        try {
            Object result = skillCatalogService.getSkillCatalogStatistics();
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 导入技能目录数据
     */
    @PostMapping("/import")
    public Result<String> importSkillCatalogData(@RequestBody @NotEmpty List<LearningSkillCatalogDTO> dataList) {
        log.info("导入技能目录数据: {} 条", dataList.size());
        try {
            skillCatalogService.importSkillCatalogData(dataList);
            return Result.success("导入成功");
        } catch (Exception e) {
            log.error("导入技能目录数据失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导出技能目录数据
     */
    @PostMapping("/export")
    public Result<List<LearningSkillCatalogVO>> exportSkillCatalogData(@RequestBody LearningSkillCatalogQueryDTO queryDTO) {
        try {
            List<LearningSkillCatalogVO> result = skillCatalogService.exportSkillCatalogData(queryDTO);
            return Result.success("导出成功", result);
        } catch (Exception e) {
            log.error("导出技能目录数据失败", e);
            return Result.error("导出失败: " + e.getMessage());
        }
    }
}
