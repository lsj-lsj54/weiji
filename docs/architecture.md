# 架构说明

MVP 采用单工程 + 领域分层，包名 `com.weiji`，后续可按模块拆微服务：用户中心、任务中心、积分中心、知识点中心、社交中心。

## 目录

```
src/main/java/com/weiji
├── WeijiApplication.java
├── common          # Result、异常、枚举、工具、常量
├── config          # Security / Redis / Cache / MyBatis-Plus / Web
├── framework       # JWT、UserContext、RedisUtils、MultiLevelCache、访问日志拦截器
└── modules
    ├── user
    ├── task        # 规则匹配 RuleTaskMatcher
    ├── point
    ├── knowledge   # 艾宾浩斯 EbbinghausReviewScheduler
    └── social
```

每个业务模块内部：`controller` → `service` → `mapper` → `entity` / `dto` / `vo`。

Redis：`weiji:focus:live:{userId}` 当前计时；`weiji:rank:week:{周一日期}` 周榜 ZSet。

实体读缓存（Caffeine L1 + Redis L2）见 [cache.md](cache.md)。key 形如 `weiji:c:{name}:{key}`，失效频道 `weiji:cache:invalidate`。

本地配置：`application-dev.yml` 不要提交；Compose 密码放 `.env`。

## 认证流

1. 注册/登录签发 accessToken（2h）与 refreshToken（7d）。
2. refreshToken 的 jti 写入 Redis `weiji:token:refresh:{userId}`。
3. 请求头 `Authorization: Bearer <accessToken>`。
4. 登出将 accessToken 的 jti 写入 Redis 黑名单，并删除 refresh jti。

## 配置

- `application.yml`：公共项、JWT 占位密钥、`weiji.cache` 多级缓存 TTL
- `application-dev.yml` / `application-prod.yml`：数据源与 Redis，密码均为 `CHANGE_ME`
- 本地可用 `application-local.yml` 覆盖（不提交 Git）

## 依赖说明

技术选型文档中的 `mybatis-plus-boot-starter` 在 Spring Boot 3 下使用 `javax` 命名空间。本仓库改为同版本的 `mybatis-plus-spring-boot3-starter`，并增加 `commons-pool2` 以启用 Lettuce 连接池。
