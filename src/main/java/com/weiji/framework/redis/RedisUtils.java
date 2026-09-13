package com.weiji.framework.redis; // 基础设施包（安全/缓存/Redis）

import lombok.RequiredArgsConstructor; // Lombok 样板代码生成
import org.springframework.data.redis.core.StringRedisTemplate; // Spring Data Redis
import org.springframework.stereotype.Component; // 通用组件

import java.time.Duration; // 日期时间
import java.util.Set; // JDK 集合/工具
import java.util.concurrent.TimeUnit; // JDK 集合/工具

@Component // 交给组件扫描注册
@RequiredArgsConstructor // Lombok：为 final 字段生成构造器注入
public class RedisUtils { // 定义类 RedisUtils

    private final StringRedisTemplate stringRedisTemplate; // 字符串 Redis 模板

    public void set(String key, String value, long timeoutSeconds) { // 方法 set
        stringRedisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS); // Redis String 命令
    }

    public String get(String key) { // 方法 get
        return stringRedisTemplate.opsForValue().get(key); // Redis String 命令
    }

    public Boolean delete(String key) { // 方法 delete
        return stringRedisTemplate.delete(key); // 返回结果
    }

    public Boolean hasKey(String key) { // 判断 Redis key 是否存在
        return stringRedisTemplate.hasKey(key); // 判断 Redis key 是否存在
    }

    public void hSet(String key, String field, String value) { // 方法 hSet
        stringRedisTemplate.opsForHash().put(key, field, value); // Redis Hash
    }

    public Object hGet(String key, String field) { // 方法 hGet
        return stringRedisTemplate.opsForHash().get(key, field); // Redis Hash
    }

    public void sAdd(String key, String... values) { // 方法 sAdd
        stringRedisTemplate.opsForSet().add(key, values); // 本行业务语句
    }

    public Set<String> sMembers(String key) { // 方法 sMembers
        return stringRedisTemplate.opsForSet().members(key); // 返回结果
    }

    public void zAdd(String key, String member, double score) { // 方法 zAdd
        stringRedisTemplate.opsForZSet().add(key, member, score); // Redis 有序集合
    }

    public Double zScore(String key, String member) { // 读成员分数
        return stringRedisTemplate.opsForZSet().score(key, member); // Redis 有序集合
    }

    public Double zIncr(String key, String member, double delta) { // 周榜 ZSet 加分
        return stringRedisTemplate.opsForZSet().incrementScore(key, member, delta); // Redis 有序集合
    }

    public Set<String> zRevRange(String key, long start, long end) { // 周榜倒序取名次
        return stringRedisTemplate.opsForZSet().reverseRange(key, start, end); // Redis 有序集合
    }

    public void expire(String key, Duration duration) { // 方法 expire
        stringRedisTemplate.expire(key, duration); // 本行业务语句
    }
}
