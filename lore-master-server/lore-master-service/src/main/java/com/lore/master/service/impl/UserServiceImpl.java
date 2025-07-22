package com.lore.master.service.impl;

import com.lore.master.data.entity.User;
import com.lore.master.service.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public User getUserById(Long id) {
        System.out.println("MOCK: Getting user by id: " + id);
        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setUsername("Mock User");
        mockUser.setEmail("mock@example.com");
        return mockUser;
    }

    @Override
    public List<User> getAllUsers() {
        System.out.println("MOCK: Getting all users");
        List<User> users = new ArrayList<>();
        User mockUser1 = new User();
        mockUser1.setId(1L);
        mockUser1.setUsername("Mock User 1");
        mockUser1.setEmail("mock1@example.com");
        users.add(mockUser1);
        return users;
    }

    @Override
    public void addUser(User user) {
        System.out.println("MOCK: Adding user: " + user.toString());
    }

    @Override
    public void updateUser(User user) {
        System.out.println("MOCK: Updating user: " + user.toString());
    }

    @Override
    public void deleteUser(Long id) {
        System.out.println("MOCK: Deleting user with id: " + id);
    }
} 