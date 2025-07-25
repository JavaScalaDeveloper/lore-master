package com.lore.master.service;

import com.lore.master.data.entity.User;
import com.lore.master.data.vo.UserVO;
import com.lore.master.common.web.Result;
import com.lore.master.data.dto.UserPageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserService {
    User getByUsername(String username);
    User getUserById(Long id);
    List<User> getAllUsers();
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(Long id);
    Result<Page<UserVO>> getUserPage(UserPageRequest req);
} 