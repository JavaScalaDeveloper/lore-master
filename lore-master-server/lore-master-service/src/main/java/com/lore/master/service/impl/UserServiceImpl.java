package com.lore.master.service.impl;

import com.lore.master.data.entity.User;
import com.lore.master.data.repository.UserRepository;
import com.lore.master.data.vo.UserVO;
import com.lore.master.common.web.Result;
import com.lore.master.data.dto.UserPageRequest;
import com.lore.master.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void addUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Result<Page<UserVO>> getUserPage(UserPageRequest req) {
        Pageable pageable = PageRequest.of(req.getPage() - 1, req.getPageSize());
        Page<User> userPage;
        if (req.getUsername() == null || req.getUsername().isEmpty()) {
            userPage = userRepository.findAll(pageable);
        } else {
            userPage = userRepository.findByUsernameContaining(req.getUsername(), pageable);
        }
        List<UserVO> voList = userPage.getContent().stream()
                .map(u -> new UserVO(u.getId(), u.getUsername()))
                .collect(Collectors.toList());
        Page<UserVO> voPage = new PageImpl<>(voList, pageable, userPage.getTotalElements());
        return Result.ok(voPage, userPage.getTotalElements());
    }
} 