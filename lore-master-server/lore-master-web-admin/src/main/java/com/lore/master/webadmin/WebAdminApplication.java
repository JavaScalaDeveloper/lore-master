package com.lore.master.webadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 管理端应用启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.lore.master.webadmin",
    "com.lore.master.service",
    "com.lore.master.data",
    "com.lore.master.common"
})
@EnableJpaRepositories(basePackages = "com.lore.master.data.repository")
@EntityScan(basePackages = "com.lore.master.data.entity")
public class WebAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebAdminApplication.class, args);
    }
}