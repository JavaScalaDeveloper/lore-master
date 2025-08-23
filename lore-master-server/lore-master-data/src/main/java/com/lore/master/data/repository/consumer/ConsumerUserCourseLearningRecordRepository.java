package com.lore.master.data.repository.consumer;

import com.lore.master.data.entity.consumer.ConsumerUserCourseLearningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 用户课程学习记录Repository
 */
@Repository
public interface ConsumerUserCourseLearningRecordRepository extends JpaRepository<ConsumerUserCourseLearningRecord, Long> {

    /**
     * 根据用户ID和课程编码查找记录
     */
    Optional<ConsumerUserCourseLearningRecord> findByUserIdAndCourseCode(String userId, String courseCode);

    /**
     * 根据用户ID和课程编码查找所有学习记录
     */
    List<ConsumerUserCourseLearningRecord> findByUserIdAndCourseCodeOrderByLearningDateDesc(String userId, String courseCode);

    /**
     * 根据用户ID查找所有学习记录
     */
    List<ConsumerUserCourseLearningRecord> findByUserIdOrderByLearningDateDesc(String userId);

    /**
     * 统计用户的学习天数
     */
    @Query("SELECT COUNT(DISTINCT r.learningDate) FROM ConsumerUserCourseLearningRecord r WHERE r.userId = :userId")
    Long countDistinctLearningDaysByUserId(@Param("userId") String userId);

    /**
     * 统计用户学习的课程数量
     */
    @Query("SELECT COUNT(DISTINCT r.courseCode) FROM ConsumerUserCourseLearningRecord r WHERE r.userId = :userId")
    Long countDistinctCoursesByUserId(@Param("userId") String userId);

    /**
     * 获取用户最近的学习记录
     */
    List<ConsumerUserCourseLearningRecord> findTop10ByUserIdOrderByCreatedTimeDesc(String userId);

    /**
     * 根据用户ID和学习日期查找记录
     */
    List<ConsumerUserCourseLearningRecord> findByUserIdAndLearningDate(String userId, LocalDate learningDate);
}
