package com.lore.master.service.admin;

import com.lore.master.data.entity.admin.CareerTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 职业目标服务接口
 */
public interface CareerTargetService {
    
    /**
     * 根据ID查询职业目标
     *
     * @param id 职业目标ID
     * @return 职业目标信息
     */
    CareerTarget getById(Long id);
    
    /**
     * 根据编码查询职业目标
     *
     * @param code 职业目标编码
     * @return 职业目标信息
     */
    CareerTarget getByCode(String code);
    
    /**
     * 获取所有启用的职业目标
     *
     * @return 职业目标列表
     */
    List<CareerTarget> getAllActive();
    
    /**
     * 分页查询职业目标
     *
     * @param pageable 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    Page<CareerTarget> getCareerTargetPage(Pageable pageable, Map<String, Object> params);
    
    /**
     * 创建职业目标
     *
     * @param careerTarget 职业目标信息
     * @return 是否成功
     */
    boolean createCareerTarget(CareerTarget careerTarget);
    
    /**
     * 更新职业目标
     *
     * @param careerTarget 职业目标信息
     * @return 是否成功
     */
    boolean updateCareerTarget(CareerTarget careerTarget);
    
    /**
     * 删除职业目标
     *
     * @param id 职业目标ID
     * @return 是否成功
     */
    boolean deleteCareerTarget(Long id);
    
    /**
     * 批量删除职业目标
     *
     * @param ids 职业目标ID列表
     * @return 是否成功
     */
    boolean batchDeleteCareerTargets(Long[] ids);
    
    /**
     * 更新职业目标状态
     *
     * @param id 职业目标ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
    
    /**
     * 根据分类获取职业目标
     *
     * @param category 分类
     * @return 职业目标列表
     */
    List<CareerTarget> getByCategory(String category);
    
    /**
     * 根据难度等级获取职业目标
     *
     * @param difficultyLevel 难度等级
     * @return 职业目标列表
     */
    List<CareerTarget> getByDifficultyLevel(Integer difficultyLevel);
    
    /**
     * 获取职业目标统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getCareerTargetStatistics();
    
    /**
     * 获取职业目标分类分布
     *
     * @return 分类分布
     */
    Map<String, Object> getCategoryDistribution();
    
    /**
     * 获取职业目标难度分布
     *
     * @return 难度分布
     */
    Map<String, Object> getDifficultyDistribution();
    
    /**
     * 调整排序权重
     *
     * @param id 职业目标ID
     * @param sortOrder 排序权重
     * @return 是否成功
     */
    boolean updateSortOrder(Long id, Integer sortOrder);
}
