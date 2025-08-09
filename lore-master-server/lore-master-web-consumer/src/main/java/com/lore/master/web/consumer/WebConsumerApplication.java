package com.lore.master.web.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import com.lore.master.data.config.DataSourceConfig;
import com.lore.master.data.config.ConsumerRepositoryConfig;
import com.lore.master.data.config.StorageRepositoryConfig;
import com.lore.master.data.config.AdminRepositoryConfig;

/**
 * C端用户Web应用启动类
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {
    "com.lore.master.web.consumer",
    "com.lore.master.service.consumer",
    "com.lore.master.service.admin",
    "com.lore.master.service.middleware",
    "com.lore.master.service.business",
    "com.lore.master.common"
})
@EntityScan(basePackages = {
    "com.lore.master.data.entity.admin",
    "com.lore.master.data.entity.consumer",
    "com.lore.master.data.entity.business"
})
@Import({DataSourceConfig.class, ConsumerRepositoryConfig.class, StorageRepositoryConfig.class, AdminRepositoryConfig.class})
@EnableJpaAuditing
public class WebConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebConsumerApplication.class, args);
    }
}
