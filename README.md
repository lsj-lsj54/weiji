# 微积后端 weiji-backend

碎片时间管理「微积」的服务端。覆盖用户、任务匹配、专注计时、积分奖励、知识点复习、社交。

## 技术栈

- Java 17 + Spring Boot 3.2.5
- Spring Security + JWT + Redis（令牌黑名单、计时会话、周榜 ZSet）
- MyBatis-Plus 3.5.5
- MySQL 8.0

## 本地准备

1. JDK 17、Maven（可用 `mvnw`）。
2. 复制 [`src/main/resources/application-dev.yml.example`](src/main/resources/application-dev.yml.example) 为 `application-dev.yml`，填写 MySQL / Redis。该文件含密码，不要提交。
3. 复制 [`.env.example`](.env.example) 为 `.env`，填写 Compose 密码。
4. 新库执行 [`sql/schema.sql`](sql/schema.sql)；已有库再执行 [`sql/patch_v2.sql`](sql/patch_v2.sql)。
5. 可选：`docker compose up -d`
6. `mvnw.cmd spring-boot:run`

请求体、错误码等细节见 [docs/api.md](docs/api.md)。

## 接口约定

前缀 `/api`。响应 `{ "code": 0, "message": "ok", "data": {} }`，`code === 0` 为成功。除注册、登录、刷新外均需 `Authorization: Bearer <accessToken>`。

## 接口目录

### 认证

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/register` | 手机号注册 |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/refresh` | 刷新令牌 |
| POST | `/api/auth/logout` | 登出 |

### 用户

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/user/me` | 当前用户 |
| GET | `/api/user/search` | 按手机号精确查找 |
| PUT | `/api/user/profile` | 改昵称、头像 |
| GET | `/api/user/preference` | 读偏好 |
| PUT | `/api/user/preference` | 写精力、休息日、免打扰、日周目标 |
| GET | `/api/user/goals` | 大目标列表 |
| POST | `/api/user/goals` | 新建大目标 |
| POST | `/api/user/goals/{id}/decompose` | 拆成短任务入池 |

### 任务

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/task/templates` | 模板列表 |
| POST | `/api/task/templates` | 新建模板 |
| POST | `/api/task/templates/{id}/apply` | 模板入池 |
| GET | `/api/task` | 任务列表 |
| POST | `/api/task` | 新建任务 |
| PUT | `/api/task` | 更新任务 |
| POST | `/api/task/{id}/buffer` | 当日缓冲，不再匹配 |
| POST | `/api/task/expire` | 批量过期 |
| POST | `/api/task/import` | 导入待办数组 |
| POST | `/api/task/match` | 按空闲时长匹配 |
| POST | `/api/task/preload` | 离线预加载 |
| POST | `/api/task/{id}/complete` | 完成并发积分 |
| POST | `/api/task/{id}/skip` | 消耗跳过卡 |
| GET | `/api/task/delay-curve` | 拖延改善曲线 |

休息日匹配返回 `3004`。免打扰只挡住预加载，主动匹配仍出结果。

### 专注

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/focus/start` | 开始计时 |
| POST | `/api/focus/{id}/pause` | 暂停 |
| POST | `/api/focus/{id}/resume` | 继续 |
| POST | `/api/focus/{id}/finish` | 结束并入账 |
| POST | `/api/focus/{id}/abandon` | 放弃，不计分 |
| GET | `/api/focus/live` | 进行中的会话 |

### 积分

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/point/account` | 余额、累计、连续天数 |
| GET | `/api/point/ledgers` | 流水 |
| GET | `/api/point/rewards` | 奖励列表 |
| POST | `/api/point/rewards` | 自建奖励 |
| POST | `/api/point/rewards/{id}/redeem` | 兑换 |
| POST | `/api/point/donate` | 公益捐赠 |
| GET | `/api/point/items` | 道具 |
| GET | `/api/point/badges` | 徽章 |
| GET | `/api/point/time-bill` | 时间账单 |
| GET | `/api/point/focus-board` | 专注看板 |
| GET | `/api/point/evening-summary` | 当日小结 |

### 知识点

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/knowledge` | 新建笔记 |
| GET | `/api/knowledge` | 笔记列表 |
| GET | `/api/knowledge/{id}` | 笔记详情 |
| PUT | `/api/knowledge/{id}` | 更新笔记 |
| DELETE | `/api/knowledge/{id}` | 删除笔记 |
| POST | `/api/knowledge/merge` | 合并笔记 |
| GET | `/api/knowledge/review/due` | 到期复习 |
| POST | `/api/knowledge/review/{id}/mark` | 记得 / 忘了 |
| GET | `/api/knowledge/weekly-summary` | 本周按标签汇总 |
| GET | `/api/knowledge/export` | 文本导出 |

### 社交

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/social/friends/requests` | 发出好友申请 |
| GET | `/api/social/friends/requests` | 待处理申请 |
| POST | `/api/social/friends/requests/{id}/handle` | 接受或拒绝 |
| GET | `/api/social/friends` | 好友列表（含昵称、手机号） |
| GET | `/api/social/teams` | 我的队伍及队员进度 |
| POST | `/api/social/teams` | 建队 |
| POST | `/api/social/teams/{id}/checkin` | 组队打卡 |
| GET | `/api/social/teams/{id}/progress` | 队员进度 |
| POST | `/api/social/plaza` | 发广场帖 |
| GET | `/api/social/plaza` | 广场列表 |
| POST | `/api/social/plaza/{id}/favorite` | 收藏成模板 |
| POST | `/api/social/desk` | 约同桌 |
| POST | `/api/social/desk/{id}/start` | 开始同桌 |
| POST | `/api/social/desk/{id}/report` | 汇报进度 |
| GET | `/api/social/desk/{id}` | 同桌详情（轮询） |
| GET | `/api/social/rank/week` | 好友周时长榜 |

## 服务端已实现

模板与任务匹配、专注校准、积分流水、艾宾浩斯复习、好友组队广场同桌、周榜 Redis ZSet。

客户端专属（小组件、语音、系统免打扰、定位 SDK、短信/OSS/推送）不在本仓。
