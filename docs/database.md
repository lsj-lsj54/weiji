# 数据库设计

引擎 InnoDB，字符集 utf8mb4。主键 `BIGINT` 自增，统一 `created_at` / `updated_at`。脚本：[`sql/schema.sql`](../sql/schema.sql)。

## 用户

| 表 | 说明 |
|---|---|
| `user` | 手机号唯一，密码 BCrypt，连续打卡 streak_days |
| `user_preference` | 精力状态、休息日、免打扰、日/周专注目标 |
| `user_goal` | 大目标及完成时长 |

## 任务

| 表 | 说明 |
|---|---|
| `task_template` | 系统/自定义模板，`timer_template` 标记计时模板 |
| `task` | 任务池。`source_type` MANUAL/TEMPLATE/GOAL/REVIEW/IMPORT；索引 `idx_match` |
| `focus_session` | 客户端/服务端时间戳、暂停秒数、source_type FOCUS/MATCH |

## 积分

| 表 | 说明 |
|---|---|
| `point_account` | 余额、累计获得 |
| `point_ledger` | `ledger_no` 唯一，`(user_id,biz_type,biz_id)` 幂等 |
| `reward` / `reward_redemption` | 奖励与兑换状态机；`item_code` 道具 |
| `badge` / `user_badge` | 徽章 |
| `user_item` | SKIP_CARD / DAY_EXEMPT 库存 |

## 知识点

| 表 | 说明 |
|---|---|
| `knowledge_note` | 文本/语音/图片 URL |
| `knowledge_tag` / `knowledge_note_tag` | 标签 |
| `knowledge_review` | 下次复习时间、间隔天数（1/2/4/7/15） |

## 社交

| 表 | 说明 |
|---|---|
| `friend_request` / `friend_relation` | 申请流；关系双向各存一行 |
| `team` / `team_member` | 组队与完成数 |
| `plaza_post` / `plaza_favorite` | 匿名广场与收藏 |
| `desk_session` / `desk_member` | 同桌同步（短轮询） |

周榜 Redis ZSet `weiji:rank:week:{周一}`。已有库升级执行 `sql/patch_v2.sql`。
