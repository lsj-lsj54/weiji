package com.weiji;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.weiji.modules")
public class WeijiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeijiApplication.class, args);
    }
}
