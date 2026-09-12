# 微积后端 weiji-backend

碎片时间管理产品「微积」的 Java 后端骨架。当前版本打通 **注册 / 登录 / JWT / 用户资料**，其余业务模块已建表与空 Service，按 `user → task → point → knowledge → social` 顺序后续迭代。

## 技术栈

- Java 17 + Spring Boot 3.2.5
- Spring Security + JWT + Redis 令牌黑名单
- MyBatis-Plus 3.5.5（`mybatis-plus-spring-boot3-starter`，适配 Spring Boot 3）
- MySQL 8.0、Redis 7.x
- Docker Compose 一键拉起中间件

## 本地准备

1. 安装 JDK 17、Maven 3.9+（或使用仓库自带 `mvnw`）、Docker（可选）。
2. **改连接信息**（必须，仓库里全是占位符 `CHANGE_ME`）：
   - [`src/main/resources/application-dev.yml`](src/main/resources/application-dev.yml)：MySQL / Redis 地址、账号、密码
   - [`src/main/resources/application.yml`](src/main/resources/application.yml)：`weiji.jwt.secret`（至少 32 字符）
   - 可选：复制一份 `application-local.yml` 覆盖本地密码（该文件已加入 `.gitignore`）
3. 中间件二选一：
   - 使用自己已有的 MySQL 8 与 Redis 7，手动建库并执行 [`sql/schema.sql`](sql/schema.sql)
   - 或改好 [`docker-compose.yml`](docker-compose.yml) 中的密码后执行：

```bash
docker compose up -d
```

Compose 首次启动会把 `sql/schema.sql` 导入 `weiji` 库。已有数据卷时不会重复执行初始化脚本。

4. 启动应用：

```bash
./mvnw spring-boot:run
```

Windows：

```bat
mvnw.cmd spring-boot:run
```

默认端口 `8080`。未改密码、未启动中间件时进程会连库失败，这是预期行为。

## 认证示例

注册：

```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"phone\":\"13800000000\",\"password\":\"123456\",\"nickname\":\"测试\"}"
```

登录后把返回的 `accessToken` 放进 Header：`Authorization: Bearer <token>`，再请求：

```bash
curl http://localhost:8080/api/user/me -H "Authorization: Bearer <accessToken>"
```

完整接口见 [docs/api.md](docs/api.md)。分层说明见 [docs/architecture.md](docs/architecture.md)，表结构见 [docs/database.md](docs/database.md)。

## 当前范围

| 模块 | 状态 |
|---|---|
| common / config / framework | 已实现 |
| user 认证与资料 | 已实现 |
| task / point / knowledge / social | 表 + 实体 + 空 Service，无业务 Controller |
| 短信、OSS、推送、WebSocket | 未接入，由你后续手动连接第三方 |

## 文档

- [docs/requirements.md](docs/requirements.md) 需求与后端边界
- [docs/architecture.md](docs/architecture.md) 项目结构
- [docs/database.md](docs/database.md) 表结构
- [docs/api.md](docs/api.md) 已开放接口
