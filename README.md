# 微积后端 weiji-backend

碎片时间管理产品「微积」后端。已实现用户、任务匹配、专注计时、积分奖励、知识点复习、社交（好友/组队/广场/周榜/同桌轮询）。

## 技术栈

- Java 17 + Spring Boot 3.2.5
- Spring Security + JWT + Redis（令牌黑名单、计时会话、周榜 ZSet）
- MyBatis-Plus 3.5.5（`mybatis-plus-spring-boot3-starter`）
- MySQL 8.0

## 本地准备

1. JDK 17、Maven（可用 `mvnw`）。
2. 复制 [`src/main/resources/application-dev.yml.example`](src/main/resources/application-dev.yml.example) 为 `application-dev.yml`，填写你的 MySQL / Redis。该文件含密码，不要提交。
3. 复制 [`.env.example`](.env.example) 为 `.env`，填写 Compose 密码。
4. 新库执行 [`sql/schema.sql`](sql/schema.sql)；已有库再执行 [`sql/patch_v2.sql`](sql/patch_v2.sql)。
5. 可选：`docker compose up -d`
6. `mvnw.cmd spring-boot:run`

接口文档：[docs/api.md](docs/api.md)

## 已实现（服务端）

- 模板库、自定义任务、大目标拆解、分类优先级、缓冲/过期/顺延、客户端待办导入
- 五维规则匹配（时长/场景/优先级/时段/精力）+ 离线预加载队列 + 休息日/免打扰
- 专注计时：开始/暂停/继续/结束/放弃，服务端校准防刷分，写入时间账单与积分
- 积分流水（唯一流水号+业务幂等）、奖励锁定/冷却、公益捐赠、跳过卡/豁免权、徽章、晚间小结
- 知识点 CRUD/标签/合并、艾宾浩斯复习并生成复习任务、周汇总、文本导出
- 好友申请、组队打卡、匿名广场收藏模板、同桌轮询、好友周榜 Redis ZSet

客户端专属（小组件、语音、系统免打扰、定位 SDK、短信/OSS/推送）不在本仓。
