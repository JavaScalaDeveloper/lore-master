package com.lore.master.webadmin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        Map<String, Object> result = new HashMap<>();
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lore_admin.admin_user WHERE username = ? AND password = ?",
                Integer.class, username, password
            );
            if (count != null && count > 0) {
                result.put("success", true);
                result.put("message", "登录成功");
            } else {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
            }
        } catch (EmptyResultDataAccessException e) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "系统异常: " + e.getMessage());
        }
        return result;
    }
} 