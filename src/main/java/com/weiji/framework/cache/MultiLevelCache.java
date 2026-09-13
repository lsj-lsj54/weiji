package com.weiji.framework.cache; // 基础设施包（安全/缓存/Redis）

import com.fasterxml.jackson.annotation.JsonAutoDetect; // Jackson JSON
import com.fasterxml.jackson.annotation.PropertyAccessor; // Jackson JSON
import com.fasterxml.jackson.databind.DeserializationFeature; // Jackson JSON
import com.fasterxml.jackson.databind.JavaType; // Jackson JSON
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson JSON
import com.fasterxml.jackson.databind.SerializationFeature; // Jackson JSON
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Jackson JSON
import com.github.benmanes.caffeine.cache.Cache; // Caffeine 本地缓存
import com.github.benmanes.caffeine.cache.Caffeine; // Caffeine 本地缓存
import com.github.benmanes.caffeine.cache.Expiry; // Caffeine 本地缓存
import com.weiji.common.constant.RedisKey; // 本仓类 RedisKey
import com.weiji.config.CacheProperties; // 本仓类 CacheProperties
import lombok.extern.slf4j.Slf4j; // Lombok 样板代码生成
import org.springframework.data.redis.core.Cursor; // Spring Data Redis
import org.springframework.data.redis.core.RedisCallback; // Spring Data Redis
import org.springframework.data.redis.core.ScanOptions; // Spring Data Redis
import org.springframework.data.redis.core.StringRedisTemplate; // Spring Data Redis
import org.springframework.stereotype.Component; // 通用组件

import java.time.Duration; // 日期时间
import java.util.ArrayList; // JDK 集合/工具
import java.util.List; // JDK 集合/工具
import java.util.concurrent.ConcurrentHashMap; // JDK 集合/工具
import java.util.concurrent.ThreadLocalRandom; // JDK 集合/工具
import java.util.concurrent.TimeUnit; // JDK 集合/工具
import java.util.function.Supplier; // JDK 集合/工具

@Slf4j // Lombok 生成 log，走 Logback
@Component // 交给组件扫描注册
public class MultiLevelCache { // 定义类 MultiLevelCache

    static final Object NULL_VALUE = new Object(); // 赋值或调用

    private static final int LOCK_STRIPES = 64; // 字段 64

    private final CacheProperties properties; // 构造注入 properties
    private final StringRedisTemplate stringRedisTemplate; // 字符串 Redis 模板
    private final ObjectMapper objectMapper; // 构造注入 objectMapper
    private final ConcurrentHashMap<String, Cache<String, Object>> l1Caches = new ConcurrentHashMap<>(); // 成员字段
    private final Object[] locks = new Object[LOCK_STRIPES]; // 成员字段

    public MultiLevelCache(CacheProperties properties, StringRedisTemplate stringRedisTemplate) { // 字符串 Redis 模板
        this.properties = properties; // 给当前对象赋值
        this.stringRedisTemplate = stringRedisTemplate; // 给当前对象赋值
        this.objectMapper = cacheObjectMapper(); // 给当前对象赋值
        for (int i = 0; i < LOCK_STRIPES; i++) { // 循环
            locks[i] = new Object(); // 赋值或调用
        }
    }

    public <T> T get(String name, String key, Class<T> type, Supplier<T> loader) { // 方法 get
        return get(name, key, objectMapper.getTypeFactory().constructType(type), loader); // 返回结果
    }

    public <T> List<T> getList(String name, String key, Class<T> elementType, Supplier<List<T>> loader) { // 方法 getList
        JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, elementType); // 赋值或调用
        List<T> list = get(name, key, javaType, loader); // 赋值或调用
        return list == null ? List.of() : list; // 返回结果
    }

    public <T> T get(String name, String key, JavaType javaType, Supplier<T> loader) { // 方法 get
        Cache<String, Object> l1 = l1Of(name); // 赋值或调用
        Object cached = l1.getIfPresent(key); // 赋值或调用
        if (cached != null) { // 条件判断
            return unwrap(cached, javaType); // 返回结果
        }

        String redisKey = RedisKey.cache(name, key); // 赋值或调用
        L2Result<T> l2 = readL2(redisKey, javaType); // 赋值或调用
        if (!l2.miss) { // 条件判断
            putL1(l1, key, l2.value); // 本行业务语句
            return l2.value; // 返回结果
        }

        synchronized (lockFor(redisKey)) { // 开始代码块
            cached = l1.getIfPresent(key); // 赋值或调用
            if (cached != null) { // 条件判断
                return unwrap(cached, javaType); // 返回结果
            }
            l2 = readL2(redisKey, javaType); // 赋值或调用
            if (!l2.miss) { // 条件判断
                putL1(l1, key, l2.value); // 本行业务语句
                return l2.value; // 返回结果
            }
            T loaded = loader.get(); // 赋值或调用
            writeL2(name, redisKey, loaded); // 本行业务语句
            putL1(l1, key, loaded); // 本行业务语句
            return loaded; // 返回结果
        }
    }

    public void evict(String name, String key) { // 方法 evict
        invalidateL1(name, key); // 本行业务语句
        try { // 捕获异常
            stringRedisTemplate.delete(RedisKey.cache(name, key)); // 本行业务语句
        } catch (Exception ex) { // 开始代码块
            log.warn("L2 evict failed, name={}, key={}", name, key, ex); // 赋值或调用
        }
        publish(new CacheMessage(properties.getInstanceId(), name, key, false)); // 本行业务语句
    }

    public void evictPrefix(String name, String prefix) { // 方法 evictPrefix
        invalidateL1Prefix(name, prefix); // 本行业务语句
        deleteL2ByPattern(RedisKey.cache(name, prefix) + "*"); // 本行业务语句
        publish(new CacheMessage(properties.getInstanceId(), name, prefix, true)); // 本行业务语句
    }

    public void invalidateL1(String name) { // 方法 invalidateL1
        Cache<String, Object> l1 = l1Caches.get(name); // 赋值或调用
        if (l1 != null) { // 条件判断
            l1.invalidateAll(); // 本行业务语句
        }
    }

    public void invalidateL1(String name, String key) { // 方法 invalidateL1
        Cache<String, Object> l1 = l1Caches.get(name); // 赋值或调用
        if (l1 != null) { // 条件判断
            l1.invalidate(key); // 本行业务语句
        }
    }

    public void invalidateL1Prefix(String name, String prefix) { // 方法 invalidateL1Prefix
        Cache<String, Object> l1 = l1Caches.get(name); // 赋值或调用
        if (l1 == null) { // 条件判断
            return; // 本行业务语句
        }
        List<String> keys = new ArrayList<>(); // 赋值或调用
        for (String k : l1.asMap().keySet()) { // 循环
            if (k.startsWith(prefix)) { // 条件判断
                keys.add(k); // 本行业务语句
            }
        }
        keys.forEach(l1::invalidate); // 本行业务语句
    }

    private Cache<String, Object> l1Of(String name) { // 方法 l1Of
        return l1Caches.computeIfAbsent(name, this::buildL1); // 返回结果
    }

    private Cache<String, Object> buildL1(String name) { // 方法 buildL1
        CacheProperties.Spec spec = properties.spec(name); // 赋值或调用
        return Caffeine.newBuilder() // 构建本地缓存
                .maximumSize(spec.resolvedL1MaxSize()) // 本行业务语句
                .expireAfter(new Expiry<String, Object>() { // Caffeine 按条目过期
                    @Override // 覆盖父类/接口方法
                    public long expireAfterCreate(String key, Object value, long currentTime) { // Caffeine 按条目过期
                        return ttlNanos(name, value); // 返回结果
                    }

                    @Override // 覆盖父类/接口方法
                    public long expireAfterUpdate(String key, Object value, long currentTime, long currentDuration) { // Caffeine 按条目过期
                        return ttlNanos(name, value); // 返回结果
                    }

                    @Override // 覆盖父类/接口方法
                    public long expireAfterRead(String key, Object value, long currentTime, long currentDuration) { // Caffeine 按条目过期
                        return currentDuration; // 返回结果
                    }
                })
                .build(); // 本行业务语句
    }

    private long ttlNanos(String name, Object value) { // 方法 ttlNanos
        CacheProperties.Spec spec = properties.spec(name); // 赋值或调用
        Duration ttl = value == NULL_VALUE ? spec.resolvedNullTtl() : spec.resolvedL1Ttl(); // 赋值或调用
        return Math.max(ttl.toNanos(), TimeUnit.SECONDS.toNanos(1)); // 返回结果
    }

    private <T> L2Result<T> readL2(String redisKey, JavaType javaType) { // 方法 readL2
        try { // 捕获异常
            String json = stringRedisTemplate.opsForValue().get(redisKey); // Redis String 命令
            if (json == null) { // 条件判断
                return L2Result.miss(); // 返回结果
            }
            CachePayload payload = objectMapper.readValue(json, CachePayload.class); // 赋值或调用
            if (payload == null || payload.isNil()) { // 条件判断
                return L2Result.hit(null); // 返回结果
            }
            return L2Result.hit(objectMapper.convertValue(payload.getValue(), javaType)); // 返回结果
        } catch (Exception ex) { // 开始代码块
            log.warn("L2 read failed, key={}", redisKey, ex); // 赋值或调用
            return L2Result.miss(); // 返回结果
        }
    }

    private void writeL2(String name, String redisKey, Object value) { // 方法 writeL2
        try { // 捕获异常
            CacheProperties.Spec spec = properties.spec(name); // 赋值或调用
            Duration ttl = value == null ? spec.resolvedNullTtl() : spec.resolvedL2Ttl(); // 赋值或调用
            CachePayload payload = value == null // 赋值或调用
                    ? new CachePayload(true, null) // 本行业务语句
                    : new CachePayload(false, objectMapper.valueToTree(value)); // 本行业务语句
            stringRedisTemplate.opsForValue().set( // Redis String 命令
                    redisKey, // 本行业务语句
                    objectMapper.writeValueAsString(payload), // 本行业务语句
                    jitterMillis(ttl), // 本行业务语句
                    TimeUnit.MILLISECONDS); // 本行业务语句
        } catch (Exception ex) { // 开始代码块
            log.warn("L2 write failed, key={}", redisKey, ex); // 赋值或调用
        }
    }

    private void putL1(Cache<String, Object> l1, String key, Object value) { // 方法 putL1
        l1.put(key, value == null ? NULL_VALUE : value); // 赋值或调用
    }

    @SuppressWarnings("unchecked") // 本行业务语句
    private <T> T unwrap(Object cached, JavaType javaType) { // 方法 unwrap
        if (cached == NULL_VALUE) { // 条件判断
            return null; // 返回空
        }
        if (javaType.getRawClass().isInstance(cached)) { // 条件判断
            return (T) cached; // 返回结果
        }
        return objectMapper.convertValue(cached, javaType); // 返回结果
    }

    private Object lockFor(String key) { // 方法 lockFor
        int idx = (key.hashCode() & 0x7fffffff) % LOCK_STRIPES; // 赋值或调用
        return locks[idx]; // 返回结果
    }

    private long jitterMillis(Duration ttl) { // 方法 jitterMillis
        long base = Math.max(ttl.toMillis(), 1000L); // 赋值或调用
        double factor = 0.8 + ThreadLocalRandom.current().nextDouble(0.4); // 赋值或调用
        return Math.max(1000L, (long) (base * factor)); // 返回结果
    }

    private void publish(CacheMessage message) { // 方法 publish
        try { // 捕获异常
            stringRedisTemplate.convertAndSend( // Redis Pub/Sub 发失效消息
                    RedisKey.CACHE_INVALIDATE_CHANNEL, // weiji:cache:invalidate
                    objectMapper.writeValueAsString(message)); // 本行业务语句
        } catch (Exception ex) { // 开始代码块
            log.warn("cache invalidate publish failed, name={}, key={}", message.getCacheName(), message.getKey(), ex); // 赋值或调用
        }
    }

    private void deleteL2ByPattern(String pattern) { // 方法 deleteL2ByPattern
        try { // 捕获异常
            stringRedisTemplate.execute((RedisCallback<Void>) connection -> { // 开始代码块
                ScanOptions options = ScanOptions.scanOptions().match(pattern).count(256).build(); // 赋值或调用
                try (Cursor<byte[]> cursor = connection.scan(options)) { // 捕获异常
                    while (cursor.hasNext()) { // 循环
                        connection.keyCommands().del(cursor.next()); // 本行业务语句
                    }
                } catch (Exception ex) { // 开始代码块
                    throw new IllegalStateException(ex); // 抛出业务或运行时异常
                }
                return null; // 返回空
            });
        } catch (Exception ex) { // 开始代码块
            log.warn("L2 prefix delete failed, pattern={}", pattern, ex); // 赋值或调用
        }
    }

    static ObjectMapper cacheObjectMapper() { // 方法 cacheObjectMapper
        ObjectMapper mapper = new ObjectMapper(); // 赋值或调用
        mapper.registerModule(new JavaTimeModule()); // Jackson 支持 LocalDateTime
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 日期写成 ISO 字符串而不是时间戳
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY); // 本行业务语句
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 本行业务语句
        return mapper; // 返回结果
    }

    private static final class L2Result<T> { // 定义类 L2Result
        private final boolean miss; // 构造注入 miss
        private final T value; // 构造注入 value

        private L2Result(boolean miss, T value) { // 方法 L2Result
            this.miss = miss; // 给当前对象赋值
            this.value = value; // 给当前对象赋值
        }

        static <T> L2Result<T> miss() { // 方法 miss
            return new L2Result<>(true, null); // 返回结果
        }

        static <T> L2Result<T> hit(T value) { // 方法 hit
            return new L2Result<>(false, value); // 返回结果
        }
    }
}
