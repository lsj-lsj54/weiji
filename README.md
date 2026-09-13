# 微积移动端

uni-app（Vue 3 + Vite + TypeScript + Pinia）客户端，对接 weiji-backend 的 JWT REST 接口。GitHub 分支：[weiji-pmd](https://github.com/lsj-lsj54/weiji/tree/weiji-pmd)。

功能对齐 Web：登录注册、此刻匹配与专注、待办/模板/目标、积点、知识点、社交。

不做桌面小组件、语音、系统免打扰、日历解析、定位、厂商推送、OSS 直传。

## 本地 H5

1. 后端在本机 `8080` 已运行。
2. 本目录：

```bash
npm install
npm run dev:h5
```

浏览器打开终端里的 Local / Network 地址（默认端口 5174）。`/api` 由 Vite 代理到 `127.0.0.1:8080`。

手机浏览器测 H5：电脑手机同一 Wi‑Fi，打开 Network 那个 `http://192.168.x.x:5174`。已开启 `host: true`。

## 微信小程序

```bash
npm run dev:mp-weixin
```

用微信开发者工具打开 `dist/dev/mp-weixin`。真机需 `.env` 里配置：

```
VITE_API_BASE=https://你的后端域名
```

开发阶段 `mp-weixin.setting.urlCheck` 为 `false`。

## Android 原生 App

见 [README-android.md](README-android.md)。摘要：

```bash
npm run build:app-android
npm run sync:android
```

再把 DCloud 离线 SDK 放进 `android/app/libs`，用 Android Studio 打开 `android/` 出 APK。

App 没有 Vite 代理。登录页 /「我」填写服务器，例如 `http://192.168.x.x:8080`。

## 接口约定

- 前缀 `/api`，成功 `{ "code": 0, "data": ... }`
- `Authorization: Bearer <accessToken>`
- Token 存在 `weiji.access` / `weiji.refresh`；401 会刷新
- 知识点更新为 `PUT /api/knowledge`（id 在 body）

## 页面

底栏：此刻 / 待办 / 积点 / 我。知识点与「一块过」从「我」进入。
