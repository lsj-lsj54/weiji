package com.weiji.framework.cache; // 基础设施包（安全/缓存/Redis）

import com.fasterxml.jackson.databind.JsonNode; // Jackson JSON
import lombok.AllArgsConstructor; // Lombok 样板代码生成
import lombok.Data; // Lombok 样板代码生成
import lombok.NoArgsConstructor; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@NoArgsConstructor // 无参构造，Jackson 反序列化需要
@AllArgsConstructor // 全参构造
public class CachePayload { // 定义类 CachePayload

    private boolean nil; // 字段 nil
    private JsonNode value; // 字段 value
}
