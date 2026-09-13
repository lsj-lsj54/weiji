package com.weiji.config; // Spring 配置包

import com.weiji.framework.interceptor.AccessLogInterceptor; // 本仓类 AccessLogInterceptor
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.springframework.context.annotation.Bean; // 注册 Bean
import org.springframework.context.annotation.Configuration; // 配置类
import org.springframework.web.cors.CorsConfiguration; // 跨域配置
import org.springframework.web.cors.CorsConfigurationSource; // 跨域配置
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // 跨域配置
import org.springframework.web.servlet.config.annotation.InterceptorRegistry; // Spring MVC
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; // Spring MVC

import java.util.List; // JDK 集合/工具

@Configuration // 配置类，可声明 @Bean
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class WebConfig implements WebMvcConfigurer { // 定义类 WebConfig，实现接口

    private final AccessLogInterceptor accessLogInterceptor; // 构造注入 访问日志拦截器

    @Override // 覆盖父类/接口方法
    public void addInterceptors(InterceptorRegistry registry) { // 注册访问日志拦截器
        registry.addInterceptor(accessLogInterceptor).addPathPatterns("/api/**"); // 注册访问日志拦截器
    }

    @Bean // 把返回值放进 Spring 容器
    public CorsConfigurationSource corsConfigurationSource() { // 方法 corsConfigurationSource
        CorsConfiguration config = new CorsConfiguration(); // 赋值或调用
        config.setAllowedOriginPatterns(List.of("*")); // 允许的前端来源
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // 允许的 HTTP 方法
        config.setAllowedHeaders(List.of("*")); // 允许的请求头
        config.setAllowCredentials(true); // 允许带 Cookie/凭证
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); // 赋值或调用
        source.registerCorsConfiguration("/**", config); // 把 CORS 应用到全部路径
        return source; // 返回结果
    }
}
