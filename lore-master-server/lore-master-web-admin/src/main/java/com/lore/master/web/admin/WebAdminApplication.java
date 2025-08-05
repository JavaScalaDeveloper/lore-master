package com.lore.master.web.admin;

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
        "com.lore.master.web.admin",
        "com.lore.master.service.admin",
        "com.lore.master.data",
        "com.lore.master.common"
})
public class WebAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebAdminApplication.class, args);
    }
}