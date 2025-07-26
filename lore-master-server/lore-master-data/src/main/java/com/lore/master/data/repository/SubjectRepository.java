package com.lore.master.data.repository;

import com.lore.master.data.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学科Repository
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    
    /**
     * 根据编码查询学科
     */
    Optional<Subject> findByCodeAndStatus(String code, Integer status);
    
    /**
     * 检查编码是否存在
     */
    boolean existsByCodeAndIdNot(String code, Long id);
    
    /**
     * 根据状态查询所有学科
     */
    List<Subject> findByStatusOrderBySortOrderAscCreateTimeDesc(Integer status);
    
    /**
     * 根据父级ID查询子学科
     */
    List<Subject> findByParentIdAndStatusOrderBySortOrderAsc(Long parentId, Integer status);
    
    /**
     * 分页查询学科
     */
    @Query("SELECT s FROM Subject s WHERE " +
           "(:name IS NULL OR s.name LIKE %:name%) AND " +
           "(:code IS NULL OR s.code LIKE %:code%) AND " +
           "(:parentId IS NULL OR s.parentId = :parentId) AND " +
           "(:level IS NULL OR s.level = :level) AND " +
           "(:status IS NULL OR s.status = :status)")
    Page<Subject> findByConditions(@Param("name") String name,
                                  @Param("code") String code,
                                  @Param("parentId") Long parentId,
                                  @Param("level") Integer level,
                                  @Param("status") Integer status,
                                  Pageable pageable);
    
    /**
     * 获取学科统计信息
     */
    @Query("SELECT " +
           "COUNT(s) as totalCount, " +
           "SUM(CASE WHEN s.status = 1 THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN s.status = 0 THEN 1 ELSE 0 END) as inactiveCount, " +
           "COUNT(DISTINCT s.level) as levelCount " +
           "FROM Subject s")
    Object[] getSubjectStatistics();
    
    /**
     * 根据层级统计学科数量
     */
    @Query("SELECT s.level, COUNT(s) FROM Subject s WHERE s.status = 1 GROUP BY s.level ORDER BY s.level")
    Object[][] getSubjectCountByLevel();
    
    /**
     * 获取最大排序权重
     */
    @Query("SELECT COALESCE(MAX(s.sortOrder), 0) FROM Subject s WHERE s.parentId = :parentId")
    Integer getMaxSortOrderByParent(@Param("parentId") Long parentId);
}
