package com.weiji.config; // Spring 配置包

import com.weiji.framework.security.JwtAuthenticationFilter; // 本仓类 JwtAuthenticationFilter
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.springframework.context.annotation.Bean; // 注册 Bean
import org.springframework.context.annotation.Configuration; // 配置类
import org.springframework.http.HttpMethod; // HTTP 类型
import org.springframework.security.config.Customizer; // Spring Security
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Spring Security
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Spring Security
import org.springframework.security.config.http.SessionCreationPolicy; // Spring Security
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Spring Security
import org.springframework.security.crypto.password.PasswordEncoder; // Spring Security
import org.springframework.http.MediaType; // HTTP 类型
import org.springframework.security.web.SecurityFilterChain; // Spring Security
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Spring Security

import java.nio.charset.StandardCharsets; // NIO/字符集

@Configuration // 配置类，可声明 @Bean
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class SecurityConfig { // 定义类 SecurityConfig

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 构造注入 JWT 过滤器

    @Bean // 把返回值放进 Spring 容器
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { // 方法 securityFilterChain
        http.csrf(AbstractHttpConfigurer::disable) // 关闭 CSRF：前后端分离用 JWT，不走 Cookie
                .cors(Customizer.withDefaults()) // 启用 CORS，规则见 WebConfig
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无 Session，认证只靠 JWT
                .authorizeHttpRequests(auth -> auth // 本行业务语句
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 预检 OPTIONS 一律放行
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll() // 这些路径不登录也可访问
                        .anyRequest().authenticated()) // 其余接口必须认证
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> { // 未登录返回 401 JSON
                    response.setStatus(401); // 返回 HTTP 401
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name()); // 响应用 UTF-8
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE); // 响应体为 JSON
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}"); // 写出 JSON 错误体
                }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // JWT 过滤器插在用户名密码过滤器之前
        return http.build(); // 生成过滤器链
    }

    @Bean // 把返回值放进 Spring 容器
    public PasswordEncoder passwordEncoder() { // 校验或加密密码
        return new BCryptPasswordEncoder(); // BCrypt 哈希密码
    }
}
