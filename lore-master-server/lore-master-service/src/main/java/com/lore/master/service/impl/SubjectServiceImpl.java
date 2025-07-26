package com.lore.master.service.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.data.entity.Subject;
import com.lore.master.data.repository.SubjectRepository;
import com.lore.master.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 学科服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {
    
    private final SubjectRepository subjectRepository;
    
    @Override
    public Subject getById(Long id) {
        return subjectRepository.findById(id).orElse(null);
    }
    
    @Override
    public Subject getByCode(String code) {
        return subjectRepository.findByCodeAndStatus(code, 1).orElse(null);
    }
    
    @Override
    public List<Subject> getAllActive() {
        return subjectRepository.findByStatusOrderBySortOrderAscCreateTimeDesc(1);
    }
    
    @Override
    public List<Subject> getByParentId(Long parentId) {
        return subjectRepository.findByParentIdAndStatusOrderBySortOrderAsc(parentId, 1);
    }
    
    @Override
    public Page<Subject> getSubjectPage(Pageable pageable, Map<String, Object> params) {
        String name = (String) params.get("name");
        String code = (String) params.get("code");
        Long parentId = (Long) params.get("parentId");
        Integer level = (Integer) params.get("level");
        Integer status = (Integer) params.get("status");
        
        return subjectRepository.findByConditions(name, code, parentId, level, status, pageable);
    }
    
    @Override
    @Transactional
    public boolean createSubject(Subject subject) {
        try {
            // 验证编码唯一性
            if (subjectRepository.existsByCodeAndIdNot(subject.getCode(), 0L)) {
                throw new RuntimeException("学科编码已存在");
            }
            
            // 设置默认值
            if (subject.getStatus() == null) {
                subject.setStatus(1);
            }
            if (subject.getLevel() == null) {
                subject.setLevel(1);
            }
            if (subject.getSortOrder() == null) {
                Long parentId = subject.getParentId() != null ? subject.getParentId() : 0L;
                Integer maxSortOrder = subjectRepository.getMaxSortOrderByParent(parentId);
                subject.setSortOrder(maxSortOrder + 1);
            }
            
            subjectRepository.save(subject);
            return true;
        } catch (Exception e) {
            log.error("创建学科失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateSubject(Subject subject) {
        try {
            Subject existingSubject = subjectRepository.findById(subject.getId()).orElse(null);
            if (existingSubject == null) {
                throw new RuntimeException("学科不存在");
            }
            
            // 验证编码唯一性
            if (!existingSubject.getCode().equals(subject.getCode()) &&
                subjectRepository.existsByCodeAndIdNot(subject.getCode(), subject.getId())) {
                throw new RuntimeException("学科编码已存在");
            }
            
            // 更新字段
            existingSubject.setName(subject.getName());
            existingSubject.setCode(subject.getCode());
            existingSubject.setDescription(subject.getDescription());
            existingSubject.setIcon(subject.getIcon());
            existingSubject.setColor(subject.getColor());
            existingSubject.setParentId(subject.getParentId());
            existingSubject.setLevel(subject.getLevel());
            existingSubject.setSortOrder(subject.getSortOrder());
            existingSubject.setStatus(subject.getStatus());
            existingSubject.setRemark(subject.getRemark());
            
            subjectRepository.save(existingSubject);
            return true;
        } catch (Exception e) {
            log.error("更新学科失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean deleteSubject(Long id) {
        try {
            Subject subject = subjectRepository.findById(id).orElse(null);
            if (subject == null) {
                throw new RuntimeException("学科不存在");
            }
            
            subjectRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("删除学科失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean batchDeleteSubjects(Long[] ids) {
        try {
            for (Long id : ids) {
                deleteSubject(id);
            }
            return true;
        } catch (Exception e) {
            log.error("批量删除学科失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        try {
            Subject subject = subjectRepository.findById(id).orElse(null);
            if (subject == null) {
                throw new RuntimeException("学科不存在");
            }
            
            subject.setStatus(status);
            subjectRepository.save(subject);
            return true;
        } catch (Exception e) {
            log.error("更新学科状态失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getSubjectStatistics() {
        try {
            Object[] result = subjectRepository.getSubjectStatistics();
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", result[0]);
            statistics.put("activeCount", result[1]);
            statistics.put("inactiveCount", result[2]);
            statistics.put("levelCount", result[3]);
            return statistics;
        } catch (Exception e) {
            log.error("获取学科统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getLevelDistribution() {
        try {
            Object[][] result = subjectRepository.getSubjectCountByLevel();
            Map<String, Object> distribution = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            
            for (Object[] row : result) {
                Map<String, Object> item = new HashMap<>();
                item.put("level", row[0]);
                item.put("count", row[1]);
                data.add(item);
            }
            
            distribution.put("data", data);
            return distribution;
        } catch (Exception e) {
            log.error("获取学科层级分布失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateSortOrder(Long id, Integer sortOrder) {
        try {
            Subject subject = subjectRepository.findById(id).orElse(null);
            if (subject == null) {
                throw new RuntimeException("学科不存在");
            }
            
            subject.setSortOrder(sortOrder);
            subjectRepository.save(subject);
            return true;
        } catch (Exception e) {
            log.error("更新学科排序失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
