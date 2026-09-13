package com.weiji.config; // Spring 配置包

import com.fasterxml.jackson.annotation.JsonAutoDetect; // Jackson JSON
import com.fasterxml.jackson.annotation.PropertyAccessor; // Jackson JSON
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson JSON
import com.fasterxml.jackson.databind.SerializationFeature; // Jackson JSON
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Jackson JSON
import org.springframework.context.annotation.Bean; // 注册 Bean
import org.springframework.context.annotation.Configuration; // 配置类
import org.springframework.data.redis.connection.RedisConnectionFactory; // Spring Data Redis
import org.springframework.data.redis.core.RedisTemplate; // Spring Data Redis
import org.springframework.data.redis.core.StringRedisTemplate; // Spring Data Redis
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer; // Spring Data Redis
import org.springframework.data.redis.serializer.StringRedisSerializer; // Spring Data Redis

@Configuration // 配置类，可声明 @Bean
public class RedisConfig { // 定义类 RedisConfig

    @Bean // 把返回值放进 Spring 容器
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) { // 方法 redisTemplate
        RedisTemplate<String, Object> template = new RedisTemplate<>(); // 赋值或调用
        template.setConnectionFactory(factory); // 本行业务语句
        ObjectMapper mapper = new ObjectMapper(); // 赋值或调用
        mapper.registerModule(new JavaTimeModule()); // Jackson 支持 LocalDateTime
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 日期写成 ISO 字符串而不是时间戳
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY); // 本行业务语句
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper); // Redis 值用 JSON 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer(); // Redis key 用字符串
        template.setKeySerializer(stringSerializer); // 设置 key 序列化
        template.setHashKeySerializer(stringSerializer); // 设置 Hash 字段名序列化
        template.setValueSerializer(jsonSerializer); // 设置 value 序列化
        template.setHashValueSerializer(jsonSerializer); // 设置 Hash 值序列化
        template.afterPropertiesSet(); // 完成 RedisTemplate 初始化
        return template; // 返回结果
    }

    @Bean // 把返回值放进 Spring 容器
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) { // 字符串 Redis 模板
        return new StringRedisTemplate(factory); // 字符串 Redis 模板
    }
}
