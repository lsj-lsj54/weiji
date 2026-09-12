# 多级缓存

读多写少的实体走 **Caffeine（L1 进程内）+ Redis（L2 共享）**，旁路加载（Cache-Aside）。令牌、专注锁、周榜 ZSet 仍用 [`RedisUtils`](../src/main/java/com/weiji/framework/redis/RedisUtils.java)，不进这层 KV 缓存。

## 读写路径

```
读：L1 → L2 → MySQL（回填 L2、L1）
写：先改 MySQL，再 evict 本机 L1 与 Redis L2，Pub/Sub 通知其他实例只清 L1
```

- L1 未命中再查 Redis；L2 命中回填 L1。
- 库没有数据时写入短 TTL 空值，挡住缓存穿透。
- 同进程同一 key 用分段锁单飞，避免热点同时打库。
- L2 TTL 在配置值的 0.8～1.2 倍之间抖动，降低雪崩。
- Redis 频道 `weiji:cache:invalidate`；消息带 `instanceId`，本机忽略自己发出的失效通知。

入口类：[`MultiLevelCache`](../src/main/java/com/weiji/framework/cache/MultiLevelCache.java)。业务里显式 `get` / `getList` / `evict`，不用 Spring `@Cacheable`。

## 配置

见 `application.yml` 的 `weiji.cache`：

| 项 | 默认 | 含义 |
|---|---|---|
| `instance-id` | `${random.uuid}` | 本 JVM 标识，用于忽略自己的 Pub/Sub |
| `defaults.l1-max-size` | 10000 | Caffeine 最大条目 |
| `defaults.l1-ttl` | 2m | 本地过期（空值用 `null-ttl`） |
| `defaults.l2-ttl` | 15m | Redis 过期 |
| `defaults.null-ttl` | 30s | 空值过期 |

可按 cacheName 覆盖，例如：

```yaml
weiji:
  cache:
    caches:
      user:
        l1-ttl: 1m
        l2-ttl: 10m
```

Redis 实际 key：`weiji:c:{cacheName}:{key}`。

## 第一期覆盖

| 数据 | cacheName | key | 读 | 失效 |
|---|---|---|---|---|
| 用户资料（UserVO，不含密码） | `user` | userId | `UserService.getUser` / `currentUser`；周榜昵称 | `updateProfile` |
| 用户偏好 | `preference` | userId | 用户、任务匹配、积分看板 | `updatePreference` |
| 徽章目录 | `badge` | `all` | 发徽章、我的徽章 | 运营改目录时 `evict`（当前无写接口） |
| 系统任务模板 | `sysTemplate` | `all` | 模板列表中的系统部分 | 管理端改模板时 `evict`（用户自定义模板不缓存） |

周榜分数仍是 Redis ZSet `weiji:rank:week:{周一日期}`，只把 `selectById` 换成用户缓存。

## 不要放进 MultiLevelCache

- `weiji:token:blacklist:` / `weiji:token:refresh:`：登出要立刻全局生效
- `weiji:focus:live:`：互斥锁
- `weiji:rank:week:`：需要 `ZINCRBY` / `ZREVRANGE`

任务池、广场 feed 等组合条件列表第一期也不缓存，失效面太大。

## 业务侧用法

```java
cache.get(CacheNames.USER, String.valueOf(userId), UserVO.class,
        () -> toVo(userMapper.selectById(userId)));

cache.evict(CacheNames.USER, String.valueOf(userId));
```

列表用 `getList`。改库成功后再 `evict`，不要先写缓存再写库。
