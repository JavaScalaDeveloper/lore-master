package com.lore.master.service.consumer;

import com.lore.master.data.dto.UserLearningGoalRequest;
import com.lore.master.data.vo.UserLearningGoalResponse;

import java.util.List;

/**
 * C端用户学习目标服务接口
 */
public interface ConsumerUserLearningGoalService {

    /**
     * 保存用户学习目标
     * 同一个用户可以保存多个不同目标，但用户-目标是唯一的
     * @param request 用户学习目标请求
     * @return 保存后的学习目标响应
     */
    UserLearningGoalResponse saveLearningGoal(UserLearningGoalRequest request);

    /**
     * 获取用户当前学习目标
     * @return 当前学习目标响应
     */
    UserLearningGoalResponse getCurrentLearningGoal();

    /**
     * 获取用户所有学习目标
     * @return 学习目标列表响应
     */
    List<UserLearningGoalResponse> getUserLearningGoals();

    /**
     * 更新学习目标状态
     * @param goalId 学习目标ID
     * @param status 状态：active-进行中，completed-已完成，paused-暂停，abandoned-放弃
     * @return 是否更新成功
     */
    boolean updateLearningGoalStatus(Long goalId, String status);

    /**
     * 设置用户当前学习目标
     * @param skillCode 技能编码
     * @return 是否设置成功
     */
    boolean setCurrentLearningGoal(String skillCode);
}