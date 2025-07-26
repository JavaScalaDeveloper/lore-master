package com.lore.master.web.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.lore.master.web.business",
    "com.lore.master.service.business",
    "com.lore.master.common"
})
@EntityScan(basePackages = {
    "com.lore.master.data.entity.business"
})
@EnableJpaRepositories(basePackages = {
    "com.lore.master.data.repository.business"
})
public class WebBusinessApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebBusinessApplication.class, args);
    }
}