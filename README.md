# 微积前端 weiji-frontend

碎片时间管理产品「微积」的 Vue 客户端。对着 GitHub 仓库 [lsj-lsj54/weiji](https://github.com/lsj-lsj54/weiji) 的 `weiji-backend` 分支实现。本目录发布在同仓库的 `weiji-frontend` 分支，不含 Java 后端代码。

## 能做什么

- 手机号注册 / 登录（JWT，过期自动刷新）
- **此刻**：拨空闲分钟、场景、类型、精力，匹配任务并专注计时
- **任务池**：短任务、模板入池、大目标拆解、缓冲 / 完成 / 跳过 / 过期、完成率
- **积点**：账单、目标进度、晚间小结、流水、自建奖励兑换、公益捐赠、道具与徽章
- **知识点**：记下、到期「还记得 / 忘了」、周汇总、导出复制
- **社交**：好友申请、匿名广场收藏模板、组队打卡、同桌、周榜
- **我**：编号（加好友用）、休息日点选、免打扰、日周目标

日历导入、OSS 上传、语音不在本仓。后端没有「待处理申请列表 / 我的队伍列表」接口，所以申请编号和队伍编号需要双方记下或写在本机。

## 本地联调

1. 先按后端 README 启动 `weiji-backend`（默认 `http://127.0.0.1:8080`）。
2. 复制 `.env.example` 为 `.env`。开发时 `VITE_API_BASE` 留空，走 Vite 代理 `/api` → 8080。
3. `npm install` 然后 `npm run dev`，打开 `http://localhost:5173`。

建议路径：注册 → 任务池放一条短待办 → 此刻匹配并入账 → 积点看流水 → 知识点记一张 → 「我」里看用户编号。

## 技术栈

Vue 3 + Vite + TypeScript + Vue Router + Pinia + Axios。样式自写。

页面与接口对照见 [docs/frontend.md](docs/frontend.md)。
