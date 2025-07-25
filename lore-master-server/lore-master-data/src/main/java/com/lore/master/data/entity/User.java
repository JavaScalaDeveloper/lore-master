package com.lore.master.data.entity;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "admin_user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
} 