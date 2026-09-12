# 微积前端说明

与后端 [`docs/api.md`](https://github.com/lsj-lsj54/weiji/blob/weiji-backend/docs/api.md) 对齐。`{ code, message, data }`，`code === 0` 成功。除注册/登录/刷新外带 `Authorization: Bearer`。

## 环境

`VITE_API_BASE` 开发留空，Vite 把 `/api` 代理到 `127.0.0.1:8080`。

## 底栏与分段

| 路径 | 主内容 | 主要接口 |
|---|---|---|
| `/now` | 拨盘、场景、预加载/匹配、计时 | `preload` `match` `/api/focus/*` `POST /api/task` |
| `/tasks` | 待办 / 模板 / 目标 | task CRUD、`import`、`templates`、`goals` `decompose` |
| `/points` | 账单 / 兑换 / 流水 | `account` `time-bill` `focus-board` `rewards` `donate` `items` `badges` `delay-curve` |
| `/knowledge` | 到期、列表、记下 | knowledge CRUD `merge` `review` `export` |
| `/social` | 周榜 / 好友 / 广场 / 组队 | `search?phone=` `friends/requests` `teams` `plaza` `desk` `rank/week` |
| `/me` | 偏好；入口到知识、社交 | `me` `profile` `preference` `logout` |

新后端口：

- `GET /api/user/search?phone=` 精确手机号
- `GET /api/social/friends/requests` 待处理申请
- `GET /api/social/teams` 我的队伍
- `GET /api/point/badges` 含 `name` `code` `description`

休息日 `3004`：此刻页可去「我」改休息日，或空手计时。免打扰默认关闭；主动点「找一条」不受免打扰拦截，预加载在免打扰时段不推送。
