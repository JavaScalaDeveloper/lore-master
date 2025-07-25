package com.lore.master.data.vo;

import com.lore.master.data.entity.User;

public class UserVO extends User {
    public UserVO(Long id, String username) {
        this.setId(id);
        this.setUsername(username);
    }
    // 可扩展更多VO专属字段和方法
} 