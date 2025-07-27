package com.lore.master.web.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * C端用户Web应用启动类
 */
@SpringBootApplication(scanBasePackages = {
    "com.lore.master.web.consumer",
    "com.lore.master.service.consumer",
    "com.lore.master.service.middleware.storage",
    "com.lore.master.common"
})
@EnableJpaAuditing
public class WebConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebConsumerApplication.class, args);
    }
}
