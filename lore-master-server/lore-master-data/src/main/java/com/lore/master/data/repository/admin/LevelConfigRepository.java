package com.lore.master.data.repository.admin;

import com.lore.master.data.entity.admin.LevelConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 等级配置Repository
 */
@Repository
public interface LevelConfigRepository extends JpaRepository<LevelConfig, Long> {
    
    /**
     * 根据等级查询配置
     */
    Optional<LevelConfig> findByLevelAndStatus(Integer level, Integer status);
    
    /**
     * 检查等级是否存在
     */
    boolean existsByLevelAndIdNot(Integer level, Long id);
    
    /**
     * 根据状态查询所有等级配置
     */
    List<LevelConfig> findByStatusOrderByLevelAsc(Integer status);
    
    /**
     * 根据积分范围查询等级
     */
    @Query("SELECT lc FROM LevelConfig lc WHERE lc.status = 1 AND lc.minScore <= :score AND (lc.maxScore >= :score OR lc.maxScore = -1) ORDER BY lc.level DESC")
    Optional<LevelConfig> findByScoreRange(@Param("score") Integer score);
    
    /**
     * 分页查询等级配置
     */
    @Query("SELECT lc FROM LevelConfig lc WHERE " +
           "(:name IS NULL OR lc.name LIKE %:name%) AND " +
           "(:level IS NULL OR lc.level = :level) AND " +
           "(:minScore IS NULL OR lc.minScore >= :minScore) AND " +
           "(:maxScore IS NULL OR lc.maxScore <= :maxScore) AND " +
           "(:status IS NULL OR lc.status = :status)")
    Page<LevelConfig> findByConditions(@Param("name") String name,
                                      @Param("level") Integer level,
                                      @Param("minScore") Integer minScore,
                                      @Param("maxScore") Integer maxScore,
                                      @Param("status") Integer status,
                                      Pageable pageable);
    
    /**
     * 获取等级配置统计信息
     */
    @Query("SELECT " +
           "COUNT(lc) as totalCount, " +
           "SUM(CASE WHEN lc.status = 1 THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN lc.status = 0 THEN 1 ELSE 0 END) as inactiveCount, " +
           "MAX(lc.level) as maxLevel, " +
           "MIN(lc.level) as minLevel, " +
           "AVG(lc.minScore) as avgMinScore " +
           "FROM LevelConfig lc")
    Object[] getLevelConfigStatistics();
    
    /**
     * 获取等级分布统计
     */
    @Query("SELECT lc.level, lc.name, lc.minScore, lc.maxScore FROM LevelConfig lc WHERE lc.status = 1 ORDER BY lc.level")
    Object[][] getLevelDistribution();
    
    /**
     * 获取下一个等级
     */
    @Query("SELECT lc FROM LevelConfig lc WHERE lc.status = 1 AND lc.level > :currentLevel ORDER BY lc.level ASC")
    Optional<LevelConfig> findNextLevel(@Param("currentLevel") Integer currentLevel);
    
    /**
     * 获取上一个等级
     */
    @Query("SELECT lc FROM LevelConfig lc WHERE lc.status = 1 AND lc.level < :currentLevel ORDER BY lc.level DESC")
    Optional<LevelConfig> findPreviousLevel(@Param("currentLevel") Integer currentLevel);
}
