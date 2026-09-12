# HTTP 接口

统一前缀 `/api`。响应 `{ "code": 0, "message": "ok", "data": {} }`。除注册/登录/刷新外均需 `Authorization: Bearer <accessToken>`。

## 认证与用户

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/register` | 手机号+密码 |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/refresh` | 刷新令牌 |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/user/me` | 当前用户 |
| GET | `/api/user/search?phone=` | 按手机号精确查找，返回 id/昵称/手机号 |
| PUT | `/api/user/profile` | 昵称/头像 |
| GET/PUT | `/api/user/preference` | 精力状态、休息日 JSON 如 `[0]`、免打扰 `"22:00-07:00"`、日/周专注目标 |
| POST/GET | `/api/user/goals` | 大目标 |
| POST | `/api/user/goals/{id}/decompose` | `{ "chunkMinutes": 15 }` 拆成 5–30 分钟任务入池 |

## 任务

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST | `/api/task/templates` | 系统+自定义模板 |
| POST | `/api/task/templates/{id}/apply` | 一键入池 |
| POST/GET/PUT | `/api/task` | 任务 CRUD；GET 可 `status` `category` |
| POST | `/api/task/{id}/buffer` | 当日缓冲不再匹配 |
| POST | `/api/task/expire` | `{ "ids": [] }` 批量过期 |
| POST | `/api/task/import` | 客户端解析后的待办数组 |
| POST | `/api/task/match` | `{ idleMinutes, sceneCode, category, energyStatus }` 场景 HOME/COMMUTE/COMPANY/LIBRARY |
| POST | `/api/task/preload` | 离线预加载 |
| POST | `/api/task/{id}/complete` | `{ startDelaySeconds }` 完成并发积分 |
| POST | `/api/task/{id}/skip` | 消耗 SKIP_CARD |
| GET | `/api/task/delay-curve` | 拖延改善曲线 |

每日 00:10 未完成待办自动 `deferred_to=今天`。休息日匹配返回 `3004`。免打扰只作用于 `preload`（空列表），用户主动 `match` 仍会出结果。

## 专注计时

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/focus/start` | 标题、分类、goalId、taskId、clientStartTs、sourceType=FOCUS\|MATCH |
| POST | `/api/focus/{id}/pause` `/resume` | 暂停/继续 |
| POST | `/api/focus/{id}/finish` | `{ clientEndTs, remark }` 服务端校准后发积分 |
| POST | `/api/focus/{id}/abandon` | 不计时长不计分 |
| GET | `/api/focus/live` | 当前进行中会话（Redis） |

放弃不计分。客户端时长超过服务端+60s 则以服务端为准。

## 积分与成长

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/point/account` | 余额、累计、连续天数 |
| GET | `/api/point/ledgers` | 流水 |
| POST/GET | `/api/point/rewards` | 自建奖励，`level` NORMAL/MID/HIGH/MILESTONE，`lockMode` `cooldownHours` `itemCode` |
| POST | `/api/point/rewards/{id}/redeem` | 锁定/冷却状态机 |
| POST | `/api/point/donate` | `{ amount }` 公益 |
| GET | `/api/point/items` | 道具 |
| GET | `/api/point/badges` | 我的徽章（`id, badgeId, code, name, description`） |
| GET | `/api/point/time-bill` | `range=day\|week\|month` |
| GET | `/api/point/focus-board` | 专注看板+目标进度 |
| GET | `/api/point/evening-summary` | 当日文字小结 |

`itemCode`：`SKIP_CARD`、`DAY_EXEMPT`、`CHARITY`、`CUSTOM`。

## 知识点

创建体：`{ "note": { "title","content","taskId","mediaType","mediaUrl","mastery","starred" }, "tags": ["英语"] }`。`mediaType` TEXT/AUDIO/IMAGE，URL 由客户端传 OSS 地址。

| 方法 | 路径 | 说明 |
|---|---|---|
| POST/PUT/GET/DELETE | `/api/knowledge` `/api/knowledge/{id}` | CRUD，GET 列表支持 tag/mastery/taskId/starred |
| POST | `/api/knowledge/merge` | `{ fromId, toId }` |
| GET | `/api/knowledge/review/due` | 到期卡片 |
| POST | `/api/knowledge/review/{id}/mark` | `{ remembered: true/false }` 间隔 1/2/4/7/15 天 |
| GET | `/api/knowledge/weekly-summary` | 本周按标签汇总 |
| GET | `/api/knowledge/export` | 文本导出 |

新建知识点会插入复习计划，并在任务池生成「复习：xxx」优先匹配。

## 社交

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/social/friends/requests` | `{ userId }` |
| GET | `/api/social/friends/requests` | 待处理申请（含对方昵称、手机号） |
| POST | `/api/social/friends/requests/{id}/handle` | `{ accept }` |
| GET | `/api/social/friends` | 好友 |
| GET | `/api/social/teams` | 我加入的队伍及成员进度 |
| POST | `/api/social/teams` | `{ name, goalDesc, memberId }` |
| POST/GET | `/api/social/teams/{id}/checkin` `/progress` | 组队打卡，全员完成后额外积分 |
| POST/GET | `/api/social/plaza` | 匿名广场 |
| POST | `/api/social/plaza/{id}/favorite` | 收藏并复制模板 |
| POST | `/api/social/desk` | `{ userId }` 同桌 |
| POST | `/api/social/desk/{id}/start` `/report` | 轮询进度，`{ done, report }` |
| GET | `/api/social/desk/{id}` | 同桌详情 |
| GET | `/api/social/rank/week` | 与好友的周时长榜 |
