package com.lore.master.data.repository.consumer;

import com.lore.master.data.entity.consumer.UserLearningGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * 用户学习目标数据访问接口
 */
@Repository
public interface UserLearningGoalRepository extends JpaRepository<UserLearningGoal, Long> {

    /**
     * 根据用户ID和技能编码查询学习目标
     */
    Optional<UserLearningGoal> findByUserIdAndSkillCode(String userId, String skillCode);

    /**
     * 根据用户ID查询所有学习目标，并按更新时间降序排序
     */
    List<UserLearningGoal> findByUserIdOrderByUpdatedTimeDesc(String userId);

    /**
     * 查询用户最新添加或设置的学习目标
     */
    Optional<UserLearningGoal> findFirstByUserIdOrderByUpdatedTimeDesc(String userId);

    /**
     * 根据用户ID和状态查询学习目标
     */
    List<UserLearningGoal> findByUserIdAndStatusOrderByUpdatedTimeDesc(String userId, String status);

    /**
     * 删除用户的学习目标
     */
    void deleteByUserIdAndSkillCode(String userId, String skillCode);

    /**
     * 检查用户是否已经有该技能的学习目标
     */
    boolean existsByUserIdAndSkillCode(String userId, String skillCode);
}