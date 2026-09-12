# 数据库设计

引擎 InnoDB，字符集 utf8mb4。主键 `BIGINT` 自增，统一 `created_at` / `updated_at`。脚本：[`sql/schema.sql`](../sql/schema.sql)。

## 用户

| 表 | 说明 |
|---|---|
| `user` | 手机号唯一，密码 BCrypt，逻辑删除 |
| `user_preference` | 精力状态、休息日、免打扰、日/周专注目标 |
| `user_goal` | 大目标及完成时长 |

## 任务

| 表 | 说明 |
|---|---|
| `task_template` | 系统/自定义模板，`timer_template` 标记计时模板 |
| `task` | 任务池。索引 `idx_match(user_id, scene_code, duration_minutes, priority)` |
| `focus_session` | 客户端/服务端起止时间戳，用于防篡改校准 |

## 积分

| 表 | 说明 |
|---|---|
| `point_account` | 余额、累计获得 |
| `point_ledger` | `ledger_no` 唯一，禁止重复结算 |
| `reward` / `reward_redemption` | 奖励与兑换状态机 PENDING/LOCKED/REDEEMED/EXPIRED |
| `badge` / `user_badge` | 徽章定义与用户解锁 |

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
| `plaza_post` | 匿名任务广场 |

周榜不落 MySQL，后续用 Redis ZSet `weiji:rank:week:`。
