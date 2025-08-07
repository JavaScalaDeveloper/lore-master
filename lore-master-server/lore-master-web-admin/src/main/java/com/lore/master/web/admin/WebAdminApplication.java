package com.lore.master.web.admin;

import com.lore.master.data.config.AdminRepositoryConfig;
import com.lore.master.data.config.DataSourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 管理端应用启动类
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {
        "com.lore.master.web.admin",
        "com.lore.master.service.admin",
        "com.lore.master.common"
})
@Import({DataSourceConfig.class, AdminRepositoryConfig.class})
public class WebAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebAdminApplication.class, args);
    }
}