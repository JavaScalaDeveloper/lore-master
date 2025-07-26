package com.lore.master.service.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.data.entity.KnowledgePoint;
import com.lore.master.data.repository.KnowledgePointRepository;
import com.lore.master.service.KnowledgePointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 知识点服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgePointServiceImpl implements KnowledgePointService {
    
    private final KnowledgePointRepository knowledgePointRepository;
    
    @Override
    public KnowledgePoint getById(Long id) {
        return knowledgePointRepository.findById(id).orElse(null);
    }
    
    @Override
    public KnowledgePoint getByCode(String code) {
        return knowledgePointRepository.findByCodeAndStatus(code, 1).orElse(null);
    }
    
    @Override
    public List<KnowledgePoint> getBySubjectId(Long subjectId) {
        return knowledgePointRepository.findBySubjectIdAndStatusOrderBySortOrderAscCreateTimeDesc(subjectId, 1);
    }
    
    @Override
    public List<KnowledgePoint> getByParentId(Long parentId) {
        return knowledgePointRepository.findByParentIdAndStatusOrderBySortOrderAscCreateTimeDesc(parentId, 1);
    }
    
    @Override
    public Page<KnowledgePoint> getKnowledgePointPage(Pageable pageable, Map<String, Object> params) {
        String title = (String) params.get("title");
        String code = (String) params.get("code");
        Long subjectId = (Long) params.get("subjectId");
        Long parentId = (Long) params.get("parentId");
        Integer difficultyLevel = (Integer) params.get("difficultyLevel");
        Integer importance = (Integer) params.get("importance");
        Integer status = (Integer) params.get("status");
        String keywords = (String) params.get("keywords");
        
        return knowledgePointRepository.findByConditions(title, code, subjectId, parentId, 
                difficultyLevel, importance, status, keywords, pageable);
    }
    
    @Override
    @Transactional
    public boolean createKnowledgePoint(KnowledgePoint knowledgePoint) {
        try {
            // 验证编码唯一性
            if (knowledgePointRepository.existsByCodeAndIdNot(knowledgePoint.getCode(), 0L)) {
                throw new RuntimeException("知识点编码已存在");
            }
            
            // 设置默认值
            if (knowledgePoint.getStatus() == null) {
                knowledgePoint.setStatus(1);
            }
            if (knowledgePoint.getDifficultyLevel() == null) {
                knowledgePoint.setDifficultyLevel(1);
            }
            if (knowledgePoint.getImportance() == null) {
                knowledgePoint.setImportance(1);
            }
            if (knowledgePoint.getLevel() == null) {
                knowledgePoint.setLevel(1);
            }
            if (knowledgePoint.getViewCount() == null) {
                knowledgePoint.setViewCount(0);
            }
            if (knowledgePoint.getSortOrder() == null) {
                Long parentId = knowledgePoint.getParentId() != null ? knowledgePoint.getParentId() : 0L;
                Integer maxSortOrder = knowledgePointRepository.getMaxSortOrderByParent(parentId);
                knowledgePoint.setSortOrder(maxSortOrder + 1);
            }
            
            knowledgePointRepository.save(knowledgePoint);
            return true;
        } catch (Exception e) {
            log.error("创建知识点失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateKnowledgePoint(KnowledgePoint knowledgePoint) {
        try {
            KnowledgePoint existingKnowledgePoint = knowledgePointRepository.findById(knowledgePoint.getId()).orElse(null);
            if (existingKnowledgePoint == null) {
                throw new RuntimeException("知识点不存在");
            }
            
            // 验证编码唯一性
            if (!existingKnowledgePoint.getCode().equals(knowledgePoint.getCode()) &&
                knowledgePointRepository.existsByCodeAndIdNot(knowledgePoint.getCode(), knowledgePoint.getId())) {
                throw new RuntimeException("知识点编码已存在");
            }
            
            // 更新字段
            existingKnowledgePoint.setTitle(knowledgePoint.getTitle());
            existingKnowledgePoint.setCode(knowledgePoint.getCode());
            existingKnowledgePoint.setContent(knowledgePoint.getContent());
            existingKnowledgePoint.setSummary(knowledgePoint.getSummary());
            existingKnowledgePoint.setSubjectId(knowledgePoint.getSubjectId());
            existingKnowledgePoint.setSubjectName(knowledgePoint.getSubjectName());
            existingKnowledgePoint.setParentId(knowledgePoint.getParentId());
            existingKnowledgePoint.setLevel(knowledgePoint.getLevel());
            existingKnowledgePoint.setDifficultyLevel(knowledgePoint.getDifficultyLevel());
            existingKnowledgePoint.setImportance(knowledgePoint.getImportance());
            existingKnowledgePoint.setTags(knowledgePoint.getTags());
            existingKnowledgePoint.setKeywords(knowledgePoint.getKeywords());
            existingKnowledgePoint.setRelatedLinks(knowledgePoint.getRelatedLinks());
            existingKnowledgePoint.setSortOrder(knowledgePoint.getSortOrder());
            existingKnowledgePoint.setStatus(knowledgePoint.getStatus());
            existingKnowledgePoint.setRemark(knowledgePoint.getRemark());
            
            knowledgePointRepository.save(existingKnowledgePoint);
            return true;
        } catch (Exception e) {
            log.error("更新知识点失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean deleteKnowledgePoint(Long id) {
        try {
            KnowledgePoint knowledgePoint = knowledgePointRepository.findById(id).orElse(null);
            if (knowledgePoint == null) {
                throw new RuntimeException("知识点不存在");
            }
            
            knowledgePointRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("删除知识点失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean batchDeleteKnowledgePoints(Long[] ids) {
        try {
            for (Long id : ids) {
                deleteKnowledgePoint(id);
            }
            return true;
        } catch (Exception e) {
            log.error("批量删除知识点失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        try {
            KnowledgePoint knowledgePoint = knowledgePointRepository.findById(id).orElse(null);
            if (knowledgePoint == null) {
                throw new RuntimeException("知识点不存在");
            }
            
            knowledgePoint.setStatus(status);
            knowledgePointRepository.save(knowledgePoint);
            return true;
        } catch (Exception e) {
            log.error("更新知识点状态失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public List<KnowledgePoint> searchByTag(String tag) {
        return knowledgePointRepository.findByTag(tag);
    }
    
    @Override
    public Page<KnowledgePoint> searchByKeyword(String keyword, Pageable pageable) {
        return knowledgePointRepository.searchByKeyword(keyword, pageable);
    }
    
    @Override
    public Page<KnowledgePoint> getPopularKnowledgePoints(Pageable pageable) {
        return knowledgePointRepository.findPopularKnowledgePoints(pageable);
    }
    
    @Override
    @Transactional
    public boolean incrementViewCount(Long id) {
        try {
            knowledgePointRepository.incrementViewCount(id);
            return true;
        } catch (Exception e) {
            log.error("增加知识点浏览次数失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getKnowledgePointStatistics() {
        try {
            Object[] result = knowledgePointRepository.getKnowledgePointStatistics();
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", result[0]);
            statistics.put("publishedCount", result[1]);
            statistics.put("draftCount", result[2]);
            statistics.put("disabledCount", result[3]);
            statistics.put("subjectCount", result[4]);
            statistics.put("totalViews", result[5]);
            return statistics;
        } catch (Exception e) {
            log.error("获取知识点统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getSubjectDistribution() {
        try {
            Object[][] result = knowledgePointRepository.getKnowledgePointCountBySubject();
            Map<String, Object> distribution = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            
            for (Object[] row : result) {
                Map<String, Object> item = new HashMap<>();
                item.put("subject", row[0]);
                item.put("count", row[1]);
                data.add(item);
            }
            
            distribution.put("data", data);
            return distribution;
        } catch (Exception e) {
            log.error("获取知识点学科分布失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getDifficultyDistribution() {
        try {
            Object[][] result = knowledgePointRepository.getKnowledgePointCountByDifficulty();
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
            log.error("获取知识点难度分布失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getImportanceDistribution() {
        try {
            Object[][] result = knowledgePointRepository.getKnowledgePointCountByImportance();
            Map<String, Object> distribution = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            
            for (Object[] row : result) {
                Map<String, Object> item = new HashMap<>();
                item.put("importance", row[0]);
                item.put("count", row[1]);
                data.add(item);
            }
            
            distribution.put("data", data);
            return distribution;
        } catch (Exception e) {
            log.error("获取知识点重要程度分布失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateSortOrder(Long id, Integer sortOrder) {
        try {
            KnowledgePoint knowledgePoint = knowledgePointRepository.findById(id).orElse(null);
            if (knowledgePoint == null) {
                throw new RuntimeException("知识点不存在");
            }
            
            knowledgePoint.setSortOrder(sortOrder);
            knowledgePointRepository.save(knowledgePoint);
            return true;
        } catch (Exception e) {
            log.error("更新知识点排序失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
