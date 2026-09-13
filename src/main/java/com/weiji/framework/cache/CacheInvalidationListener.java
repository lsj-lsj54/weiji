package com.weiji.framework.cache; // 基础设施包（安全/缓存/Redis）

import com.fasterxml.jackson.databind.ObjectMapper; // Jackson JSON
import com.weiji.config.CacheProperties; // 本仓类 CacheProperties
import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import lombok.extern.slf4j.Slf4j; // Lombok 样板代码生成
import org.apache.commons.lang3.StringUtils; // 字符串工具
import org.springframework.data.redis.connection.Message; // Spring Data Redis
import org.springframework.data.redis.connection.MessageListener; // Spring Data Redis
import org.springframework.stereotype.Component; // 通用组件

@Slf4j // Lombok 生成 log，走 Logback
@Component // 交给组件扫描注册
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class CacheInvalidationListener implements MessageListener { // 定义类 CacheInvalidationListener，实现接口

    private final CacheProperties properties; // 构造注入 properties
    private final MultiLevelCache multiLevelCache; // 构造注入 multiLevelCache
    private final ObjectMapper objectMapper = MultiLevelCache.cacheObjectMapper(); // 成员字段

    @Override // 覆盖父类/接口方法
    public void onMessage(Message message, byte[] pattern) { // 方法 onMessage
        try { // 捕获异常
            CacheMessage msg = objectMapper.readValue(message.getBody(), CacheMessage.class); // 赋值或调用
            if (msg == null || StringUtils.isBlank(msg.getCacheName())) { // 条件判断
                return; // 本行业务语句
            }
            if (StringUtils.equals(properties.getInstanceId(), msg.getInstanceId())) { // 条件判断
                return; // 本行业务语句
            }
            if (msg.isPrefix()) { // 条件判断
                multiLevelCache.invalidateL1Prefix(msg.getCacheName(), StringUtils.defaultString(msg.getKey())); // 本行业务语句
                return; // 本行业务语句
            }
            if (StringUtils.isBlank(msg.getKey())) { // 条件判断
                multiLevelCache.invalidateL1(msg.getCacheName()); // 本行业务语句
                return; // 本行业务语句
            }
            multiLevelCache.invalidateL1(msg.getCacheName(), msg.getKey()); // 本行业务语句
        } catch (Exception ex) { // 开始代码块
            log.warn("ignore cache invalidate message", ex); // 本行业务语句
        }
    }
}
