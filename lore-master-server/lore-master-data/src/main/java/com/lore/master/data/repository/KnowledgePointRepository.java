package com.lore.master.data.repository;

import com.lore.master.data.entity.KnowledgePoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 知识点Repository
 */
@Repository
public interface KnowledgePointRepository extends JpaRepository<KnowledgePoint, Long> {
    
    /**
     * 根据编码查询知识点
     */
    Optional<KnowledgePoint> findByCodeAndStatus(String code, Integer status);
    
    /**
     * 检查编码是否存在
     */
    boolean existsByCodeAndIdNot(String code, Long id);
    
    /**
     * 根据学科ID查询知识点
     */
    List<KnowledgePoint> findBySubjectIdAndStatusOrderBySortOrderAscCreateTimeDesc(Long subjectId, Integer status);
    
    /**
     * 根据父级ID查询子知识点
     */
    List<KnowledgePoint> findByParentIdAndStatusOrderBySortOrderAscCreateTimeDesc(Long parentId, Integer status);
    
    /**
     * 分页查询知识点
     */
    @Query("SELECT kp FROM KnowledgePoint kp WHERE " +
           "(:title IS NULL OR kp.title LIKE %:title%) AND " +
           "(:code IS NULL OR kp.code LIKE %:code%) AND " +
           "(:subjectId IS NULL OR kp.subjectId = :subjectId) AND " +
           "(:parentId IS NULL OR kp.parentId = :parentId) AND " +
           "(:difficultyLevel IS NULL OR kp.difficultyLevel = :difficultyLevel) AND " +
           "(:importance IS NULL OR kp.importance = :importance) AND " +
           "(:status IS NULL OR kp.status = :status) AND " +
           "(:keywords IS NULL OR kp.keywords LIKE %:keywords%)")
    Page<KnowledgePoint> findByConditions(@Param("title") String title,
                                         @Param("code") String code,
                                         @Param("subjectId") Long subjectId,
                                         @Param("parentId") Long parentId,
                                         @Param("difficultyLevel") Integer difficultyLevel,
                                         @Param("importance") Integer importance,
                                         @Param("status") Integer status,
                                         @Param("keywords") String keywords,
                                         Pageable pageable);
    
    /**
     * 根据标签搜索知识点
     */
    @Query("SELECT kp FROM KnowledgePoint kp WHERE kp.status = 1 AND kp.tags LIKE %:tag%")
    List<KnowledgePoint> findByTag(@Param("tag") String tag);
    
    /**
     * 全文搜索知识点
     */
    @Query("SELECT kp FROM KnowledgePoint kp WHERE kp.status = 1 AND " +
           "(kp.title LIKE %:keyword% OR kp.content LIKE %:keyword% OR kp.keywords LIKE %:keyword%)")
    Page<KnowledgePoint> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 获取热门知识点（按浏览次数排序）
     */
    @Query("SELECT kp FROM KnowledgePoint kp WHERE kp.status = 1 ORDER BY kp.viewCount DESC")
    Page<KnowledgePoint> findPopularKnowledgePoints(Pageable pageable);
    
    /**
     * 增加浏览次数
     */
    @Modifying
    @Query("UPDATE KnowledgePoint kp SET kp.viewCount = kp.viewCount + 1 WHERE kp.id = :id")
    void incrementViewCount(@Param("id") Long id);
    
    /**
     * 获取知识点统计信息
     */
    @Query("SELECT " +
           "COUNT(kp) as totalCount, " +
           "SUM(CASE WHEN kp.status = 1 THEN 1 ELSE 0 END) as publishedCount, " +
           "SUM(CASE WHEN kp.status = 2 THEN 1 ELSE 0 END) as draftCount, " +
           "SUM(CASE WHEN kp.status = 0 THEN 1 ELSE 0 END) as disabledCount, " +
           "COUNT(DISTINCT kp.subjectId) as subjectCount, " +
           "SUM(kp.viewCount) as totalViews " +
           "FROM KnowledgePoint kp")
    Object[] getKnowledgePointStatistics();
    
    /**
     * 根据学科统计知识点数量
     */
    @Query("SELECT kp.subjectName, COUNT(kp) FROM KnowledgePoint kp WHERE kp.status = 1 GROUP BY kp.subjectName ORDER BY COUNT(kp) DESC")
    Object[][] getKnowledgePointCountBySubject();
    
    /**
     * 根据难度等级统计知识点数量
     */
    @Query("SELECT kp.difficultyLevel, COUNT(kp) FROM KnowledgePoint kp WHERE kp.status = 1 GROUP BY kp.difficultyLevel ORDER BY kp.difficultyLevel")
    Object[][] getKnowledgePointCountByDifficulty();
    
    /**
     * 根据重要程度统计知识点数量
     */
    @Query("SELECT kp.importance, COUNT(kp) FROM KnowledgePoint kp WHERE kp.status = 1 GROUP BY kp.importance ORDER BY kp.importance")
    Object[][] getKnowledgePointCountByImportance();
    
    /**
     * 获取最大排序权重
     */
    @Query("SELECT COALESCE(MAX(kp.sortOrder), 0) FROM KnowledgePoint kp WHERE kp.parentId = :parentId")
    Integer getMaxSortOrderByParent(@Param("parentId") Long parentId);
}
