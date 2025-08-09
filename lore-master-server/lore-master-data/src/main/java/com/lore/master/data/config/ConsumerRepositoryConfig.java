package com.lore.master.data.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 主数据源Repository配置
 */
@Configuration
@EnableJpaRepositories(
        basePackages = {
            "com.lore.master.data.repository.consumer",
            "com.lore.master.data.repository.business"
        },
        entityManagerFactoryRef = "consumerEntityManagerFactory",
        transactionManagerRef = "consumerTransactionManager"
)
@EntityScan(basePackages = {
    "com.lore.master.data.entity.consumer"
        ,
    "com.lore.master.data.entity.business"
})
public class ConsumerRepositoryConfig {
}
