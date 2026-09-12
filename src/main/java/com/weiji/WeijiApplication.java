package com.weiji;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(basePackages = "com.weiji.modules", annotationClass = Mapper.class)
@EnableScheduling
public class WeijiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeijiApplication.class, args);
    }
}
