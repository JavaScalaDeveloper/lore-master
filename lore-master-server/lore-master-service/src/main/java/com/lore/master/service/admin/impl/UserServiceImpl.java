package com.lore.master.service.admin.impl;

import cn.hutool.core.util.StrUtil;
import com.lore.master.common.util.CryptoUtil;
import com.lore.master.data.entity.User;
import com.lore.master.data.repository.UserRepository;
import com.lore.master.service.admin.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsernameAndStatus(username, 1).orElse(null);
    }
    
    @Override
    public Page<User> getUserPage(Pageable pageable, Map<String, Object> params) {
        String username = (String) params.get("username");
        String realName = (String) params.get("realName");
        String email = (String) params.get("email");
        String phone = (String) params.get("phone");
        Integer gender = (Integer) params.get("gender");
        Integer education = (Integer) params.get("education");
        Integer status = (Integer) params.get("status");
        
        return userRepository.findByConditions(username, realName, email, phone, gender, education, status, pageable);
    }
    
    @Override
    @Transactional
    public boolean createUser(User user) {
        try {
            // 验证用户名唯一性
            if (userRepository.existsByUsernameAndIdNot(user.getUsername(), 0L)) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 验证邮箱唯一性
            if (StrUtil.isNotBlank(user.getEmail()) && 
                userRepository.existsByEmailAndIdNot(user.getEmail(), 0L)) {
                throw new RuntimeException("邮箱已存在");
            }
            
            // 验证手机号唯一性
            if (StrUtil.isNotBlank(user.getPhone()) && 
                userRepository.existsByPhoneAndIdNot(user.getPhone(), 0L)) {
                throw new RuntimeException("手机号已存在");
            }
            
            // 设置默认值
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            if (user.getCurrentLevel() == null) {
                user.setCurrentLevel(1);
            }
            if (user.getTotalScore() == null) {
                user.setTotalScore(0);
            }
            if (user.getLoginCount() == null) {
                user.setLoginCount(0);
            }
            
            // 处理密码
            if (StrUtil.isBlank(user.getPassword())) {
                user.setPassword("123456"); // 默认密码
            } else if (user.getPassword().length() > 50) {
                // 如果是加密密码，先解密
                try {
                    user.setPassword(CryptoUtil.decryptPasswordFromTransmission(user.getPassword()));
                } catch (Exception e) {
                    log.warn("密码解密失败，使用原始值: {}", e.getMessage());
                }
            }
            
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateUser(User user) {
        try {
            User existingUser = userRepository.findById(user.getId()).orElse(null);
            if (existingUser == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 验证用户名唯一性
            if (!existingUser.getUsername().equals(user.getUsername()) &&
                userRepository.existsByUsernameAndIdNot(user.getUsername(), user.getId())) {
                throw new RuntimeException("用户名已存在");
            }
            
            // 验证邮箱唯一性
            if (StrUtil.isNotBlank(user.getEmail()) && 
                !Objects.equals(existingUser.getEmail(), user.getEmail()) &&
                userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId())) {
                throw new RuntimeException("邮箱已存在");
            }
            
            // 验证手机号唯一性
            if (StrUtil.isNotBlank(user.getPhone()) && 
                !Objects.equals(existingUser.getPhone(), user.getPhone()) &&
                userRepository.existsByPhoneAndIdNot(user.getPhone(), user.getId())) {
                throw new RuntimeException("手机号已存在");
            }
            
            // 更新字段（不更新密码、创建时间等敏感字段）
            existingUser.setUsername(user.getUsername());
            existingUser.setRealName(user.getRealName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            existingUser.setAvatarUrl(user.getAvatarUrl());
            existingUser.setGender(user.getGender());
            existingUser.setAge(user.getAge());
            existingUser.setEducation(user.getEducation());
            existingUser.setProfession(user.getProfession());
            existingUser.setCurrentLevel(user.getCurrentLevel());
            existingUser.setTotalScore(user.getTotalScore());
            existingUser.setStatus(user.getStatus());
            existingUser.setRemark(user.getRemark());
            
            userRepository.save(existingUser);
            return true;
        } catch (Exception e) {
            log.error("更新用户失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            userRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean batchDeleteUsers(Long[] ids) {
        try {
            for (Long id : ids) {
                deleteUser(id);
            }
            return true;
        } catch (Exception e) {
            log.error("批量删除用户失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            user.setStatus(status);
            userRepository.save(user);
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
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 如果是加密密码，先解密
            String plainPassword = newPassword;
            if (newPassword.length() > 50) {
                try {
                    plainPassword = CryptoUtil.decryptPasswordFromTransmission(newPassword);
                } catch (Exception e) {
                    log.warn("密码解密失败，使用原始值: {}", e.getMessage());
                }
            }
            
            user.setPassword(plainPassword);
            userRepository.save(user);
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
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 验证原密码（前端传输的是加密密码）
            if (!CryptoUtil.verifyPassword(oldPassword, user.getPassword())) {
                throw new RuntimeException("原密码错误");
            }
            
            // 解密新密码并明文存储
            String plainNewPassword = newPassword;
            if (newPassword.length() > 50) {
                try {
                    plainNewPassword = CryptoUtil.decryptPasswordFromTransmission(newPassword);
                } catch (Exception e) {
                    log.warn("新密码解密失败，使用原始值: {}", e.getMessage());
                }
            }
            
            user.setPassword(plainNewPassword);
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public User login(String username, String encryptedPassword, String clientIp) {
        try {
            // 查询用户
            Optional<User> userOpt = userRepository.findByUsernameAndStatus(username, 1);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("用户名或密码错误");
            }
            
            User user = userOpt.get();
            
            // 验证密码：解密前端传输的密码，与数据库明文密码比较
            if (!CryptoUtil.verifyPassword(encryptedPassword, user.getPassword())) {
                throw new RuntimeException("用户名或密码错误");
            }
            
            // 更新登录信息
            user.setLoginCount(user.getLoginCount() + 1);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(clientIp);
            userRepository.save(user);
            
            log.info("用户登录成功: username={}, ip={}", username, clientIp);
            return user;
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserStatistics() {
        try {
            Object[] result = userRepository.getUserStatistics();
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", result[0]);
            statistics.put("activeCount", result[1]);
            statistics.put("inactiveCount", result[2]);
            statistics.put("todayCount", result[3]);
            statistics.put("maleCount", result[4]);
            statistics.put("femaleCount", result[5]);
            return statistics;
        } catch (Exception e) {
            log.error("获取用户统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserLevelDistribution() {
        try {
            Object[][] result = userRepository.getUserCountByLevel();
            Map<String, Object> distribution = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            
            for (Object[] row : result) {
                Map<String, Object> item = new HashMap<>();
                item.put("level", row[0]);
                item.put("count", row[1]);
                data.add(item);
            }
            
            distribution.put("data", data);
            return distribution;
        } catch (Exception e) {
            log.error("获取用户等级分布失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getUserEducationDistribution() {
        try {
            Object[][] result = userRepository.getUserCountByEducation();
            Map<String, Object> distribution = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            
            for (Object[] row : result) {
                Map<String, Object> item = new HashMap<>();
                item.put("education", row[0]);
                item.put("count", row[1]);
                data.add(item);
            }
            
            distribution.put("data", data);
            return distribution;
        } catch (Exception e) {
            log.error("获取用户学历分布失败: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
