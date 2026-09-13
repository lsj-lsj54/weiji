package com.weiji.config; // Spring 配置包

import com.weiji.common.constant.RedisKey; // 本仓类 RedisKey
import com.weiji.framework.cache.CacheInvalidationListener; // 本仓类 CacheInvalidationListener
import org.springframework.boot.context.properties.EnableConfigurationProperties; // 配置绑定
import org.springframework.context.annotation.Bean; // 注册 Bean
import org.springframework.context.annotation.Configuration; // 配置类
import org.springframework.data.redis.connection.RedisConnectionFactory; // Spring Data Redis
import org.springframework.data.redis.listener.ChannelTopic; // Spring Data Redis
import org.springframework.data.redis.listener.RedisMessageListenerContainer; // Spring Data Redis

@Configuration // 配置类，可声明 @Bean
@EnableConfigurationProperties(CacheProperties.class) // 启用配置属性类
public class CacheConfig { // 定义类 CacheConfig

    @Bean // 把返回值放进 Spring 容器
    public RedisMessageListenerContainer cacheMessageListenerContainer( // 订阅 Redis 频道的监听容器
            RedisConnectionFactory connectionFactory, // 本行业务语句
            CacheInvalidationListener listener) { // 开始代码块
        RedisMessageListenerContainer container = new RedisMessageListenerContainer(); // 订阅 Redis 频道的监听容器
        container.setConnectionFactory(connectionFactory); // 本行业务语句
        container.addMessageListener(listener, new ChannelTopic(RedisKey.CACHE_INVALIDATE_CHANNEL)); // 缓存失效 Pub/Sub 频道
        return container; // 返回结果
    }
}
