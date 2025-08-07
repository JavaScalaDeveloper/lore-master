package com.lore.master.data.repository.admin;

import com.lore.master.data.entity.admin.AdminKnowledgeMap;
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
 * 管理端知识图谱Repository
 */
@Repository
public interface AdminKnowledgeMapRepository extends JpaRepository<AdminKnowledgeMap, Long> {
    
    /**
     * 根据节点编码查询节点
     */
    Optional<AdminKnowledgeMap> findByNodeCodeAndStatus(String nodeCode, String status);
    
    /**
     * 检查节点编码是否存在
     */
    boolean existsByNodeCode(String nodeCode);
    
    /**
     * 根据父节点编码查询子节点列表
     */
    List<AdminKnowledgeMap> findByParentCodeAndStatusOrderBySortOrder(String parentCode, String status);
    
    /**
     * 根据根节点编码查询所有节点
     */
    List<AdminKnowledgeMap> findByRootCodeAndStatusOrderByLevelDepthAscSortOrderAsc(String rootCode, String status);
    
    /**
     * 查询所有根节点
     */
    List<AdminKnowledgeMap> findByNodeTypeAndStatusOrderBySortOrder(String nodeType, String status);
    
    /**
     * 根据根节点编码和层级深度查询节点
     */
    List<AdminKnowledgeMap> findByRootCodeAndLevelDepthAndStatusOrderBySortOrder(String rootCode, Integer levelDepth, String status);
    
    /**
     * 根据层级类型查询节点
     */
    List<AdminKnowledgeMap> findByRootCodeAndLevelTypeAndStatusOrderBySortOrder(String rootCode, String levelType, String status);
    
    /**
     * 根据难度等级查询节点
     */
    List<AdminKnowledgeMap> findByRootCodeAndDifficultyLevelAndStatusOrderBySortOrder(String rootCode, String difficultyLevel, String status);
    
    /**
     * 多条件分页查询
     */
    @Query("SELECT akm FROM AdminKnowledgeMap akm WHERE " +
           "(:rootCode IS NULL OR akm.rootCode = :rootCode) AND " +
           "(:nodeType IS NULL OR akm.nodeType = :nodeType) AND " +
           "(:levelDepth IS NULL OR akm.levelDepth = :levelDepth) AND " +
           "(:levelType IS NULL OR akm.levelType = :levelType) AND " +
           "(:difficultyLevel IS NULL OR akm.difficultyLevel = :difficultyLevel) AND " +
           "(:status IS NULL OR akm.status = :status) AND " +
           "(:keyword IS NULL OR akm.nodeName LIKE %:keyword% OR akm.description LIKE %:keyword%) " +
           "ORDER BY akm.rootCode, akm.levelDepth, akm.sortOrder")
    Page<AdminKnowledgeMap> findByConditions(@Param("rootCode") String rootCode,
                                           @Param("nodeType") String nodeType,
                                           @Param("levelDepth") Integer levelDepth,
                                           @Param("levelType") String levelType,
                                           @Param("difficultyLevel") String difficultyLevel,
                                           @Param("status") String status,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);
    
    /**
     * 批量更新排序序号
     */
    @Modifying
    @Query("UPDATE AdminKnowledgeMap akm SET akm.sortOrder = :sortOrder, akm.updatedBy = :updatedBy, akm.updatedTime = CURRENT_TIMESTAMP WHERE akm.nodeCode = :nodeCode")
    int updateSortOrder(@Param("nodeCode") String nodeCode, 
                       @Param("sortOrder") Integer sortOrder, 
                       @Param("updatedBy") String updatedBy);
    
    /**
     * 更新节点路径和层级深度
     */
    @Modifying
    @Query("UPDATE AdminKnowledgeMap akm SET akm.nodePath = :nodePath, akm.levelDepth = :levelDepth, akm.updatedBy = :updatedBy, akm.updatedTime = CURRENT_TIMESTAMP WHERE akm.nodeCode = :nodeCode")
    int updateNodePathAndLevel(@Param("nodeCode") String nodeCode,
                              @Param("nodePath") String nodePath,
                              @Param("levelDepth") Integer levelDepth,
                              @Param("updatedBy") String updatedBy);
    
    /**
     * 软删除节点
     */
    @Modifying
    @Query("UPDATE AdminKnowledgeMap akm SET akm.status = 'INACTIVE', akm.updatedBy = :updatedBy, akm.updatedTime = CURRENT_TIMESTAMP WHERE akm.nodeCode = :nodeCode")
    int softDeleteByNodeCode(@Param("nodeCode") String nodeCode, @Param("updatedBy") String updatedBy);
    
    /**
     * 统计子节点数量
     */
    @Query("SELECT COUNT(akm) FROM AdminKnowledgeMap akm WHERE akm.parentCode = :parentCode AND akm.status = :status")
    long countByParentCodeAndStatus(@Param("parentCode") String parentCode, @Param("status") String status);
    
    /**
     * 查询节点的所有子孙节点编码
     */
    @Query("SELECT akm.nodeCode FROM AdminKnowledgeMap akm WHERE akm.nodePath LIKE CONCAT(:nodePath, '/%') AND akm.status = :status")
    List<String> findDescendantNodeCodes(@Param("nodePath") String nodePath, @Param("status") String status);
    
    /**
     * 根据节点路径前缀查询节点
     */
    @Query("SELECT akm FROM AdminKnowledgeMap akm WHERE akm.nodePath LIKE CONCAT(:pathPrefix, '%') AND akm.status = :status ORDER BY akm.levelDepth, akm.sortOrder")
    List<AdminKnowledgeMap> findByNodePathStartingWith(@Param("pathPrefix") String pathPrefix, @Param("status") String status);
    
    /**
     * 查询指定根节点下的统计信息
     */
    @Query("SELECT akm.nodeType, COUNT(akm) FROM AdminKnowledgeMap akm WHERE akm.rootCode = :rootCode AND akm.status = :status GROUP BY akm.nodeType")
    List<Object[]> countByNodeTypeAndRootCode(@Param("rootCode") String rootCode, @Param("status") String status);
    
    /**
     * 查询指定难度等级的节点数量
     */
    @Query("SELECT COUNT(akm) FROM AdminKnowledgeMap akm WHERE akm.rootCode = :rootCode AND akm.difficultyLevel = :difficultyLevel AND akm.status = :status")
    long countByDifficultyLevel(@Param("rootCode") String rootCode, @Param("difficultyLevel") String difficultyLevel, @Param("status") String status);
    
    /**
     * 查询最大排序序号
     */
    @Query("SELECT COALESCE(MAX(akm.sortOrder), 0) FROM AdminKnowledgeMap akm WHERE akm.parentCode = :parentCode")
    Integer findMaxSortOrderByParentCode(@Param("parentCode") String parentCode);
    
    /**
     * 查询最大排序序号（根节点）
     */
    @Query("SELECT COALESCE(MAX(akm.sortOrder), 0) FROM AdminKnowledgeMap akm WHERE akm.parentCode IS NULL AND akm.nodeType = 'ROOT'")
    Integer findMaxSortOrderForRootNodes();
}
