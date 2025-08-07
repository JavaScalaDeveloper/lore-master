package com.lore.master.service.admin.impl;

import com.alibaba.fastjson2.JSON;
import com.lore.master.data.dto.admin.LearningSkillCatalogDTO;
import com.lore.master.data.dto.admin.LearningSkillCatalogQueryDTO;
import com.lore.master.data.entity.admin.LearningSkillCatalog;
import com.lore.master.data.repository.admin.LearningSkillCatalogRepository;
import com.lore.master.data.vo.admin.LearningSkillCatalogVO;
import com.lore.master.service.admin.LearningSkillCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习技能目录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningSkillCatalogServiceImpl implements LearningSkillCatalogService {

    private final LearningSkillCatalogRepository skillCatalogRepository;

    @Override
    @Transactional
    public LearningSkillCatalogVO createSkillCatalog(LearningSkillCatalogDTO dto) {
        log.info("创建技能目录: {}", dto.getSkillCode());
        
        // 验证数据
        validateSkillCatalog(dto);
        
        // 检查技能编码是否已存在
        if (existsBySkillCode(dto.getSkillCode())) {
            throw new RuntimeException("技能编码已存在: " + dto.getSkillCode());
        }
        
        // 生成技能路径
        String skillPath = generateSkillPath(dto.getParentCode(), dto.getSkillCode());
        dto.setSkillPath(skillPath);
        
        // 获取排序序号
        if (dto.getSortOrder() == null) {
            dto.setSortOrder(getNextSortOrder(dto.getParentCode(), dto.getLevel()));
        }
        
        // 转换为实体
        LearningSkillCatalog entity = convertToEntity(dto);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        
        // 保存
        entity = skillCatalogRepository.save(entity);
        
        return convertToVO(entity);
    }

    @Override
    @Transactional
    public LearningSkillCatalogVO updateSkillCatalog(Long id, LearningSkillCatalogDTO dto) {
        log.info("更新技能目录: id={}, skillCode={}", id, dto.getSkillCode());
        
        // 查询原记录
        LearningSkillCatalog entity = skillCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("技能目录不存在: " + id));
        
        // 验证数据
        validateSkillCatalog(dto);
        
        // 检查技能编码是否已存在（排除自己）
        if (!entity.getSkillCode().equals(dto.getSkillCode()) && existsBySkillCode(dto.getSkillCode())) {
            throw new RuntimeException("技能编码已存在: " + dto.getSkillCode());
        }
        
        // 保存原有的skillPath，避免被null值覆盖
        String originalSkillPath = entity.getSkillPath();
        String originalParentCode = entity.getParentCode();
        String originalSkillCode = entity.getSkillCode();

        // 更新字段（排除skillPath，因为它需要特殊处理）
        BeanUtils.copyProperties(dto, entity, "id", "createdTime", "createdBy", "skillPath");
        entity.setUpdatedTime(LocalDateTime.now());

        // 如果父级或编码发生变化，重新生成路径；否则保持原路径
        if (!Objects.equals(originalParentCode, dto.getParentCode()) ||
            !Objects.equals(originalSkillCode, dto.getSkillCode())) {
            String skillPath = generateSkillPath(dto.getParentCode(), dto.getSkillCode());
            entity.setSkillPath(skillPath);
            log.info("技能路径已更新: {} -> {}", originalSkillPath, skillPath);
        } else {
            // 保持原有路径
            entity.setSkillPath(originalSkillPath);
        }
        
        // 处理标签
        if (!CollectionUtils.isEmpty(dto.getTagList())) {
            entity.setTags(JSON.toJSONString(dto.getTagList()));
        }
        
        // 保存
        entity = skillCatalogRepository.save(entity);
        
        return convertToVO(entity);
    }

    @Override
    @Transactional
    public void deleteSkillCatalog(Long id) {
        log.info("删除技能目录: id={}", id);
        
        LearningSkillCatalog entity = skillCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("技能目录不存在: " + id));
        
        // 检查是否有子技能
        long childrenCount = skillCatalogRepository.countByParentCodeAndIsActive(entity.getSkillCode(), true);
        if (childrenCount > 0) {
            throw new RuntimeException("该技能目录下还有子技能，无法删除");
        }
        
        skillCatalogRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDeleteSkillCatalog(List<Long> ids) {
        log.info("批量删除技能目录: ids={}", ids);
        
        for (Long id : ids) {
            deleteSkillCatalog(id);
        }
    }

    @Override
    public LearningSkillCatalogVO getSkillCatalogById(Long id) {
        LearningSkillCatalog entity = skillCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("技能目录不存在: " + id));
        
        return convertToVO(entity);
    }

    @Override
    public LearningSkillCatalogVO getSkillCatalogByCode(String skillCode) {
        LearningSkillCatalog entity = skillCatalogRepository.findBySkillCode(skillCode)
                .orElseThrow(() -> new RuntimeException("技能目录不存在: " + skillCode));
        
        return convertToVO(entity);
    }

    @Override
    public Page<LearningSkillCatalogVO> querySkillCatalogPage(LearningSkillCatalogQueryDTO queryDTO) {
        Pageable pageable = createPageable(queryDTO);
        
        // 这里简化处理，实际应该根据查询条件构建动态查询
        Page<LearningSkillCatalog> entityPage = skillCatalogRepository.findByIsActiveOrderByLevelAscSortOrderAsc(
                queryDTO.getIsActive() != null ? queryDTO.getIsActive() : true, pageable);
        
        return entityPage.map(this::convertToVO);
    }

    @Override
    public List<LearningSkillCatalogVO> querySkillCatalogList(LearningSkillCatalogQueryDTO queryDTO) {
        List<LearningSkillCatalog> entities = buildQueryResult(queryDTO);
        return entities.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<LearningSkillCatalogVO> querySkillCatalogTree(LearningSkillCatalogQueryDTO queryDTO) {
        List<LearningSkillCatalogVO> allSkills = querySkillCatalogList(queryDTO);
        return buildTree(allSkills);
    }

    @Override
    public List<LearningSkillCatalogVO> queryFirstLevelCategories() {
        List<LearningSkillCatalog> entities = skillCatalogRepository.findAllFirstLevelCategories(true);
        return entities.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<LearningSkillCatalogVO> queryChildrenByParentCode(String parentCode) {
        List<LearningSkillCatalog> entities = skillCatalogRepository.findByParentCodeAndIsActiveOrderBySortOrder(parentCode, true);
        return entities.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<LearningSkillCatalogVO> querySkillsByLevel(Integer level) {
        List<LearningSkillCatalog> entities = skillCatalogRepository.findByLevelAndIsActiveOrderBySortOrder(level, true);
        return entities.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<LearningSkillCatalogVO> querySkillsByPathPrefix(String pathPrefix) {
        List<LearningSkillCatalog> entities = skillCatalogRepository.findBySkillPathStartingWithAndIsActiveOrderByLevelAscSortOrderAsc(pathPrefix, true);
        return entities.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleSkillCatalogStatus(Long id, Boolean isActive) {
        LearningSkillCatalog entity = skillCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("技能目录不存在: " + id));
        
        entity.setIsActive(isActive);
        entity.setUpdatedTime(LocalDateTime.now());
        skillCatalogRepository.save(entity);
    }

    @Override
    @Transactional
    public void batchToggleSkillCatalogStatus(List<Long> ids, Boolean isActive) {
        for (Long id : ids) {
            toggleSkillCatalogStatus(id, isActive);
        }
    }

    @Override
    @Transactional
    public void adjustSortOrder(Long id, Integer newSortOrder) {
        LearningSkillCatalog entity = skillCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("技能目录不存在: " + id));
        
        entity.setSortOrder(newSortOrder);
        entity.setUpdatedTime(LocalDateTime.now());
        skillCatalogRepository.save(entity);
    }

    @Override
    @Transactional
    public void moveSkillCatalog(Long id, String newParentCode) {
        LearningSkillCatalog entity = skillCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("技能目录不存在: " + id));
        
        entity.setParentCode(newParentCode);
        entity.setSkillPath(generateSkillPath(newParentCode, entity.getSkillCode()));
        entity.setUpdatedTime(LocalDateTime.now());
        skillCatalogRepository.save(entity);
    }

    @Override
    @Transactional
    public LearningSkillCatalogVO copySkillCatalog(Long id, String newSkillCode, String newSkillName) {
        LearningSkillCatalog original = skillCatalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("技能目录不存在: " + id));
        
        LearningSkillCatalog copy = new LearningSkillCatalog();
        BeanUtils.copyProperties(original, copy, "id", "skillCode", "skillName", "skillPath", "createdTime", "updatedTime");
        
        copy.setSkillCode(newSkillCode);
        copy.setSkillName(newSkillName);
        copy.setSkillPath(generateSkillPath(copy.getParentCode(), newSkillCode));
        copy.setCreatedTime(LocalDateTime.now());
        copy.setUpdatedTime(LocalDateTime.now());
        
        copy = skillCatalogRepository.save(copy);
        return convertToVO(copy);
    }

    @Override
    public boolean existsBySkillCode(String skillCode) {
        return skillCatalogRepository.existsBySkillCode(skillCode);
    }

    @Override
    public boolean existsBySkillPath(String skillPath) {
        return skillCatalogRepository.existsBySkillPath(skillPath);
    }

    @Override
    public void validateSkillCatalog(LearningSkillCatalogDTO dto) {
        if (!StringUtils.hasText(dto.getSkillCode())) {
            throw new RuntimeException("技能编码不能为空");
        }
        if (!StringUtils.hasText(dto.getSkillName())) {
            throw new RuntimeException("技能名称不能为空");
        }
        if (dto.getLevel() == null || dto.getLevel() < 1 || dto.getLevel() > 3) {
            throw new RuntimeException("层级必须在1-3之间");
        }
        if (dto.getLevel() > 1 && !StringUtils.hasText(dto.getParentCode())) {
            throw new RuntimeException("非一级分类必须指定父级编码");
        }
    }

    @Override
    public String generateSkillPath(String parentCode, String skillCode) {
        if (!StringUtils.hasText(parentCode)) {
            return skillCode;
        }

        // 查询父级路径
        Optional<LearningSkillCatalog> parent = skillCatalogRepository.findBySkillCode(parentCode);
        if (parent.isPresent()) {
            return parent.get().getSkillPath() + "/" + skillCode;
        }

        return parentCode + "/" + skillCode;
    }

    @Override
    public Integer getNextSortOrder(String parentCode, Integer level) {
        if (StringUtils.hasText(parentCode)) {
            return skillCatalogRepository.getNextSortOrder(parentCode);
        } else {
            return skillCatalogRepository.getNextSortOrderByLevel(level, null);
        }
    }

    @Override
    @Transactional
    public void importSkillCatalogData(List<LearningSkillCatalogDTO> dataList) {
        log.info("导入技能目录数据: {} 条", dataList.size());

        for (LearningSkillCatalogDTO dto : dataList) {
            try {
                if (!existsBySkillCode(dto.getSkillCode())) {
                    createSkillCatalog(dto);
                }
            } catch (Exception e) {
                log.error("导入技能目录失败: {}, error: {}", dto.getSkillCode(), e.getMessage());
            }
        }
    }

    @Override
    public List<LearningSkillCatalogVO> exportSkillCatalogData(LearningSkillCatalogQueryDTO queryDTO) {
        return querySkillCatalogList(queryDTO);
    }

    @Override
    public Object getSkillCatalogStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 总数统计
        statistics.put("totalCount", skillCatalogRepository.count());
        statistics.put("activeCount", skillCatalogRepository.countByIsActive(true));

        // 按层级统计
        Map<String, Long> levelStats = new HashMap<>();
        levelStats.put("level1", skillCatalogRepository.countByLevelAndIsActive(1, true));
        levelStats.put("level2", skillCatalogRepository.countByLevelAndIsActive(2, true));
        levelStats.put("level3", skillCatalogRepository.countByLevelAndIsActive(3, true));
        statistics.put("levelStats", levelStats);

        return statistics;
    }

    /**
     * 构建查询结果
     */
    private List<LearningSkillCatalog> buildQueryResult(LearningSkillCatalogQueryDTO queryDTO) {
        // 简化实现，实际应该使用Specification或QueryDSL构建动态查询
        if (StringUtils.hasText(queryDTO.getSkillCode())) {
            return skillCatalogRepository.findBySkillCode(queryDTO.getSkillCode())
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }

        if (queryDTO.getLevel() != null && StringUtils.hasText(queryDTO.getParentCode())) {
            return skillCatalogRepository.findByLevelAndParentCodeAndIsActiveOrderBySortOrder(
                    queryDTO.getLevel(), queryDTO.getParentCode(),
                    queryDTO.getIsActive() != null ? queryDTO.getIsActive() : true);
        }

        if (queryDTO.getLevel() != null) {
            return skillCatalogRepository.findByLevelAndIsActiveOrderBySortOrder(
                    queryDTO.getLevel(), queryDTO.getIsActive() != null ? queryDTO.getIsActive() : true);
        }

        if (StringUtils.hasText(queryDTO.getParentCode())) {
            return skillCatalogRepository.findByParentCodeAndIsActiveOrderBySortOrder(
                    queryDTO.getParentCode(), queryDTO.getIsActive() != null ? queryDTO.getIsActive() : true);
        }

        if (StringUtils.hasText(queryDTO.getSkillPathPrefix())) {
            return skillCatalogRepository.findBySkillPathStartingWithAndIsActiveOrderByLevelAscSortOrderAsc(
                    queryDTO.getSkillPathPrefix(), queryDTO.getIsActive() != null ? queryDTO.getIsActive() : true);
        }

        // 默认查询所有启用的技能
        return skillCatalogRepository.findByIsActiveOrderByLevelAscSortOrderAsc(
                queryDTO.getIsActive() != null ? queryDTO.getIsActive() : true);
    }

    /**
     * 创建分页对象
     */
    private Pageable createPageable(LearningSkillCatalogQueryDTO queryDTO) {
        Sort sort = Sort.by(Sort.Direction.fromString(queryDTO.getSortDirection()), queryDTO.getSortField());
        return PageRequest.of(queryDTO.getPageNum() - 1, queryDTO.getPageSize(), sort);
    }

    /**
     * 构建树形结构
     */
    private List<LearningSkillCatalogVO> buildTree(List<LearningSkillCatalogVO> allSkills) {
        Map<String, LearningSkillCatalogVO> skillMap = allSkills.stream()
                .collect(Collectors.toMap(LearningSkillCatalogVO::getSkillCode, skill -> skill));

        List<LearningSkillCatalogVO> rootNodes = new ArrayList<>();

        for (LearningSkillCatalogVO skill : allSkills) {
            if (!StringUtils.hasText(skill.getParentCode())) {
                // 根节点
                rootNodes.add(skill);
            } else {
                // 子节点
                LearningSkillCatalogVO parent = skillMap.get(skill.getParentCode());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(skill);
                }
            }
        }

        return rootNodes;
    }

    /**
     * DTO转Entity
     */
    private LearningSkillCatalog convertToEntity(LearningSkillCatalogDTO dto) {
        LearningSkillCatalog entity = new LearningSkillCatalog();
        BeanUtils.copyProperties(dto, entity);

        // 处理标签
        if (!CollectionUtils.isEmpty(dto.getTagList())) {
            entity.setTags(JSON.toJSONString(dto.getTagList()));
        }

        return entity;
    }

    /**
     * Entity转VO
     */
    private LearningSkillCatalogVO convertToVO(LearningSkillCatalog entity) {
        LearningSkillCatalogVO vo = new LearningSkillCatalogVO();
        BeanUtils.copyProperties(entity, vo);

        // 处理标签
        if (StringUtils.hasText(entity.getTags())) {
            try {
                List<String> tagList = JSON.parseArray(entity.getTags(), String.class);
                vo.setTagList(tagList);
            } catch (Exception e) {
                log.warn("解析标签失败: {}", entity.getTags());
            }
        }

        // 设置显示名称
        vo.setLevelName(getLevelName(entity.getLevel()));
        vo.setStatusName(entity.getIsActive() ? "启用" : "禁用");
        vo.setDifficultyLevelName(getDifficultyLevelName(entity.getDifficultyLevel()));

        // 设置是否有子技能
        if (StringUtils.hasText(entity.getSkillCode())) {
            long childrenCount = skillCatalogRepository.countByParentCodeAndIsActive(entity.getSkillCode(), true);
            vo.setChildrenCount(childrenCount);
            vo.setHasChildren(childrenCount > 0);
        }

        // 设置父级名称
        if (StringUtils.hasText(entity.getParentCode())) {
            skillCatalogRepository.findBySkillCode(entity.getParentCode())
                    .ifPresent(parent -> vo.setParentName(parent.getSkillName()));
        }

        // 生成完整路径名称
        vo.setFullPathName(generateFullPathName(entity));

        return vo;
    }

    /**
     * 获取层级名称
     */
    private String getLevelName(Integer level) {
        if (level == null) return "";
        switch (level) {
            case 1: return "一级分类";
            case 2: return "二级分类";
            case 3: return "三级目标";
            default: return "未知";
        }
    }

    /**
     * 获取难度等级名称
     */
    private String getDifficultyLevelName(String difficultyLevel) {
        if (!StringUtils.hasText(difficultyLevel)) return "";
        switch (difficultyLevel) {
            case "beginner": return "初级";
            case "intermediate": return "中级";
            case "advanced": return "高级";
            default: return difficultyLevel;
        }
    }

    /**
     * 生成完整路径名称
     */
    private String generateFullPathName(LearningSkillCatalog entity) {
        List<String> pathNames = new ArrayList<>();

        // 递归获取路径名称
        buildPathNames(entity, pathNames);

        Collections.reverse(pathNames);
        return String.join(" > ", pathNames);
    }

    /**
     * 递归构建路径名称
     */
    private void buildPathNames(LearningSkillCatalog entity, List<String> pathNames) {
        pathNames.add(entity.getSkillName());

        if (StringUtils.hasText(entity.getParentCode())) {
            skillCatalogRepository.findBySkillCode(entity.getParentCode())
                    .ifPresent(parent -> buildPathNames(parent, pathNames));
        }
    }
}
