package com.lore.master.data.repository;

import com.lore.master.data.entity.CareerTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 职业目标Repository
 */
@Repository
public interface CareerTargetRepository extends JpaRepository<CareerTarget, Long> {
    
    /**
     * 根据编码查询职业目标
     */
    Optional<CareerTarget> findByCodeAndStatus(String code, Integer status);
    
    /**
     * 检查编码是否存在
     */
    boolean existsByCodeAndIdNot(String code, Long id);
    
    /**
     * 根据状态查询所有职业目标
     */
    List<CareerTarget> findByStatusOrderBySortOrderAscCreateTimeDesc(Integer status);
    
    /**
     * 分页查询职业目标
     */
    @Query("SELECT ct FROM CareerTarget ct WHERE " +
           "(:name IS NULL OR ct.name LIKE %:name%) AND " +
           "(:code IS NULL OR ct.code LIKE %:code%) AND " +
           "(:category IS NULL OR ct.category = :category) AND " +
           "(:difficultyLevel IS NULL OR ct.difficultyLevel = :difficultyLevel) AND " +
           "(:status IS NULL OR ct.status = :status)")
    Page<CareerTarget> findByConditions(@Param("name") String name,
                                       @Param("code") String code,
                                       @Param("category") String category,
                                       @Param("difficultyLevel") Integer difficultyLevel,
                                       @Param("status") Integer status,
                                       Pageable pageable);
    
    /**
     * 根据分类查询职业目标
     */
    List<CareerTarget> findByCategoryAndStatusOrderBySortOrderAsc(String category, Integer status);
    
    /**
     * 根据难度等级查询职业目标
     */
    List<CareerTarget> findByDifficultyLevelAndStatusOrderBySortOrderAsc(Integer difficultyLevel, Integer status);
    
    /**
     * 获取职业目标统计信息
     */
    @Query("SELECT " +
           "COUNT(ct) as totalCount, " +
           "SUM(CASE WHEN ct.status = 1 THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN ct.status = 0 THEN 1 ELSE 0 END) as inactiveCount, " +
           "COUNT(DISTINCT ct.category) as categoryCount, " +
           "AVG(ct.estimatedHours) as avgHours " +
           "FROM CareerTarget ct")
    Object[] getCareerTargetStatistics();
    
    /**
     * 根据分类统计职业目标数量
     */
    @Query("SELECT ct.category, COUNT(ct) FROM CareerTarget ct WHERE ct.status = 1 GROUP BY ct.category ORDER BY COUNT(ct) DESC")
    Object[][] getCareerTargetCountByCategory();
    
    /**
     * 根据难度等级统计职业目标数量
     */
    @Query("SELECT ct.difficultyLevel, COUNT(ct) FROM CareerTarget ct WHERE ct.status = 1 GROUP BY ct.difficultyLevel ORDER BY ct.difficultyLevel")
    Object[][] getCareerTargetCountByDifficulty();
    
    /**
     * 获取最大排序权重
     */
    @Query("SELECT COALESCE(MAX(ct.sortOrder), 0) FROM CareerTarget ct")
    Integer getMaxSortOrder();
}
