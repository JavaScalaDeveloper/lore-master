package com.lore.master.web.admin;

import com.lore.master.data.config.*;
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
        "com.lore.master.service.*",
        "com.lore.master.common"
})
@Import({DataSourceConfig.class
        , AdminRepositoryConfig.class
        , ConsumerRepositoryConfig.class
        , BusinessRepositoryConfig.class
        , StorageRepositoryConfig.class
})
public class WebAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebAdminApplication.class, args);
    }
}