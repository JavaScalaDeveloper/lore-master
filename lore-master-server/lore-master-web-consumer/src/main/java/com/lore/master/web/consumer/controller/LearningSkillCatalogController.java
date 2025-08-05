package com.lore.master.web.consumer.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.dto.admin.LearningSkillCatalogDTO;
import com.lore.master.data.dto.admin.LearningSkillCatalogQueryDTO;
import com.lore.master.data.vo.admin.LearningSkillCatalogVO;
import com.lore.master.service.admin.LearningSkillCatalogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习技能目录管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/consumer/skill-catalog")
@RequiredArgsConstructor
@Validated
public class LearningSkillCatalogController {

    private final LearningSkillCatalogService skillCatalogService;

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

}
