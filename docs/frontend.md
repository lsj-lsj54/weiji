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
| `/login` `/register` | 登录、注册 | `POST /api/auth/login` `POST /api/auth/register` |
| `/now` | 空闲拨盘、场景、匹配、直播专注 | `POST /api/task/match`，`POST /api/focus/start`，`pause` / `resume` / `finish` / `abandon`，`GET /api/focus/live` |
| `/tasks` | 任务池 | `GET/POST /api/task`，`POST /api/task/{id}/buffer\|complete\|skip` |
| `/points` | 积点看板 | `GET /api/point/account` `time-bill` `focus-board` `evening-summary` `ledgers` |
| `/knowledge` | 知识点轻量 | `GET /api/knowledge` `GET /api/knowledge/review/due` |
| `/social` | 周榜轻量 | `GET /api/social/rank/week` |
| `/me` | 资料与偏好、退出 | `GET /api/user/me`，`PUT /api/user/profile`，`GET/PUT /api/user/preference`，`POST /api/auth/logout` |

底栏只放：此刻 / 积点 / 我。任务池从「我」或匹配空结果进入。

## 场景与精力

场景码：`HOME` `COMMUTE` `COMPANY` `LIBRARY`。  
精力：`ENERGETIC` `TIRED` `SLACKING`。  
任务状态：0 待做 / 1 进行中 / 2 已完成 / 3 今日缓冲 / 4 已过期。  
专注状态：0 进行 / 1 暂停 / 2 结束 / 3 放弃。结束时提交 `clientEndTs`，服务端校准后发积分（满 30 秒才按分钟入账）。

## 目录

```
src/api/         HTTP 与类型
src/stores/      auth、focus
src/views/       页面
src/components/  底栏、空闲拨盘、专注环
src/styles/      色板与基础样式
```

## 和后端分支的关系

- 后端：`weiji-backend`
- 本前端：`weiji-frontend`（orphan，无 Java 文件）
- 不改后端 CORS（已允许任意 Origin）
