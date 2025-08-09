package com.lore.master.data.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 主数据源Repository配置
 */
@Configuration
@Order(2) // 设置较低的优先级
@EnableJpaRepositories(
        basePackages = "com.lore.master.data.repository.consumer",
        entityManagerFactoryRef = "consumerEntityManagerFactory",
        transactionManagerRef = "consumerTransactionManager"
)
@EntityScan(basePackages = "com.lore.master.data.entity.consumer")
public class ConsumerRepositoryConfig {
}
