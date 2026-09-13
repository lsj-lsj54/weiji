package com.weiji.framework.cache; // 基础设施包（安全/缓存/Redis）

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Jackson JSON
import lombok.AllArgsConstructor; // Lombok 样板代码生成
import lombok.Data; // Lombok 样板代码生成
import lombok.NoArgsConstructor; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@NoArgsConstructor // 无参构造，Jackson 反序列化需要
@AllArgsConstructor // 全参构造
@JsonIgnoreProperties(ignoreUnknown = true) // Jackson 忽略未知字段
public class CacheMessage { // 定义类 CacheMessage

    private String instanceId; // 字段 本 JVM 标识，用来忽略自己发出的失效消息
    private String cacheName; // 字段 cacheName
    private String key; // 字段 key
    /** When true, {@link #key} is a prefix; other instances evict matching L1 keys. */
    private boolean prefix; // 字段 prefix
}
