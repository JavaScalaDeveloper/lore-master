package com.lore.master.webadmin.controller;

import com.lore.master.data.entity.User;
import com.lore.master.data.vo.UserVO;
import com.lore.master.data.dto.LoginRequest;
import com.lore.master.data.dto.UserPageRequest;
import com.lore.master.service.UserService;
import com.lore.master.common.web.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public Result<Map<String, String>> login(@RequestBody LoginRequest params) {
        String username = params.getUsername();
        String passwordHash = params.getPassword(); // 前端多重 hash
        User user = userService.getByUsername(username);
        if (user != null && user.getPassword() != null && com.lore.master.common.util.HashUtil.multiHash(user.getPassword()).equals(passwordHash)) {
            // 生成简单token，实际可用JWT
            String token = username + ":loremaster2024";
            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            return Result.ok(data);
        } else {
            return Result.fail("用户名或密码错误");
        }
    }

    @PostMapping("/users/page")
    public Result<Page<UserVO>> getUsers(@RequestBody UserPageRequest req) {
        return userService.getUserPage(req);
    }
} 