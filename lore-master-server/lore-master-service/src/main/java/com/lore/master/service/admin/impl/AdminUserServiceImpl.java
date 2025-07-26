package com.lore.master.service.admin.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.common.util.JwtUtil;
import com.lore.master.common.util.CryptoUtil;
import com.lore.master.data.dto.LoginRequest;
import com.lore.master.data.entity.admin.AdminUser;
import com.lore.master.data.repository.admin.AdminUserRepository;
import com.lore.master.data.vo.LoginResponse;
import com.lore.master.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 管理员用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    
    private final AdminUserRepository adminUserRepository;
    
    @Override
    public LoginResponse login(LoginRequest loginRequest, String clientIp) {
        String username = loginRequest.getUsername();
        String encryptedPassword = loginRequest.getPassword(); // 前端传输的加密密码

        // 查询用户
        Optional<AdminUser> userOpt = adminUserRepository.findByUsernameAndStatus(username, 1);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }

        AdminUser user = userOpt.get();

        // 验证密码：解密前端传输的密码，与数据库明文密码比较
        if (!CryptoUtil.verifyPassword(encryptedPassword, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 更新登录信息
        user.setLoginCount(user.getLoginCount() + 1);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        adminUserRepository.save(user);
        
        // 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(24 * 60 * 60 * 1000L); // 24小时
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setEmail(user.getEmail());
        userInfo.setRole(user.getRole());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        
        response.setUserInfo(userInfo);
        
        log.info("用户登录成功: username={}, ip={}", username, clientIp);
        return response;
    }
    
    @Override
    public Page<AdminUser> getAdminUserPage(Pageable pageable, Map<String, Object> params) {
        String username = (String) params.get("username");
        String realName = (String) params.get("realName");
        String email = (String) params.get("email");
        String role = (String) params.get("role");
        Integer status = (Integer) params.get("status");
        
        return adminUserRepository.findByConditions(username, realName, email, role, status, pageable);
    }
    
    @Override
    public AdminUser getById(Long id) {
        return adminUserRepository.findById(id).orElse(null);
    }
    
    @Override
    @Transactional
    public boolean createAdminUser(AdminUser adminUser) {
        try {
            // 验证用户名唯一性
            if (adminUserRepository.existsByUsernameAndIdNot(adminUser.getUsername(), 0L)) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 验证邮箱唯一性
            if (StrUtil.isNotBlank(adminUser.getEmail()) && 
                adminUserRepository.existsByEmailAndIdNot(adminUser.getEmail(), 0L)) {
                throw new RuntimeException("邮箱已存在");
            }
            
            // 验证手机号唯一性
            if (StrUtil.isNotBlank(adminUser.getPhone()) && 
                adminUserRepository.existsByPhoneAndIdNot(adminUser.getPhone(), 0L)) {
                throw new RuntimeException("手机号已存在");
            }
            
            // 设置默认值
            if (adminUser.getStatus() == null) {
                adminUser.setStatus(1);
            }
            if (StrUtil.isBlank(adminUser.getRole())) {
                adminUser.setRole("operator");
            }
            if (adminUser.getLoginCount() == null) {
                adminUser.setLoginCount(0);
            }
            
            // 密码明文存储（前端传输时会加密）
            if (StrUtil.isBlank(adminUser.getPassword())) {
                adminUser.setPassword("123456"); // 默认密码
            }
            // 如果前端传输的是加密密码，需要先解密
            if (adminUser.getPassword().length() > 50) { // 简单判断是否为加密密码
                try {
                    adminUser.setPassword(CryptoUtil.decryptPasswordFromTransmission(adminUser.getPassword()));
                } catch (Exception e) {
                    // 如果解密失败，说明可能是明文密码，直接使用
                    log.warn("密码解密失败，使用原始值: {}", e.getMessage());
                }
            }
            
            adminUserRepository.save(adminUser);
            return true;
        } catch (Exception e) {
            log.error("创建管理员用户失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateAdminUser(AdminUser adminUser) {
        try {
            AdminUser existingUser = adminUserRepository.findById(adminUser.getId()).orElse(null);
            if (existingUser == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 验证用户名唯一性
            if (!existingUser.getUsername().equals(adminUser.getUsername()) &&
                adminUserRepository.existsByUsernameAndIdNot(adminUser.getUsername(), adminUser.getId())) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 验证邮箱唯一性
            if (StrUtil.isNotBlank(adminUser.getEmail()) && 
                !Objects.equals(existingUser.getEmail(), adminUser.getEmail()) &&
                adminUserRepository.existsByEmailAndIdNot(adminUser.getEmail(), adminUser.getId())) {
                throw new RuntimeException("邮箱已存在");
            }
            
            // 验证手机号唯一性
            if (StrUtil.isNotBlank(adminUser.getPhone()) && 
                !Objects.equals(existingUser.getPhone(), adminUser.getPhone()) &&
                adminUserRepository.existsByPhoneAndIdNot(adminUser.getPhone(), adminUser.getId())) {
                throw new RuntimeException("手机号已存在");
            }
            
            // 更新字段（不更新密码、创建时间等敏感字段）
            existingUser.setUsername(adminUser.getUsername());
            existingUser.setRealName(adminUser.getRealName());
            existingUser.setEmail(adminUser.getEmail());
            existingUser.setPhone(adminUser.getPhone());
            existingUser.setAvatarUrl(adminUser.getAvatarUrl());
            existingUser.setRole(adminUser.getRole());
            existingUser.setStatus(adminUser.getStatus());
            existingUser.setRemark(adminUser.getRemark());
            
            adminUserRepository.save(existingUser);
            return true;
        } catch (Exception e) {
            log.error("更新管理员用户失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean deleteAdminUser(Long id) {
        try {
            AdminUser user = adminUserRepository.findById(id).orElse(null);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 不能删除超级管理员
            if ("super_admin".equals(user.getRole())) {
                throw new RuntimeException("不能删除超级管理员");
            }
            
            adminUserRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("删除管理员用户失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean batchDeleteAdminUsers(Long[] ids) {
        try {
            for (Long id : ids) {
                deleteAdminUser(id);
            }
            return true;
        } catch (Exception e) {
            log.error("批量删除管理员用户失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        try {
            AdminUser user = adminUserRepository.findById(id).orElse(null);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            user.setStatus(status);
            adminUserRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("更新用户状态失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean resetPassword(Long id, String newPassword) {
        try {
            AdminUser user = adminUserRepository.findById(id).orElse(null);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 如果是加密密码，先解密
            String plainPassword = newPassword;
            if (newPassword.length() > 50) { // 简单判断是否为加密密码
                try {
                    plainPassword = CryptoUtil.decryptPasswordFromTransmission(newPassword);
                } catch (Exception e) {
                    log.warn("密码解密失败，使用原始值: {}", e.getMessage());
                }
            }

            user.setPassword(plainPassword); // 明文存储
            adminUserRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("重置密码失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            AdminUser user = adminUserRepository.findById(userId).orElse(null);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 验证原密码（前端传输的是加密密码）
            if (!CryptoUtil.verifyPassword(oldPassword, user.getPassword())) {
                throw new RuntimeException("原密码错误");
            }

            // 解密新密码并明文存储
            String plainNewPassword = newPassword;
            if (newPassword.length() > 50) { // 简单判断是否为加密密码
                try {
                    plainNewPassword = CryptoUtil.decryptPasswordFromTransmission(newPassword);
                } catch (Exception e) {
                    log.warn("新密码解密失败，使用原始值: {}", e.getMessage());
                }
            }

            user.setPassword(plainNewPassword); // 明文存储
            adminUserRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        try {
            Object[] result = adminUserRepository.getStatistics();
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", result[0]);
            statistics.put("activeCount", result[1]);
            statistics.put("inactiveCount", result[2]);
            statistics.put("todayCount", result[3]);
            return statistics;
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * 初始化默认管理员账户
     */
    @PostConstruct
    public void initDefaultAdmin() {
        try {
            // 检查是否已存在管理员账户
            Optional<AdminUser> existingAdmin = adminUserRepository.findByUsernameAndStatus("admin", 1);
            if (existingAdmin.isPresent()) {
                log.info("默认管理员账户已存在，跳过初始化");
                return;
            }
            
            // 创建默认管理员账户（明文密码存储）
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // 明文存储
            admin.setRealName("系统管理员");
            admin.setEmail("admin@loremaster.com");
            admin.setRole("super_admin");
            admin.setStatus(1);
            admin.setLoginCount(0);
            admin.setRemark("系统默认管理员账户");
            
            adminUserRepository.save(admin);
            log.info("默认管理员账户初始化成功: username=admin, password=admin123");
        } catch (Exception e) {
            log.error("初始化默认管理员账户失败: {}", e.getMessage(), e);
        }
    }
}
