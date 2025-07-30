package com.lore.master.data.repository.admin;

import com.lore.master.data.entity.admin.LearningSkillCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学习技能目录Repository
 */
@Repository
public interface LearningSkillCatalogRepository extends JpaRepository<LearningSkillCatalog, Long> {

    /**
     * 根据技能编码查询
     */
    Optional<LearningSkillCatalog> findBySkillCode(String skillCode);

    /**
     * 根据技能编码和是否启用查询
     */
    Optional<LearningSkillCatalog> findBySkillCodeAndIsActive(String skillCode, Boolean isActive);

    /**
     * 根据层级查询所有技能
     */
    List<LearningSkillCatalog> findByLevelAndIsActiveOrderBySortOrder(Integer level, Boolean isActive);

    /**
     * 根据父级编码查询子级技能
     */
    List<LearningSkillCatalog> findByParentCodeAndIsActiveOrderBySortOrder(String parentCode, Boolean isActive);

    /**
     * 根据技能路径前缀查询（用于查询某个分类下的所有技能）
     */
    List<LearningSkillCatalog> findBySkillPathStartingWithAndIsActiveOrderByLevelAscSortOrderAsc(String pathPrefix, Boolean isActive);

    /**
     * 根据难度等级查询
     */
    List<LearningSkillCatalog> findByDifficultyLevelAndIsActiveOrderBySortOrder(String difficultyLevel, Boolean isActive);

    /**
     * 分页查询技能目录
     */
    Page<LearningSkillCatalog> findByIsActiveOrderByLevelAscSortOrderAsc(Boolean isActive, Pageable pageable);

    /**
     * 查询所有技能目录（不分页）
     */
    List<LearningSkillCatalog> findByIsActiveOrderByLevelAscSortOrderAsc(Boolean isActive);

    /**
     * 根据技能名称模糊查询
     */
    List<LearningSkillCatalog> findBySkillNameContainingAndIsActiveOrderBySortOrder(String skillName, Boolean isActive);

    /**
     * 根据层级和父级编码查询
     */
    List<LearningSkillCatalog> findByLevelAndParentCodeAndIsActiveOrderBySortOrder(Integer level, String parentCode, Boolean isActive);

    /**
     * 查询指定父级下的技能数量
     */
    long countByParentCodeAndIsActive(String parentCode, Boolean isActive);

    /**
     * 查询指定层级的技能数量
     */
    long countByLevelAndIsActive(Integer level, Boolean isActive);

    /**
     * 查询启用的技能总数
     */
    long countByIsActive(Boolean isActive);

    /**
     * 检查技能编码是否存在
     */
    boolean existsBySkillCode(String skillCode);

    /**
     * 检查技能路径是否存在
     */
    boolean existsBySkillPath(String skillPath);

    /**
     * 根据标签查询技能（JSON查询）
     */
    @Query("SELECT s FROM LearningSkillCatalog s WHERE s.tags LIKE %:tag% AND s.isActive = :isActive ORDER BY s.sortOrder")
    List<LearningSkillCatalog> findByTagsContaining(@Param("tag") String tag, @Param("isActive") Boolean isActive);

    /**
     * 查询所有一级分类
     */
    @Query("SELECT s FROM LearningSkillCatalog s WHERE s.level = 1 AND s.isActive = :isActive ORDER BY s.sortOrder")
    List<LearningSkillCatalog> findAllFirstLevelCategories(@Param("isActive") Boolean isActive);

    /**
     * 查询指定一级分类下的所有二级分类
     */
    @Query("SELECT s FROM LearningSkillCatalog s WHERE s.level = 2 AND s.parentCode = :parentCode AND s.isActive = :isActive ORDER BY s.sortOrder")
    List<LearningSkillCatalog> findSecondLevelByParent(@Param("parentCode") String parentCode, @Param("isActive") Boolean isActive);

    /**
     * 查询指定二级分类下的所有三级目标
     */
    @Query("SELECT s FROM LearningSkillCatalog s WHERE s.level = 3 AND s.parentCode = :parentCode AND s.isActive = :isActive ORDER BY s.sortOrder")
    List<LearningSkillCatalog> findThirdLevelByParent(@Param("parentCode") String parentCode, @Param("isActive") Boolean isActive);

    /**
     * 根据技能路径查询完整层级结构
     */
    @Query("SELECT s FROM LearningSkillCatalog s WHERE s.skillPath LIKE :pathPattern AND s.isActive = :isActive ORDER BY s.level, s.sortOrder")
    List<LearningSkillCatalog> findBySkillPathPattern(@Param("pathPattern") String pathPattern, @Param("isActive") Boolean isActive);

    /**
     * 查询预估学习时长范围内的技能
     */
    @Query("SELECT s FROM LearningSkillCatalog s WHERE s.estimatedHours BETWEEN :minHours AND :maxHours AND s.isActive = :isActive ORDER BY s.estimatedHours")
    List<LearningSkillCatalog> findByEstimatedHoursRange(@Param("minHours") Integer minHours, @Param("maxHours") Integer maxHours, @Param("isActive") Boolean isActive);

    /**
     * 获取下一个排序序号
     */
    @Query("SELECT COALESCE(MAX(s.sortOrder), 0) + 1 FROM LearningSkillCatalog s WHERE s.parentCode = :parentCode")
    Integer getNextSortOrder(@Param("parentCode") String parentCode);

    /**
     * 获取同级别下一个排序序号
     */
    @Query("SELECT COALESCE(MAX(s.sortOrder), 0) + 1 FROM LearningSkillCatalog s WHERE s.level = :level AND s.parentCode = :parentCode")
    Integer getNextSortOrderByLevel(@Param("level") Integer level, @Param("parentCode") String parentCode);
}
