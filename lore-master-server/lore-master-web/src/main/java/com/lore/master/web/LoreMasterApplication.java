package com.lore.master.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lore.master")
public class LoreMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoreMasterApplication.class, args);
    }

} 