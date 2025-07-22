package com.lore.master.service;

import com.lore.master.data.entity.User;
import java.util.List;

public interface UserService {
    User getUserById(Long id);
    List<User> getAllUsers();
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(Long id);
} 