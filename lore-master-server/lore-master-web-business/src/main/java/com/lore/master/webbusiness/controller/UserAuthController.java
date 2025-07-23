package com.lore.master.webbusiness.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserAuthController {

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        if ("exists".equals(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("用户名已存在（mock）");
        }
        return ResponseEntity.ok("注册成功（mock）");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");
        if ("test".equals(username) && "123456".equals(password)) {
            Map<String, String> resp = new HashMap<>();
            resp.put("token", "mock-token");
            return ResponseEntity.ok(resp);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误（mock）");
    }
} 