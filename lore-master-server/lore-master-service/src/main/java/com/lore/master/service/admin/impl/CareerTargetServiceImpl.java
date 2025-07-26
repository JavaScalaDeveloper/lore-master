package com.lore.master.service.admin.impl;

import com.lore.master.data.entity.admin.CareerTarget;
import com.lore.master.data.repository.admin.CareerTargetRepository;
import com.lore.master.service.admin.CareerTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 职业目标服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CareerTargetServiceImpl implements CareerTargetService {
    
    private final CareerTargetRepository careerTargetRepository;
    
    @Override
    public CareerTarget getById(Long id) {
        return careerTargetRepository.findById(id).orElse(null);
    }
    
    @Override
    public CareerTarget getByCode(String code) {
        return careerTargetRepository.findByCodeAndStatus(code, 1).orElse(null);
    }
    
    @Override
    public List<CareerTarget> getAllActive() {
        return careerTargetRepository.findByStatusOrderBySortOrderAscCreateTimeDesc(1);
    }
    
    @Override
    public Page<CareerTarget> getCareerTargetPage(Pageable pageable, Map<String, Object> params) {
        String name = (String) params.get("name");
        String code = (String) params.get("code");
        String category = (String) params.get("category");
        Integer difficultyLevel = (Integer) params.get("difficultyLevel");
        Integer status = (Integer) params.get("status");
        
        return careerTargetRepository.findByConditions(name, code, category, difficultyLevel, status, pageable);
    }
    
    @Override
    @Transactional
    public boolean createCareerTarget(CareerTarget careerTarget) {
        try {
            // 验证编码唯一性
            if (careerTargetRepository.existsByCodeAndIdNot(careerTarget.getCode(), 0L)) {
                throw new RuntimeException("职业目标编码已存在");
            }
            
            // 设置默认值
            if (careerTarget.getStatus() == null) {
                careerTarget.setStatus(1);
            }
            if (careerTarget.getDifficultyLevel() == null) {
                careerTarget.setDifficultyLevel(1);
            }
            if (careerTarget.getEstimatedHours() == null) {
                careerTarget.setEstimatedHours(0);
            }
            if (careerTarget.getSortOrder() == null) {
                Integer maxSortOrder = careerTargetRepository.getMaxSortOrder();
                careerTarget.setSortOrder(maxSortOrder + 1);
            }
            
            careerTargetRepository.save(careerTarget);
            return true;
        } catch (Exception e) {
            log.error("创建职业目标失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateCareerTarget(CareerTarget careerTarget) {
        try {
            CareerTarget existingCareerTarget = careerTargetRepository.findById(careerTarget.getId()).orElse(null);
            if (existingCareerTarget == null) {
                throw new RuntimeException("职业目标不存在");
            }
            
            // 验证编码唯一性
            if (!existingCareerTarget.getCode().equals(careerTarget.getCode()) &&
                careerTargetRepository.existsByCodeAndIdNot(careerTarget.getCode(), careerTarget.getId())) {
                throw new RuntimeException("职业目标编码已存在");
            }
            
            // 更新字段
            existingCareerTarget.setName(careerTarget.getName());
            existingCareerTarget.setCode(careerTarget.getCode());
            existingCareerTarget.setDescription(careerTarget.getDescription());
            existingCareerTarget.setCategory(careerTarget.getCategory());
            existingCareerTarget.setDifficultyLevel(careerTarget.getDifficultyLevel());
            existingCareerTarget.setEstimatedHours(careerTarget.getEstimatedHours());
            existingCareerTarget.setRequiredSkills(careerTarget.getRequiredSkills());
            existingCareerTarget.setLearningPath(careerTarget.getLearningPath());
            existingCareerTarget.setRecommendedResources(careerTarget.getRecommendedResources());
            existingCareerTarget.setSortOrder(careerTarget.getSortOrder());
            existingCareerTarget.setStatus(careerTarget.getStatus());
            existingCareerTarget.setRemark(careerTarget.getRemark());
            
            careerTargetRepository.save(existingCareerTarget);
            return true;
        } catch (Exception e) {
            log.error("更新职业目标失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean deleteCareerTarget(Long id) {
        try {
            CareerTarget careerTarget = careerTargetRepository.findById(id).orElse(null);
            if (careerTarget == null) {
                throw new RuntimeException("职业目标不存在");
            }
            
            careerTargetRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("删除职业目标失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean batchDeleteCareerTargets(Long[] ids) {
        try {
            for (Long id : ids) {
                deleteCareerTarget(id);
            }
            return true;
        } catch (Exception e) {
            log.error("批量删除职业目标失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        try {
            CareerTarget careerTarget = careerTargetRepository.findById(id).orElse(null);
            if (careerTarget == null) {
                throw new RuntimeException("职业目标不存在");
            }
            
            careerTarget.setStatus(status);
            careerTargetRepository.save(careerTarget);
            return true;
        } catch (Exception e) {
            log.error("更新职业目标状态失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public List<CareerTarget> getByCategory(String category) {
        return careerTargetRepository.findByCategoryAndStatusOrderBySortOrderAsc(category, 1);
    }
    
    @Override
    public List<CareerTarget> getByDifficultyLevel(Integer difficultyLevel) {
        return careerTargetRepository.findByDifficultyLevelAndStatusOrderBySortOrderAsc(difficultyLevel, 1);
    }
    
    @Override
    public Map<String, Object> getCareerTargetStatistics() {
        try {
            Object[] result = careerTargetRepository.getCareerTargetStatistics();
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", result[0]);
            statistics.put("activeCount", result[1]);
            statistics.put("inactiveCount", result[2]);
            statistics.put("categoryCount", result[3]);
            statistics.put("avgHours", result[4]);
            return statistics;
        } catch (Exception e) {
            log.error("获取职业目标统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getCategoryDistribution() {
        try {
            Object[][] result = careerTargetRepository.getCareerTargetCountByCategory();
            Map<String, Object> distribution = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            
            for (Object[] row : result) {
                Map<String, Object> item = new HashMap<>();
                item.put("category", row[0]);
                item.put("count", row[1]);
                data.add(item);
            }
            
            distribution.put("data", data);
            return distribution;
        } catch (Exception e) {
            log.error("获取职业目标分类分布失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getDifficultyDistribution() {
        try {
            Object[][] result = careerTargetRepository.getCareerTargetCountByDifficulty();
            Map<String, Object> distribution = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            
            for (Object[] row : result) {
                Map<String, Object> item = new HashMap<>();
                item.put("difficulty", row[0]);
                item.put("count", row[1]);
                data.add(item);
            }
            
            distribution.put("data", data);
            return distribution;
        } catch (Exception e) {
            log.error("获取职业目标难度分布失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateSortOrder(Long id, Integer sortOrder) {
        try {
            CareerTarget careerTarget = careerTargetRepository.findById(id).orElse(null);
            if (careerTarget == null) {
                throw new RuntimeException("职业目标不存在");
            }
            
            careerTarget.setSortOrder(sortOrder);
            careerTargetRepository.save(careerTarget);
            return true;
        } catch (Exception e) {
            log.error("更新职业目标排序失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
