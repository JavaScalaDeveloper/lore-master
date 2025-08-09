package com.lore.master.web.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import com.lore.master.data.config.DataSourceConfig;
import com.lore.master.data.config.BusinessRepositoryConfig;
import com.lore.master.data.config.StorageRepositoryConfig;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {
    "com.lore.master.web.business",
    "com.lore.master.service.business",
    "com.lore.master.service.middleware",
    "com.lore.master.common"
})
@Import({DataSourceConfig.class, BusinessRepositoryConfig.class, StorageRepositoryConfig.class})
public class WebBusinessApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebBusinessApplication.class, args);
    }
}