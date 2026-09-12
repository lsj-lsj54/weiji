package com.weiji.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@ConfigurationProperties(prefix = "weiji.cache")
public class CacheProperties {

    /**
     * Distinguishes this JVM so Pub/Sub can ignore messages this instance already applied.
     */
    private String instanceId = UUID.randomUUID().toString();

    private Spec defaults = new Spec();

    /**
     * Optional per-cacheName overrides; null fields fall back to {@link #defaults}.
     */
    private Map<String, Spec> caches = new HashMap<>();

    public Spec spec(String cacheName) {
        return merge(defaults, caches.get(cacheName));
    }

    private static Spec merge(Spec base, Spec override) {
        Spec spec = new Spec();
        Spec src = base == null ? new Spec() : base;
        spec.setL1MaxSize(src.getL1MaxSize());
        spec.setL1Ttl(src.getL1Ttl());
        spec.setL2Ttl(src.getL2Ttl());
        spec.setNullTtl(src.getNullTtl());
        if (override == null) {
            return spec;
        }
        if (override.getL1MaxSize() != null) {
            spec.setL1MaxSize(override.getL1MaxSize());
        }
        if (override.getL1Ttl() != null) {
            spec.setL1Ttl(override.getL1Ttl());
        }
        if (override.getL2Ttl() != null) {
            spec.setL2Ttl(override.getL2Ttl());
        }
        if (override.getNullTtl() != null) {
            spec.setNullTtl(override.getNullTtl());
        }
        return spec;
    }

    @Data
    public static class Spec {
        private Integer l1MaxSize = 10_000;
        private Duration l1Ttl = Duration.ofMinutes(2);
        private Duration l2Ttl = Duration.ofMinutes(15);
        private Duration nullTtl = Duration.ofSeconds(30);

        public int resolvedL1MaxSize() {
            return l1MaxSize == null || l1MaxSize <= 0 ? 10_000 : l1MaxSize;
        }

        public Duration resolvedL1Ttl() {
            return l1Ttl == null ? Duration.ofMinutes(2) : l1Ttl;
        }

        public Duration resolvedL2Ttl() {
            return l2Ttl == null ? Duration.ofMinutes(15) : l2Ttl;
        }

        public Duration resolvedNullTtl() {
            return nullTtl == null ? Duration.ofSeconds(30) : nullTtl;
        }
    }
}
