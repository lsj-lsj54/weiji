# 微积移动端

uni-app（Vue 3 + Vite + TypeScript + Pinia）客户端，对接 [weiji-backend](../weiji-backend) 的 JWT REST 接口。功能对齐 Web 端：登录注册、此刻匹配与专注、待办/模板/目标、积点、知识点、社交。

不做桌面小组件、语音、系统免打扰、日历解析、定位、厂商推送、OSS 直传。

## 本地启动

1. 后端在 `127.0.0.1:8080` 已运行（见 weiji-backend README）。
2. 本目录：

```bash
npm install
npm run dev:h5
```

H5 默认 [http://localhost:5174](http://localhost:5174)，`/api` 由 Vite 代理到后端，无需配 `VITE_API_BASE`。

## 微信小程序

```bash
npm run dev:mp-weixin
```

用微信开发者工具打开 `dist/dev/mp-weixin`。真机需在小程序后台配置 request 合法域名，并在项目根创建 `.env`：

```
VITE_API_BASE=https://你的后端域名
```

开发阶段 `manifest.json` 里 `mp-weixin.setting.urlCheck` 已为 `false`。

## 接口约定

- 前缀 `/api`，成功 `{ "code": 0, "data": ... }`
- `Authorization: Bearer <accessToken>`
- Token 存在本地 `weiji.access` / `weiji.refresh`；401 会刷新，失败回登录页
- 知识点更新为 `PUT /api/knowledge`（id 在 body）

## 页面

底栏：此刻 / 待办 / 积点 / 我。知识点与「一块过」从「我」进入。
