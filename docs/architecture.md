# 架构说明

MVP 采用单工程 + 领域分层，包名 `com.weiji`，后续可按模块拆微服务：用户中心、任务中心、积分中心、知识点中心、社交中心。

## 目录

```
src/main/java/com/weiji
├── WeijiApplication.java
├── common          # Result、异常、枚举、工具、常量
├── config          # Security / Redis / MyBatis-Plus / Web
├── framework       # JWT、UserContext、RedisUtils、访问日志拦截器
└── modules
    ├── user
    ├── task        # matcher 规则匹配占位
    ├── point
    ├── knowledge   # review 艾宾浩斯占位
    └── social
```

每个业务模块内部：`controller` → `service` → `mapper` → `entity` / `dto` / `vo`。

当前仅 `user` 提供 Controller。其余模块 Service 方法会抛出 `UnsupportedOperationException`，避免半成品 API。

## 认证流

1. 注册/登录签发 accessToken（2h）与 refreshToken（7d）。
2. refreshToken 的 jti 写入 Redis `weiji:token:refresh:{userId}`。
3. 请求头 `Authorization: Bearer <accessToken>`。
4. 登出将 accessToken 的 jti 写入 Redis 黑名单，并删除 refresh jti。

## 配置

- `application.yml`：公共项与 JWT 占位密钥
- `application-dev.yml` / `application-prod.yml`：数据源与 Redis，密码均为 `CHANGE_ME`
- 本地可用 `application-local.yml` 覆盖（不提交 Git）

## 依赖说明

技术选型文档中的 `mybatis-plus-boot-starter` 在 Spring Boot 3 下使用 `javax` 命名空间。本仓库改为同版本的 `mybatis-plus-spring-boot3-starter`，并增加 `commons-pool2` 以启用 Lettuce 连接池。
