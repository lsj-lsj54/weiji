# 微积前端 weiji-frontend

碎片时间管理产品「微积」的 Vue 客户端。对着 GitHub 仓库 [lsj-lsj54/weiji](https://github.com/lsj-lsj54/weiji) 的 `weiji-backend` 分支实现。本目录发布在同仓库的 `weiji-frontend` 分支，不含 Java 后端代码。

## 能做什么

- 手机号注册 / 登录（JWT，过期自动刷新）
- **此刻**：拨空闲分钟、选场景和精力，匹配一条能塞进去的任务，专注计时（暂停 / 继续 / 入账 / 这段不算）
- **任务池**：加短任务、缓冲、记完成、跳过
- **积点**：余额、连续天数、日/周账单、目标进度、晚间小结、流水
- **我**：昵称、精力、休息日、免打扰、日周目标
- 知识点到期复习、好友周榜：只读轻量页

日历导入、OSS 上传、语音、系统免打扰不在本迭代。

## 本地联调

1. 先按后端 README 启动 `weiji-backend`（默认 `http://127.0.0.1:8080`）。
2. 复制 `.env.example` 为 `.env`。开发时 `VITE_API_BASE` 留空，走 Vite 代理 `/api` → 8080。
3. 安装并启动：

```bash
npm install
npm run dev
```

浏览器打开提示的地址（默认 `http://localhost:5173`）。注册一个手机号，去任务池放一条 15 分钟以内的待办，再回此刻匹配并开始计时。

生产或直连后端时设置：

```
VITE_API_BASE=http://127.0.0.1:8080
```

## 技术栈

Vue 3 + Vite + TypeScript + Vue Router + Pinia + Axios。样式自写，不用 Element 一类默认皮肤。

页面与接口对照见 [docs/frontend.md](docs/frontend.md)。
