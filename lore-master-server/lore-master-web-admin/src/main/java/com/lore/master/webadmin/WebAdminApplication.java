package com.lore.master.webadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {"com.lore.master.webadmin", "com.lore.master.service", "com.lore.master.data"})
@EnableJpaRepositories(basePackages = "com.lore.master.data.repository")
@EntityScan(basePackages = "com.lore.master.data.entity")
public class WebAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebAdminApplication.class, args);
    }
} 