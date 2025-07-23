package com.lore.master.webbusiness.controller;

import com.lore.master.data.entity.User;
import com.lore.master.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public String addUser(@RequestBody User user) {
        userService.addUser(user);
        return "User added successfully (mocked)";
    }

    @PutMapping("/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateUser(user);
        return "User updated successfully (mocked)";
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "User deleted successfully (mocked)";
    }
} 