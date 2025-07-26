package com.lore.master.webadmin.controller;

import com.lore.master.common.result.Result;
import com.lore.master.data.entity.User;
import com.lore.master.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 普通用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 分页查询用户
     */
    @PostMapping("/page")
    public Result<Page<User>> getUserPage(@RequestBody Map<String, Object> params) {
        try {
            // 分页参数
            int current = (int) params.getOrDefault("current", 1);
            int size = (int) params.getOrDefault("size", 10);
            String sortField = (String) params.getOrDefault("sortField", "createTime");
            String sortOrder = (String) params.getOrDefault("sortOrder", "desc");
            
            // 创建排序对象
            Sort sort = Sort.by(
                "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortField
            );
            Pageable pageable = PageRequest.of(current - 1, size, sort);
            
            // 查询数据
            Page<User> result = userService.getUserPage(pageable, params);
            
            // 清除密码字段
            result.getContent().forEach(user -> user.setPassword(null));
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getById(id);
            if (user != null) {
                user.setPassword(null); // 清除密码字段
            }
            return Result.success(user);
        } catch (Exception e) {
            log.error("查询用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    public Result<Boolean> createUser(@Valid @RequestBody User user) {
        try {
            boolean success = userService.createUser(user);
            return success ? Result.success(true) : Result.error("创建用户失败");
        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        try {
            user.setId(id);
            boolean success = userService.updateUser(user);
            return success ? Result.success(true) : Result.error("更新用户失败");
        } catch (Exception e) {
            log.error("更新用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        try {
            boolean success = userService.deleteUser(id);
            return success ? Result.success(true) : Result.error("删除用户失败");
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteUsers(@RequestBody Long[] ids) {
        try {
            boolean success = userService.batchDeleteUsers(ids);
            return success ? Result.success(true) : Result.error("批量删除用户失败");
        } catch (Exception e) {
            log.error("批量删除用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Integer status = (Integer) params.get("status");
            boolean success = userService.updateStatus(id, status);
            return success ? Result.success(true) : Result.error("更新用户状态失败");
        } catch (Exception e) {
            log.error("更新用户状态失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/password/reset")
    public Result<Boolean> resetUserPassword(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            String newPassword = (String) params.get("newPassword");
            boolean success = userService.resetPassword(id, newPassword);
            return success ? Result.success(true) : Result.error("重置密码失败");
        } catch (Exception e) {
            log.error("重置用户密码失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> statistics = userService.getUserStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取用户统计信息失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
