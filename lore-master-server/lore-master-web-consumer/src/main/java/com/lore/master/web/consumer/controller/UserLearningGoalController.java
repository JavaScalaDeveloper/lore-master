package com.lore.master.web.consumer.controller;

import com.lore.master.common.annotation.RequireLogin;
import com.lore.master.common.result.Result;
import com.lore.master.data.dto.UserLearningGoalRequest;
import com.lore.master.data.vo.UserLearningGoalResponse;
import com.lore.master.service.consumer.ConsumerUserLearningGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * C端用户学习目标控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user/learning-goal")
@RequiredArgsConstructor
public class UserLearningGoalController {

    private final ConsumerUserLearningGoalService userLearningGoalService;

    /**
     * 保存用户学习目标
     * 同一个用户可以保存多个不同目标，但用户-目标是唯一的
     */
    @PostMapping("/saveLearningGoal")
    @RequireLogin
    public Result<UserLearningGoalResponse> saveLearningGoal(@Valid @RequestBody UserLearningGoalRequest request) {
        try {
            UserLearningGoalResponse response = userLearningGoalService.saveLearningGoal(request);
            return Result.success("保存学习目标成功", response);
        } catch (IllegalArgumentException e) {
            log.warn("C端用户保存学习目标参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("C端用户保存学习目标失败", e);
            return Result.error("保存学习目标失败，请稍后重试");
        }
    }

    /**
     * 获取用户当前学习目标
     */
    @GetMapping("/current")
    @RequireLogin
    public Result<UserLearningGoalResponse> getCurrentLearningGoal() {
        try {
            UserLearningGoalResponse response = userLearningGoalService.getCurrentLearningGoal();
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取用户当前学习目标失败", e);
            return Result.error("获取学习目标失败，请稍后重试");
        }
    }

    /**
     * 获取用户所有学习目标
     */
    @GetMapping("/list")
    @RequireLogin
    public Result<java.util.List<UserLearningGoalResponse>> getUserLearningGoals() {
        try {
            java.util.List<UserLearningGoalResponse> response = userLearningGoalService.getUserLearningGoals();
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取用户学习目标列表失败", e);
            return Result.error("获取学习目标列表失败，请稍后重试");
        }
    }

    /**
     * 更新学习目标状态
     */
    @PutMapping("/{goalId}/status")
    @RequireLogin
    public Result<Boolean> updateLearningGoalStatus(@PathVariable Long goalId, @RequestParam String status) {
        try {
            boolean success = userLearningGoalService.updateLearningGoalStatus(goalId, status);
            return success ? Result.success("更新学习目标状态成功", true) : Result.error("更新学习目标状态失败");
        } catch (Exception e) {
            log.error("更新学习目标状态失败", e);
            return Result.error("更新学习目标状态失败，请稍后重试");
        }
    }
}