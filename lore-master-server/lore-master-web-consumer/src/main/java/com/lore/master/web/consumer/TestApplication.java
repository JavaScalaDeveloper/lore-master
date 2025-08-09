//package com.lore.master.web.consumer;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
//
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Import;
//import com.lore.master.data.config.DataSourceConfig;
//import com.lore.master.data.config.StorageRepositoryConfig;
//
///**
// * 测试启动类 - 测试 FileStorageRepository 是否能正常加载
// */
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
//@ComponentScan(basePackages = {
//    "com.lore.master.service.middleware.storage"
//})
//@Import({DataSourceConfig.class, StorageRepositoryConfig.class})
//public class TestApplication {
//
//    public static void main(String[] args) {
//        System.out.println("Starting TestApplication...");
//        SpringApplication.run(TestApplication.class, args);
//        System.out.println("TestApplication started successfully!");
//    }
//}
