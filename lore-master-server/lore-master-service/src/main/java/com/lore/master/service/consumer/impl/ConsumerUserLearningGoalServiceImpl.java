package com.lore.master.service.consumer.impl;

import com.lore.master.common.context.UserContext;
import com.lore.master.common.exception.BusinessException;
import com.lore.master.common.result.ResultCode;
import com.lore.master.data.dto.UserLearningGoalRequest;
import com.lore.master.data.entity.consumer.ConsumerUser;
import com.lore.master.data.entity.consumer.UserLearningGoal;
import com.lore.master.data.entity.consumer.UserLearningGoalStatusEnum;
import com.lore.master.data.repository.consumer.ConsumerUserRepository;
import com.lore.master.data.repository.consumer.UserLearningGoalRepository;
import com.lore.master.data.vo.UserLearningGoalResponse;
import com.lore.master.service.consumer.ConsumerUserLearningGoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * C端用户学习目标服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerUserLearningGoalServiceImpl implements ConsumerUserLearningGoalService {

    private final UserLearningGoalRepository userLearningGoalRepository;
    private final ConsumerUserRepository consumerUserRepository;

    @Override
    @Transactional
    public UserLearningGoalResponse saveLearningGoal(UserLearningGoalRequest request) {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.NOT_LOGIN, "用户未登录");
        }

        // 检查用户是否存在
        Optional<ConsumerUser> userOptional = consumerUserRepository.findByUserId(userId);
        if (userOptional.isEmpty()) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "用户不存在");
        }

        // 检查学习目标是否已存在
        Optional<UserLearningGoal> existingGoal = userLearningGoalRepository
                .findByUserIdAndSkillCode(userId, request.getSkillCode());

        UserLearningGoal goal;
        if (existingGoal.isPresent()) {
            // 更新现有目标
            goal = existingGoal.get();
            goal.setTargetLevel(request.getTargetLevel());
            goal.setPriority(request.getPriority());
            goal.setNotes(request.getNotes());
            if (request.getStartDate() != null) {
                goal.setStartDate(request.getStartDate());
            }
            if (request.getTargetDate() != null) {
                goal.setTargetDate(request.getTargetDate());
            }
        } else {
            // 创建新目标
            goal = new UserLearningGoal();
            goal.setUserId(userId);
            goal.setSkillCode(request.getSkillCode());
            goal.setSkillName(request.getSkillName());
            goal.setSkillPath(request.getSkillPath());
            goal.setTargetLevel(request.getTargetLevel());
            goal.setCurrentProgress(BigDecimal.valueOf(0.0));
            goal.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
            goal.setTargetDate(request.getTargetDate());
            goal.setStatus(UserLearningGoalStatusEnum.ACTIVE.name());
            goal.setPriority(request.getPriority() != null ? request.getPriority() : 1);
            goal.setNotes(request.getNotes());
        }

        // 保存目标
        UserLearningGoal savedGoal = userLearningGoalRepository.save(goal);

        // 设置为当前目标
        setCurrentLearningGoal(savedGoal.getSkillCode());

        // 转换为响应对象
        return convertToResponse(savedGoal);
    }

    @Override
    public UserLearningGoalResponse getCurrentLearningGoal() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.NOT_LOGIN, "用户未登录");
        }

        // 查询用户最新添加或设置的学习目标
        Optional<UserLearningGoal> goalOptional = userLearningGoalRepository
                .findFirstByUserIdOrderByUpdatedTimeDesc(userId);

        return goalOptional.map(this::convertToResponse).orElse(null);
    }

    @Override
    public List<UserLearningGoalResponse> getUserLearningGoals() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.NOT_LOGIN, "用户未登录");
        }

        List<UserLearningGoal> goals = userLearningGoalRepository.findByUserIdOrderByUpdatedTimeDesc(userId);
        return goals.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean updateLearningGoalStatus(Long goalId, String status) {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.NOT_LOGIN, "用户未登录");
        }

        // 检查状态是否有效
        boolean isValidStatus = false;
        for (UserLearningGoalStatusEnum enumStatus : UserLearningGoalStatusEnum.values()) {
            if (enumStatus.name().equals(status)) {
                isValidStatus = true;
                break;
            }
        }

        if (!isValidStatus) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的学习目标状态");
        }

        // 检查目标是否存在且属于当前用户
        Optional<UserLearningGoal> goalOptional = userLearningGoalRepository.findById(goalId);
        if (goalOptional.isEmpty() || !Objects.equals(goalOptional.get().getUserId(), userId)) {
            throw new BusinessException(ResultCode.LEARNING_GOAL_NOT_FOUND, "学习目标不存在或不属于当前用户");
        }

        UserLearningGoal goal = goalOptional.get();
        goal.setStatus(status);
        userLearningGoalRepository.save(goal);

        return true;
    }

    @Override
    public boolean setCurrentLearningGoal(String skillCode) {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.NOT_LOGIN, "用户未登录");
        }

        // 检查目标是否存在且属于当前用户
        Optional<UserLearningGoal> goalOptional = userLearningGoalRepository
                .findByUserIdAndSkillCode(userId, skillCode);

        if (goalOptional.isEmpty()) {
            throw new BusinessException(ResultCode.LEARNING_GOAL_NOT_FOUND, "学习目标不存在");
        }

        UserLearningGoal goal = goalOptional.get();
        // 设置为活跃状态
        if (!UserLearningGoalStatusEnum.ACTIVE.name().equals(goal.getStatus())) {
            goal.setStatus(UserLearningGoalStatusEnum.ACTIVE.name());
            userLearningGoalRepository.save(goal);
        }

        return true;
    }

    /**
     * 将实体转换为响应对象
     */
    private UserLearningGoalResponse convertToResponse(UserLearningGoal goal) {
        UserLearningGoalResponse response = new UserLearningGoalResponse();
        response.setId(goal.getId());
        response.setSkillCode(goal.getSkillCode());
        response.setSkillName(goal.getSkillName());
        response.setSkillPath(goal.getSkillPath());
        response.setTargetLevel(goal.getTargetLevel());
        BigDecimal currentProgress = goal.getCurrentProgress();
        response.setCurrentProgress(currentProgress.doubleValue());
        response.setStartDate(goal.getStartDate());
        response.setTargetDate(goal.getTargetDate());
        response.setStatus(goal.getStatus());
        response.setPriority(goal.getPriority());
        response.setNotes(goal.getNotes());
        response.setCreatedTime(goal.getCreatedTime());
        response.setUpdatedTime(goal.getUpdatedTime());
        return response;
    }
}