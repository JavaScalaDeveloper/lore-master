package com.lore.master.service.admin;

import com.lore.master.data.entity.admin.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 学科服务接口
 */
public interface SubjectService {
    
    /**
     * 根据ID查询学科
     *
     * @param id 学科ID
     * @return 学科信息
     */
    Subject getById(Long id);
    
    /**
     * 根据编码查询学科
     *
     * @param code 学科编码
     * @return 学科信息
     */
    Subject getByCode(String code);
    
    /**
     * 获取所有启用的学科
     *
     * @return 学科列表
     */
    List<Subject> getAllActive();
    
    /**
     * 根据父级ID获取子学科
     *
     * @param parentId 父级ID
     * @return 学科列表
     */
    List<Subject> getByParentId(Long parentId);
    
    /**
     * 分页查询学科
     *
     * @param pageable 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    Page<Subject> getSubjectPage(Pageable pageable, Map<String, Object> params);
    
    /**
     * 创建学科
     *
     * @param subject 学科信息
     * @return 是否成功
     */
    boolean createSubject(Subject subject);
    
    /**
     * 更新学科
     *
     * @param subject 学科信息
     * @return 是否成功
     */
    boolean updateSubject(Subject subject);
    
    /**
     * 删除学科
     *
     * @param id 学科ID
     * @return 是否成功
     */
    boolean deleteSubject(Long id);
    
    /**
     * 批量删除学科
     *
     * @param ids 学科ID列表
     * @return 是否成功
     */
    boolean batchDeleteSubjects(Long[] ids);
    
    /**
     * 更新学科状态
     *
     * @param id 学科ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
    
    /**
     * 获取学科统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getSubjectStatistics();
    
    /**
     * 获取学科层级分布
     *
     * @return 层级分布
     */
    Map<String, Object> getLevelDistribution();
    
    /**
     * 调整排序权重
     *
     * @param id 学科ID
     * @param sortOrder 排序权重
     * @return 是否成功
     */
    boolean updateSortOrder(Long id, Integer sortOrder);
}
