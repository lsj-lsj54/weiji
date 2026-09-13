package com.weiji.config; // Spring 配置包

import lombok.Data; // Lombok 样板代码生成
import org.springframework.boot.context.properties.ConfigurationProperties; // 配置绑定

import java.time.Duration; // 日期时间
import java.util.HashMap; // JDK 集合/工具
import java.util.Map; // JDK 集合/工具
import java.util.UUID; // JDK 集合/工具

@Data // Lombok：getter/setter/equals/hashCode
@ConfigurationProperties(prefix = "weiji.cache") // 配置类，可声明 @Bean
public class CacheProperties { // 定义类 CacheProperties

    /**
     * Distinguishes this JVM so Pub/Sub can ignore messages this instance already applied.
     */
    private String instanceId = UUID.randomUUID().toString(); // 成员字段

    private Spec defaults = new Spec(); // 成员字段

    /**
     * Optional per-cacheName overrides; null fields fall back to {@link #defaults}.
     */
    private Map<String, Spec> caches = new HashMap<>(); // 成员字段

    public Spec spec(String cacheName) { // 方法 spec
        return merge(defaults, caches.get(cacheName)); // 返回结果
    }

    private static Spec merge(Spec base, Spec override) { // 方法 merge
        Spec spec = new Spec(); // 赋值或调用
        Spec src = base == null ? new Spec() : base; // 赋值或调用
        spec.setL1MaxSize(src.getL1MaxSize()); // 本行业务语句
        spec.setL1Ttl(src.getL1Ttl()); // 本行业务语句
        spec.setL2Ttl(src.getL2Ttl()); // 本行业务语句
        spec.setNullTtl(src.getNullTtl()); // 本行业务语句
        if (override == null) { // 条件判断
            return spec; // 返回结果
        }
        if (override.getL1MaxSize() != null) { // 条件判断
            spec.setL1MaxSize(override.getL1MaxSize()); // 本行业务语句
        }
        if (override.getL1Ttl() != null) { // 条件判断
            spec.setL1Ttl(override.getL1Ttl()); // 本行业务语句
        }
        if (override.getL2Ttl() != null) { // 条件判断
            spec.setL2Ttl(override.getL2Ttl()); // 本行业务语句
        }
        if (override.getNullTtl() != null) { // 条件判断
            spec.setNullTtl(override.getNullTtl()); // 本行业务语句
        }
        return spec; // 返回结果
    }

    @Data // Lombok：getter/setter/equals/hashCode
    public static class Spec { // 定义类 Spec
        private Integer l1MaxSize = 10_000; // 字段 10_000
        private Duration l1Ttl = Duration.ofMinutes(2); // 成员字段
        private Duration l2Ttl = Duration.ofMinutes(15); // 成员字段
        private Duration nullTtl = Duration.ofSeconds(30); // 成员字段

        public int resolvedL1MaxSize() { // 方法 resolvedL1MaxSize
            return l1MaxSize == null || l1MaxSize <= 0 ? 10_000 : l1MaxSize; // 返回结果
        }

        public Duration resolvedL1Ttl() { // 方法 resolvedL1Ttl
            return l1Ttl == null ? Duration.ofMinutes(2) : l1Ttl; // 返回结果
        }

        public Duration resolvedL2Ttl() { // 方法 resolvedL2Ttl
            return l2Ttl == null ? Duration.ofMinutes(15) : l2Ttl; // 返回结果
        }

        public Duration resolvedNullTtl() { // 方法 resolvedNullTtl
            return nullTtl == null ? Duration.ofSeconds(30) : nullTtl; // 返回结果
        }
    }
}
