# 微积前端说明

与后端 [`docs/api.md`](https://github.com/lsj-lsj54/weiji/blob/weiji-backend/docs/api.md) 对齐。统一响应 `{ code, message, data }`，`code === 0` 为成功。除注册、登录、刷新外，请求头带 `Authorization: Bearer <accessToken>`。

## 环境

| 变量 | 含义 |
|---|---|
| `VITE_API_BASE` | Axios `baseURL`。开发留空，由 Vite 把 `/api` 代理到 `http://127.0.0.1:8080`。 |

Access 约 2 小时、Refresh 约 7 天，存在 `localStorage`（`weiji.access` / `weiji.refresh`）。HTTP 401 时用 refresh 换票，失败则回登录页。

休息日、免打扰匹配失败时后端业务码 `3004` / `3005`，界面直接展示 `message`。

## 路由

| 路径 | 页面 | 接口 |
|---|---|---|
| `/login` `/register` | 登录、注册 | `POST /api/auth/login` `register` |
| `/now` | 空闲拨盘、场景/类型/精力、匹配、直播专注 | `POST /api/task/match`，`/api/focus/*` |
| `/tasks` | 任务、模板入池、大目标拆解 | `GET/POST /api/task`，`templates`，`expire`，`delay-curve`，`/api/user/goals` `decompose` |
| `/points` | 积点、奖励、捐赠、道具徽章 | `account` `time-bill` `focus-board` `evening-summary` `ledgers` `rewards` `donate` `items` `badges` |
| `/knowledge` | 笔记、到期复习、导出 | `POST/GET/DELETE /api/knowledge`，`review/due` `review/{id}/mark`，`weekly-summary` `export` |
| `/social` | 周榜、好友、广场、组队、同桌 | `rank/week` `friends` `plaza` `teams` `desk` |
| `/me` | 资料、休息日、免打扰、退出 | `user/me` `profile` `preference` `logout` |

底栏：此刻 / 积点 / 我。任务、知识、社交从「我」或此刻的任务池入口进入。

组队编号存在 `localStorage weiji.teamId`，同桌编号 `weiji.deskId`。后端暂无「我的队伍列表 / 待处理好友申请列表」。

## 场景与精力

场景码：`HOME` `COMMUTE` `COMPANY` `LIBRARY`。  
精力：`ENERGETIC` `TIRED` `SLACKING`。  
休息日：Java `DayOfWeek % 7`，周日为 `0`。  
任务状态：0 待做 / 1 进行中 / 2 已完成 / 3 今日缓冲 / 4 已过期。  
专注状态：0 进行 / 1 暂停 / 2 结束 / 3 放弃。满 30 秒才按分钟入账。

## 和后端分支的关系

- 后端：`weiji-backend`
- 本前端：`weiji-frontend`（orphan，无 Java 文件）
