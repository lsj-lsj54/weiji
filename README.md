# 微积前端 weiji-frontend

碎片时间管理「微积」的 Vue 客户端。对应仓库 [lsj-lsj54/weiji](https://github.com/lsj-lsj54/weiji) 的 `weiji-backend` 分支。本目录在 `weiji-frontend` 分支。

按钮为圆角。此刻页空闲分钟自己填写，没有预设档位。

## 页面

底栏四项：**此刻 / 待办 / 积点 / 我**。知识、社交从「我」进入。

- **此刻**：手写空闲分钟、场景、匹配、专注。空池可当场写一条。休息日/免打扰可空手计时。
- **待办**：待办 | 模板 | 目标。导入文本、改名、存模板、拆解。
- **积点**：账单（日/周/月）| 兑换 | 流水。拖延刻度、徽章、道具。
- **知识**：到期复习、记下、星标、合并、导出。
- **社交**：周榜、搜好友、好友列表约同桌、待处理申请、广场、队伍进度、同桌轮询。

## 本地

1. 启动后端 `http://127.0.0.1:8080`。
2. `.env` 里 `VITE_API_BASE` 留空。
3. `npm install && npm run dev`

对照见 [docs/frontend.md](docs/frontend.md)。后端接口目录在仓库 `weiji-backend` 分支的 [README](https://github.com/lsj-lsj54/weiji/blob/weiji-backend/README.md)。
