package com.lore.master.web.admin.controller;

import cn.hutool.core.util.ArrayUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.lore.master.common.result.Result;
import com.lore.master.data.entity.admin.AdminUser;
import com.lore.master.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * 管理员用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/admin-users")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final AdminUserService adminUserService;
    
    /**
     * 分页查询管理员用户
     */
    @PostMapping("/page")
    public Result<Page<AdminUser>> getAdminUserPage(@RequestBody Map<String, Object> params) {
        try {
            // 分页参数
            int current = (int) params.getOrDefault("current", 1);
            int size = (int) params.getOrDefault("size", 10);
            Pageable pageable = PageRequest.of(current - 1, size); // JPA分页从0开始

            // 查询数据
            Page<AdminUser> result = adminUserService.getAdminUserPage(pageable, params);

            // 清除密码字段
            result.getContent().forEach(user -> user.setPassword(null));

            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询管理员用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据ID查询管理员用户
     */
    @GetMapping("/{id}")
    public Result<AdminUser> getAdminUserById(@PathVariable Long id) {
        try {
            AdminUser user = adminUserService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 清除密码字段
            user.setPassword(null);
            
            return Result.success(user);
        } catch (Exception e) {
            log.error("查询管理员用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建管理员用户
     */
    @PostMapping
    public Result<String> createAdminUser(@Valid @RequestBody AdminUser adminUser, 
                                        HttpServletRequest request) {
        try {
            // 获取当前操作用户
            Long currentUserId = (Long) request.getAttribute("userId");
            log.info("用户{}创建管理员用户: {}", currentUserId, adminUser.getUsername());
            
            boolean success = adminUserService.createAdminUser(adminUser);
            if (success) {
                return Result.success("创建成功");
            } else {
                return Result.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建管理员用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新管理员用户
     */
    @PutMapping("/{id}")
    public Result<String> updateAdminUser(@PathVariable Long id, 
                                        @Valid @RequestBody AdminUser adminUser,
                                        HttpServletRequest request) {
        try {
            // 获取当前操作用户
            Long currentUserId = (Long) request.getAttribute("userId");
            log.info("用户{}更新管理员用户: {}", currentUserId, id);
            
            adminUser.setId(id);
            boolean success = adminUserService.updateAdminUser(adminUser);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新管理员用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除管理员用户
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteAdminUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 获取当前操作用户
            Long currentUserId = (Long) request.getAttribute("userId");
            
            // 不能删除自己
            if (currentUserId.equals(id)) {
                return Result.error("不能删除自己");
            }
            
            log.info("用户{}删除管理员用户: {}", currentUserId, id);
            
            boolean success = adminUserService.deleteAdminUser(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除管理员用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量删除管理员用户
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteAdminUsers(@RequestBody Long[] ids, HttpServletRequest request) {
        try {
            if (ArrayUtil.isEmpty(ids)) {
                return Result.error("请选择要删除的用户");
            }
            
            // 获取当前操作用户
            Long currentUserId = (Long) request.getAttribute("userId");
            
            // 检查是否包含自己
            for (Long id : ids) {
                if (currentUserId.equals(id)) {
                    return Result.error("不能删除自己");
                }
            }
            
            log.info("用户{}批量删除管理员用户: {}", currentUserId, ArrayUtil.join(ids, ","));
            
            boolean success = adminUserService.batchDeleteAdminUsers(ids);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除管理员用户失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, 
                                     @RequestBody Map<String, Object> params,
                                     HttpServletRequest request) {
        try {
            Integer status = (Integer) params.get("status");
            if (status == null) {
                return Result.error("状态参数不能为空");
            }
            
            // 获取当前操作用户
            Long currentUserId = (Long) request.getAttribute("userId");
            
            // 不能禁用自己
            if (currentUserId.equals(id) && status == 0) {
                return Result.error("不能禁用自己");
            }
            
            log.info("用户{}更新用户{}状态为: {}", currentUserId, id, status);
            
            boolean success = adminUserService.updateStatus(id, status);
            if (success) {
                return Result.success("状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id, 
                                      @RequestBody Map<String, Object> params,
                                      HttpServletRequest request) {
        try {
            String newPassword = (String) params.get("newPassword");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                newPassword = "123456"; // 默认密码
            }
            
            // 获取当前操作用户
            Long currentUserId = (Long) request.getAttribute("userId");
            log.info("用户{}重置用户{}密码", currentUserId, id);
            
            boolean success = adminUserService.resetPassword(id, newPassword);
            if (success) {
                return Result.success("密码重置成功");
            } else {
                return Result.error("密码重置失败");
            }
        } catch (Exception e) {
            log.error("重置密码失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, Object> params,
                                       HttpServletRequest request) {
        try {
            String oldPassword = (String) params.get("oldPassword");
            String newPassword = (String) params.get("newPassword");
            
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return Result.error("原密码不能为空");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }
            
            // 获取当前用户ID
            Long currentUserId = (Long) request.getAttribute("userId");
            
            boolean success = adminUserService.changePassword(currentUserId, oldPassword, newPassword);
            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("密码修改失败");
            }
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = adminUserService.getStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
