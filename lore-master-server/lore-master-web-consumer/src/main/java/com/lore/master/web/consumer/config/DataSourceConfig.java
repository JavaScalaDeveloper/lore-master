package com.lore.master.web.consumer.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * 多数据源配置
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    /**
     * 主数据源（C端用户数据库 - lore_consumer）
     */
    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * 存储数据源（文件存储数据库 - lore_middleware）
     */
    @Bean(name = "storageDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.storage")
    public DataSource storageDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * 主数据源的EntityManagerFactory
     */
    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("primaryDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.lore.master.data.entity.consumer")
                .persistenceUnit("primary")
                .build();
    }

    /**
     * 存储数据源的EntityManagerFactory
     */
    @Bean(name = "storageEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean storageEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("storageDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.lore.master.data.entity.storage")
                .persistenceUnit("storage")
                .build();
    }

    /**
     * 主数据源的事务管理器
     */
    @Primary
    @Bean(name = "primaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * 主数据源的EntityManager
     */
    @Bean(name = "primaryEntityManager")
    public EntityManager primaryEntityManager(@Qualifier("primaryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    /**
     * 存储数据源的EntityManager
     */
    @Bean(name = "storageEntityManager")
    public EntityManager storageEntityManager(@Qualifier("storageEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    /**
     * 存储数据源的事务管理器
     */
    @Bean(name = "storageTransactionManager")
    public PlatformTransactionManager storageTransactionManager(
            @Qualifier("storageEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

/**
 * 主数据源Repository配置
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.lore.master.data.repository.consumer",
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
class PrimaryRepositoryConfig {
}

/**
 * 存储数据源Repository配置
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.lore.master.data.repository.storage",
    entityManagerFactoryRef = "storageEntityManagerFactory",
    transactionManagerRef = "storageTransactionManager"
)
class StorageRepositoryConfig {
}
