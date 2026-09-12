package com.weiji.framework.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.weiji.common.constant.RedisKey;
import com.weiji.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class MultiLevelCache {

    static final Object NULL_VALUE = new Object();

    private static final int LOCK_STRIPES = 64;

    private final CacheProperties properties;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Cache<String, Object>> l1Caches = new ConcurrentHashMap<>();
    private final Object[] locks = new Object[LOCK_STRIPES];

    public MultiLevelCache(CacheProperties properties, StringRedisTemplate stringRedisTemplate) {
        this.properties = properties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = cacheObjectMapper();
        for (int i = 0; i < LOCK_STRIPES; i++) {
            locks[i] = new Object();
        }
    }

    public <T> T get(String name, String key, Class<T> type, Supplier<T> loader) {
        return get(name, key, objectMapper.getTypeFactory().constructType(type), loader);
    }

    public <T> List<T> getList(String name, String key, Class<T> elementType, Supplier<List<T>> loader) {
        JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, elementType);
        List<T> list = get(name, key, javaType, loader);
        return list == null ? List.of() : list;
    }

    public <T> T get(String name, String key, JavaType javaType, Supplier<T> loader) {
        Cache<String, Object> l1 = l1Of(name);
        Object cached = l1.getIfPresent(key);
        if (cached != null) {
            return unwrap(cached, javaType);
        }

        String redisKey = RedisKey.cache(name, key);
        L2Result<T> l2 = readL2(redisKey, javaType);
        if (!l2.miss) {
            putL1(l1, key, l2.value);
            return l2.value;
        }

        synchronized (lockFor(redisKey)) {
            cached = l1.getIfPresent(key);
            if (cached != null) {
                return unwrap(cached, javaType);
            }
            l2 = readL2(redisKey, javaType);
            if (!l2.miss) {
                putL1(l1, key, l2.value);
                return l2.value;
            }
            T loaded = loader.get();
            writeL2(name, redisKey, loaded);
            putL1(l1, key, loaded);
            return loaded;
        }
    }

    public void evict(String name, String key) {
        invalidateL1(name, key);
        try {
            stringRedisTemplate.delete(RedisKey.cache(name, key));
        } catch (Exception ex) {
            log.warn("L2 evict failed, name={}, key={}", name, key, ex);
        }
        publish(new CacheMessage(properties.getInstanceId(), name, key, false));
    }

    public void evictPrefix(String name, String prefix) {
        invalidateL1Prefix(name, prefix);
        deleteL2ByPattern(RedisKey.cache(name, prefix) + "*");
        publish(new CacheMessage(properties.getInstanceId(), name, prefix, true));
    }

    public void invalidateL1(String name) {
        Cache<String, Object> l1 = l1Caches.get(name);
        if (l1 != null) {
            l1.invalidateAll();
        }
    }

    public void invalidateL1(String name, String key) {
        Cache<String, Object> l1 = l1Caches.get(name);
        if (l1 != null) {
            l1.invalidate(key);
        }
    }

    public void invalidateL1Prefix(String name, String prefix) {
        Cache<String, Object> l1 = l1Caches.get(name);
        if (l1 == null) {
            return;
        }
        List<String> keys = new ArrayList<>();
        for (String k : l1.asMap().keySet()) {
            if (k.startsWith(prefix)) {
                keys.add(k);
            }
        }
        keys.forEach(l1::invalidate);
    }

    private Cache<String, Object> l1Of(String name) {
        return l1Caches.computeIfAbsent(name, this::buildL1);
    }

    private Cache<String, Object> buildL1(String name) {
        CacheProperties.Spec spec = properties.spec(name);
        return Caffeine.newBuilder()
                .maximumSize(spec.resolvedL1MaxSize())
                .expireAfter(new Expiry<String, Object>() {
                    @Override
                    public long expireAfterCreate(String key, Object value, long currentTime) {
                        return ttlNanos(name, value);
                    }

                    @Override
                    public long expireAfterUpdate(String key, Object value, long currentTime, long currentDuration) {
                        return ttlNanos(name, value);
                    }

                    @Override
                    public long expireAfterRead(String key, Object value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }

    private long ttlNanos(String name, Object value) {
        CacheProperties.Spec spec = properties.spec(name);
        Duration ttl = value == NULL_VALUE ? spec.resolvedNullTtl() : spec.resolvedL1Ttl();
        return Math.max(ttl.toNanos(), TimeUnit.SECONDS.toNanos(1));
    }

    private <T> L2Result<T> readL2(String redisKey, JavaType javaType) {
        try {
            String json = stringRedisTemplate.opsForValue().get(redisKey);
            if (json == null) {
                return L2Result.miss();
            }
            CachePayload payload = objectMapper.readValue(json, CachePayload.class);
            if (payload == null || payload.isNil()) {
                return L2Result.hit(null);
            }
            return L2Result.hit(objectMapper.convertValue(payload.getValue(), javaType));
        } catch (Exception ex) {
            log.warn("L2 read failed, key={}", redisKey, ex);
            return L2Result.miss();
        }
    }

    private void writeL2(String name, String redisKey, Object value) {
        try {
            CacheProperties.Spec spec = properties.spec(name);
            Duration ttl = value == null ? spec.resolvedNullTtl() : spec.resolvedL2Ttl();
            CachePayload payload = value == null
                    ? new CachePayload(true, null)
                    : new CachePayload(false, objectMapper.valueToTree(value));
            stringRedisTemplate.opsForValue().set(
                    redisKey,
                    objectMapper.writeValueAsString(payload),
                    jitterMillis(ttl),
                    TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            log.warn("L2 write failed, key={}", redisKey, ex);
        }
    }

    private void putL1(Cache<String, Object> l1, String key, Object value) {
        l1.put(key, value == null ? NULL_VALUE : value);
    }

    @SuppressWarnings("unchecked")
    private <T> T unwrap(Object cached, JavaType javaType) {
        if (cached == NULL_VALUE) {
            return null;
        }
        if (javaType.getRawClass().isInstance(cached)) {
            return (T) cached;
        }
        return objectMapper.convertValue(cached, javaType);
    }

    private Object lockFor(String key) {
        int idx = (key.hashCode() & 0x7fffffff) % LOCK_STRIPES;
        return locks[idx];
    }

    private long jitterMillis(Duration ttl) {
        long base = Math.max(ttl.toMillis(), 1000L);
        double factor = 0.8 + ThreadLocalRandom.current().nextDouble(0.4);
        return Math.max(1000L, (long) (base * factor));
    }

    private void publish(CacheMessage message) {
        try {
            stringRedisTemplate.convertAndSend(
                    RedisKey.CACHE_INVALIDATE_CHANNEL,
                    objectMapper.writeValueAsString(message));
        } catch (Exception ex) {
            log.warn("cache invalidate publish failed, name={}, key={}", message.getCacheName(), message.getKey(), ex);
        }
    }

    private void deleteL2ByPattern(String pattern) {
        try {
            stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
                ScanOptions options = ScanOptions.scanOptions().match(pattern).count(256).build();
                try (Cursor<byte[]> cursor = connection.scan(options)) {
                    while (cursor.hasNext()) {
                        connection.keyCommands().del(cursor.next());
                    }
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
                return null;
            });
        } catch (Exception ex) {
            log.warn("L2 prefix delete failed, pattern={}", pattern, ex);
        }
    }

    static ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private static final class L2Result<T> {
        private final boolean miss;
        private final T value;

        private L2Result(boolean miss, T value) {
            this.miss = miss;
            this.value = value;
        }

        static <T> L2Result<T> miss() {
            return new L2Result<>(true, null);
        }

        static <T> L2Result<T> hit(T value) {
            return new L2Result<>(false, value);
        }
    }
}
