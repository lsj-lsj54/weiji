package com.weiji; // 根包，组件扫描从这里开始

import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标记
import org.mybatis.spring.annotation.MapperScan; // 扫描 Mapper 接口
import org.springframework.boot.SpringApplication; // Spring Boot 启动入口
import org.springframework.boot.autoconfigure.SpringBootApplication; // 组合启动注解
import org.springframework.scheduling.annotation.EnableScheduling; // 开启定时任务

@SpringBootApplication // 配置+自动配置+扫描 com.weiji
@MapperScan(basePackages = "com.weiji.modules", annotationClass = Mapper.class) // 只扫描带 @Mapper 的接口，避免把 Service 当 Mapper
@EnableScheduling // 允许 @Scheduled 定时任务
public class WeijiApplication { // 定义类 WeijiApplication

    public static void main(String[] args) { // JVM 入口
        SpringApplication.run(WeijiApplication.class, args); // 启动 Spring 容器和内嵌 Tomcat
    }
}
