package com.lore.master.data.repository;

import com.lore.master.data.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题目Repository
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    /**
     * 根据学科ID查询题目
     */
    List<Question> findBySubjectIdAndStatusOrderByCreateTimeDesc(Long subjectId, Integer status);
    
    /**
     * 根据知识点ID查询题目
     */
    List<Question> findByKnowledgePointIdAndStatusOrderByCreateTimeDesc(Long knowledgePointId, Integer status);
    
    /**
     * 根据题目类型查询题目
     */
    List<Question> findByTypeAndStatusOrderByCreateTimeDesc(Integer type, Integer status);
    
    /**
     * 分页查询题目
     */
    @Query("SELECT q FROM Question q WHERE " +
           "(:title IS NULL OR q.title LIKE %:title%) AND " +
           "(:type IS NULL OR q.type = :type) AND " +
           "(:subjectId IS NULL OR q.subjectId = :subjectId) AND " +
           "(:knowledgePointId IS NULL OR q.knowledgePointId = :knowledgePointId) AND " +
           "(:difficultyLevel IS NULL OR q.difficultyLevel = :difficultyLevel) AND " +
           "(:status IS NULL OR q.status = :status) AND " +
           "(:tags IS NULL OR q.tags LIKE %:tags%)")
    Page<Question> findByConditions(@Param("title") String title,
                                   @Param("type") Integer type,
                                   @Param("subjectId") Long subjectId,
                                   @Param("knowledgePointId") Long knowledgePointId,
                                   @Param("difficultyLevel") Integer difficultyLevel,
                                   @Param("status") Integer status,
                                   @Param("tags") String tags,
                                   Pageable pageable);
    
    /**
     * 根据标签搜索题目
     */
    @Query("SELECT q FROM Question q WHERE q.status = 1 AND q.tags LIKE %:tag%")
    List<Question> findByTag(@Param("tag") String tag);
    
    /**
     * 全文搜索题目
     */
    @Query("SELECT q FROM Question q WHERE q.status = 1 AND " +
           "(q.title LIKE %:keyword% OR q.content LIKE %:keyword%)")
    Page<Question> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 随机获取题目
     */
    @Query(value = "SELECT * FROM questions WHERE status = 1 AND " +
                   "(:subjectId IS NULL OR subject_id = :subjectId) AND " +
                   "(:difficultyLevel IS NULL OR difficulty_level = :difficultyLevel) AND " +
                   "(:type IS NULL OR type = :type) " +
                   "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("subjectId") Long subjectId,
                                      @Param("difficultyLevel") Integer difficultyLevel,
                                      @Param("type") Integer type,
                                      @Param("limit") Integer limit);
    
    /**
     * 增加使用次数
     */
    @Modifying
    @Query("UPDATE Question q SET q.usageCount = q.usageCount + 1 WHERE q.id = :id")
    void incrementUsageCount(@Param("id") Long id);
    
    /**
     * 更新正确率
     */
    @Modifying
    @Query("UPDATE Question q SET q.accuracyRate = :accuracyRate WHERE q.id = :id")
    void updateAccuracyRate(@Param("id") Long id, @Param("accuracyRate") Double accuracyRate);
    
    /**
     * 获取题目统计信息
     */
    @Query("SELECT " +
           "COUNT(q) as totalCount, " +
           "SUM(CASE WHEN q.status = 1 THEN 1 ELSE 0 END) as publishedCount, " +
           "SUM(CASE WHEN q.status = 2 THEN 1 ELSE 0 END) as draftCount, " +
           "SUM(CASE WHEN q.status = 0 THEN 1 ELSE 0 END) as disabledCount, " +
           "COUNT(DISTINCT q.subjectId) as subjectCount, " +
           "SUM(q.usageCount) as totalUsage, " +
           "AVG(q.accuracyRate) as avgAccuracy " +
           "FROM Question q")
    Object[] getQuestionStatistics();
    
    /**
     * 根据题目类型统计数量
     */
    @Query("SELECT q.type, COUNT(q) FROM Question q WHERE q.status = 1 GROUP BY q.type ORDER BY q.type")
    Object[][] getQuestionCountByType();
    
    /**
     * 根据难度等级统计数量
     */
    @Query("SELECT q.difficultyLevel, COUNT(q) FROM Question q WHERE q.status = 1 GROUP BY q.difficultyLevel ORDER BY q.difficultyLevel")
    Object[][] getQuestionCountByDifficulty();
    
    /**
     * 根据学科统计数量
     */
    @Query("SELECT q.subjectName, COUNT(q) FROM Question q WHERE q.status = 1 GROUP BY q.subjectName ORDER BY COUNT(q) DESC")
    Object[][] getQuestionCountBySubject();
}
