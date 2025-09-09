package com.lore.master.data.repository.business;

import com.lore.master.data.entity.business.BusinessCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 课程数据访问层
 */
@Repository
public interface BusinessCourseRepository extends JpaRepository<BusinessCourse, Long> {

    /**
     * 根据课程编码查找课程
     */
    Optional<BusinessCourse> findByCourseCodeAndIsDeletedFalse(String courseCode);

    /**
     * 检查课程编码是否存在
     */
    boolean existsByCourseCodeAndIsDeletedFalse(String courseCode);

    /**
     * 根据ID查找课程
     */
    Optional<BusinessCourse> findByIdAndIsDeletedFalse(Long id);

    /**
     * 根据状态查找课程（分页）
     */
    Page<BusinessCourse> findByStatusAndIsDeletedFalseOrderByPublishTimeDesc(String status, Pageable pageable);

    /**
     * 根据课程类型查找课程（分页）
     */
    Page<BusinessCourse> findByCourseTypeAndIsDeletedFalseOrderByPublishTimeDesc(String courseType, Pageable pageable);

    /**
     * 根据课程类型和状态查找课程（分页）
     */
    Page<BusinessCourse> findByCourseTypeAndStatusAndIsDeletedFalseOrderByPublishTimeDesc(
            String courseType, String status, Pageable pageable);

    /**
     * 根据难度等级查找课程（分页）
     */
    @Query("SELECT c FROM BusinessCourse c WHERE c.isDeleted = false AND " +
           "(c.difficultyLevel = :level OR c.difficultyLevels LIKE %:level%) " +
           "ORDER BY c.publishTime DESC")
    Page<BusinessCourse> findByDifficultyLevel(@Param("level") String level, Pageable pageable);



    /**
     * 根据父课程ID查找子课程
     */
    List<BusinessCourse> findByParentCourseIdAndIsDeletedFalseOrderBySortOrderAscCreatedTimeAsc(Long parentCourseId);

    /**
     * 根据父课程ID查找子课程（用于删除检查）
     */
    List<BusinessCourse> findByParentCourseIdAndIsDeletedFalse(Long parentCourseId);

    /**
     * 根据作者查找课程（分页）
     */
    Page<BusinessCourse> findByAuthorAndIsDeletedFalseOrderByPublishTimeDesc(String author, Pageable pageable);

    /**
     * 全文搜索课程（标题、描述、标签）
     */
    @Query("SELECT c FROM BusinessCourse c WHERE c.isDeleted = false AND c.status = 'PUBLISHED' AND " +
           "(c.title LIKE %:keyword% OR c.description LIKE %:keyword% OR " +
           "c.tags LIKE %:keyword%) " +
           "ORDER BY c.viewCount DESC, c.publishTime DESC")
    Page<BusinessCourse> searchCourses(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查找热门课程（按观看次数排序）
     */
    Page<BusinessCourse> findByStatusAndIsDeletedFalseOrderByViewCountDescPublishTimeDesc(
            String status, Pageable pageable);



    /**
     * 统计各难度等级的课程数量
     */
    @Query("SELECT COUNT(c) FROM BusinessCourse c WHERE c.isDeleted = false AND c.status = 'PUBLISHED' AND " +
           "(c.difficultyLevel = :level OR c.difficultyLevels LIKE %:level%)")
    Long countByDifficultyLevel(@Param("level") String level);

    /**
     * 统计各课程类型的数量
     */
    Long countByCourseTypeAndStatusAndIsDeletedFalse(String courseType, String status);

    /**
     * 统计指定状态的课程总数
     */
    Long countByStatusAndIsDeletedFalse(String status);

    /**
     * 统计作者的课程数量
     */
    Long countByAuthorAndStatusAndIsDeletedFalse(String author, String status);



    /**
     * 查找合集课程
     */
    Page<BusinessCourse> findByCourseTypeAndStatusAndIsDeletedFalseOrderByCreatedTimeDesc(
            String courseType, String status, Pageable pageable);

    /**
     * 复合条件查询课程
     */
    @Query("SELECT c FROM BusinessCourse c WHERE c.isDeleted = false " +
           "AND (:courseType IS NULL OR c.courseType = :courseType) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:difficultyLevel IS NULL OR c.difficultyLevel = :difficultyLevel OR c.difficultyLevels LIKE %:difficultyLevel%) " +
           "AND (:author IS NULL OR c.author = :author) " +
           "ORDER BY c.publishTime DESC")
    Page<BusinessCourse> findCoursesWithConditions(
            @Param("courseType") String courseType,
            @Param("status") String status,
            @Param("difficultyLevel") String difficultyLevel,
            @Param("author") String author,
            Pageable pageable);
}
