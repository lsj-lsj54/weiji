package com.weiji.framework.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value, long timeoutSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    public void hSet(String key, String field, String value) {
        stringRedisTemplate.opsForHash().put(key, field, value);
    }

    public Object hGet(String key, String field) {
        return stringRedisTemplate.opsForHash().get(key, field);
    }

    public void sAdd(String key, String... values) {
        stringRedisTemplate.opsForSet().add(key, values);
    }

    public Set<String> sMembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    public void zAdd(String key, String member, double score) {
        stringRedisTemplate.opsForZSet().add(key, member, score);
    }

    public Set<String> zRevRange(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    public void expire(String key, Duration duration) {
        stringRedisTemplate.expire(key, duration);
    }
}
