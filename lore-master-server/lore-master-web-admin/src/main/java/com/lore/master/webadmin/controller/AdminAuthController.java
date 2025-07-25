package com.lore.master.webadmin.controller;

import com.lore.master.data.entity.User;
import com.lore.master.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
        String username = params.get("username");
        String passwordHash = params.get("password"); // 前端多重 hash
        Map<String, Object> result = new HashMap<>();
        User user = userService.getByUsername(username);
        if (user != null && user.getPassword() != null && multiHash(user.getPassword()).equals(passwordHash)) {
            session.setAttribute("adminUser", user);
            result.put("success", true);
            result.put("message", "登录成功");
        } else {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
        }
        return result;
    }

    // 多重 hash: md5(sha1(sha256(pwd)))
    private String multiHash(String pwd) {
        return md5(sha1(sha256(pwd)));
    }
    private String sha256(String str) {
        return hash(str, "SHA-256");
    }
    private String sha1(String str) {
        return hash(str, "SHA-1");
    }
    private String md5(String str) {
        return hash(str, "MD5");
    }
    private String hash(String str, String algorithm) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance(algorithm);
            byte[] hash = md.digest(str.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 