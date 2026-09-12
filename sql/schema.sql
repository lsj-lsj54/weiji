-- 微积 MySQL 8.0 建表脚本
-- Docker 初始化时已选择 MYSQL_DATABASE=weiji，直接建表即可

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 用户模块
-- =========================
CREATE TABLE IF NOT EXISTS `user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `phone`           VARCHAR(20)  NOT NULL,
    `password_hash`   VARCHAR(100) NOT NULL,
    `nickname`        VARCHAR(64)  DEFAULT NULL,
    `avatar`          VARCHAR(512) DEFAULT NULL,
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
    `deleted`         TINYINT      NOT NULL DEFAULT 0,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号';

CREATE TABLE IF NOT EXISTS `user_preference` (
    `id`                     BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`                BIGINT       NOT NULL,
    `energy_status`          VARCHAR(32)  DEFAULT 'ENERGETIC' COMMENT 'ENERGETIC/TIRED/SLACKING',
    `rest_days`              VARCHAR(64)  DEFAULT NULL COMMENT '休息日 JSON，如 [0] 表示周日',
    `dnd_periods`            VARCHAR(512) DEFAULT NULL COMMENT '免打扰时段 JSON',
    `daily_focus_minutes`    INT          DEFAULT NULL,
    `weekly_focus_minutes`   INT          DEFAULT NULL,
    `created_at`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好与专注目标';

CREATE TABLE IF NOT EXISTS `user_goal` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT       NOT NULL,
    `title`              VARCHAR(128) NOT NULL,
    `description`        VARCHAR(512) DEFAULT NULL,
    `target_minutes`     INT          DEFAULT NULL,
    `finished_minutes`   INT          NOT NULL DEFAULT 0,
    `status`             TINYINT      NOT NULL DEFAULT 0 COMMENT '0进行中 1完成 2放弃',
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大目标';

-- =========================
-- 任务模块
-- =========================
CREATE TABLE IF NOT EXISTS `task_template` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT       DEFAULT NULL COMMENT '空为系统模板',
    `name`               VARCHAR(64)  NOT NULL,
    `content`            VARCHAR(512) DEFAULT NULL,
    `category`           VARCHAR(32)  DEFAULT NULL COMMENT '学习/工作/生活/自我提升',
    `duration_minutes`   INT          NOT NULL DEFAULT 15,
    `scene_code`         VARCHAR(32)  DEFAULT NULL COMMENT 'HOME/COMMUTE/COMPANY/LIBRARY',
    `scene_tags`         VARCHAR(128) DEFAULT NULL,
    `timer_template`     TINYINT      NOT NULL DEFAULT 0,
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务模板';

CREATE TABLE IF NOT EXISTS `task` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT       NOT NULL,
    `template_id`        BIGINT       DEFAULT NULL,
    `goal_id`            BIGINT       DEFAULT NULL,
    `title`              VARCHAR(128) NOT NULL,
    `content`            VARCHAR(512) DEFAULT NULL,
    `category`           VARCHAR(32)  DEFAULT NULL,
    `priority`           TINYINT      NOT NULL DEFAULT 1 COMMENT '0紧急 1普通 2随缘',
    `duration_minutes`   INT          NOT NULL,
    `scene_code`         VARCHAR(32)  DEFAULT NULL,
    `scene_tags`         VARCHAR(128) DEFAULT NULL,
    `status`             TINYINT      NOT NULL DEFAULT 0 COMMENT '0待做 1进行中 2完成 3缓冲 4过期',
    `buffered`           TINYINT      NOT NULL DEFAULT 0,
    `deferred_to`        DATE         DEFAULT NULL,
    `sort_order`         INT          NOT NULL DEFAULT 0,
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_match` (`user_id`, `scene_code`, `duration_minutes`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务池';

CREATE TABLE IF NOT EXISTS `focus_session` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`              BIGINT       NOT NULL,
    `task_id`              BIGINT       DEFAULT NULL,
    `goal_id`              BIGINT       DEFAULT NULL,
    `title`                VARCHAR(128) NOT NULL,
    `category`             VARCHAR(32)  DEFAULT NULL,
    `client_start_ts`      BIGINT       NOT NULL,
    `client_end_ts`        BIGINT       DEFAULT NULL,
    `server_start_ts`      BIGINT       DEFAULT NULL,
    `server_end_ts`        BIGINT       DEFAULT NULL,
    `duration_seconds`     INT          NOT NULL DEFAULT 0,
    `status`               TINYINT      NOT NULL DEFAULT 0 COMMENT '0计时中 1已结束 2已放弃',
    `remark`               VARCHAR(512) DEFAULT NULL,
    `point_granted`        INT          NOT NULL DEFAULT 0,
    `created_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专注计时会话';

-- =========================
-- 积分与奖励
-- =========================
CREATE TABLE IF NOT EXISTS `point_account` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT   NOT NULL,
    `balance`      BIGINT   NOT NULL DEFAULT 0,
    `total_earned` BIGINT   NOT NULL DEFAULT 0,
    `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分账户';

CREATE TABLE IF NOT EXISTS `point_ledger` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT       NOT NULL,
    `ledger_no`    VARCHAR(64)  NOT NULL,
    `change_amount` BIGINT      NOT NULL,
    `biz_type`     VARCHAR(32)  NOT NULL COMMENT 'TASK/FOCUS/REDEEM/MILESTONE',
    `biz_id`       VARCHAR(64)  DEFAULT NULL,
    `remark`       VARCHAR(256) DEFAULT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ledger_no` (`ledger_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水';

CREATE TABLE IF NOT EXISTS `reward` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL,
    `name`            VARCHAR(64)  NOT NULL,
    `level`           VARCHAR(16)  NOT NULL COMMENT 'NORMAL/MID/HIGH/MILESTONE',
    `point_cost`      INT          NOT NULL,
    `lock_mode`       TINYINT      NOT NULL DEFAULT 0,
    `cooldown_hours`  INT          DEFAULT NULL,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖励池';

CREATE TABLE IF NOT EXISTS `reward_redemption` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT       NOT NULL,
    `reward_id`     BIGINT       NOT NULL,
    `status`        VARCHAR(16)  NOT NULL COMMENT 'PENDING/LOCKED/REDEEMED/EXPIRED',
    `locked_until`  DATETIME     DEFAULT NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖励兑换';

CREATE TABLE IF NOT EXISTS `badge` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `code`        VARCHAR(64)  NOT NULL,
    `name`        VARCHAR(64)  NOT NULL,
    `description` VARCHAR(256) DEFAULT NULL,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就徽章定义';

CREATE TABLE IF NOT EXISTS `user_badge` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL,
    `badge_id`    BIGINT   NOT NULL,
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_badge` (`user_id`, `badge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户徽章';

-- =========================
-- 知识点
-- =========================
CREATE TABLE IF NOT EXISTS `knowledge_note` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT        NOT NULL,
    `task_id`          BIGINT        DEFAULT NULL,
    `title`            VARCHAR(128)  NOT NULL,
    `content`          TEXT,
    `media_type`       VARCHAR(16)   NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT/AUDIO/IMAGE',
    `media_url`        VARCHAR(512)  DEFAULT NULL,
    `mastery`          VARCHAR(16)   NOT NULL DEFAULT 'UNKNOWN' COMMENT 'MASTERED/FUZZY/UNKNOWN',
    `starred`          TINYINT       NOT NULL DEFAULT 0,
    `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点';

CREATE TABLE IF NOT EXISTS `knowledge_tag` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT      NOT NULL,
    `name`        VARCHAR(32) NOT NULL,
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_name` (`user_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点标签';

CREATE TABLE IF NOT EXISTS `knowledge_note_tag` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `note_id`    BIGINT   NOT NULL,
    `tag_id`     BIGINT   NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_note_tag` (`note_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点标签关联';

CREATE TABLE IF NOT EXISTS `knowledge_review` (
    `id`               BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`          BIGINT      NOT NULL,
    `note_id`          BIGINT      NOT NULL,
    `interval_days`    INT         NOT NULL DEFAULT 1,
    `next_review_at`   DATETIME    NOT NULL,
    `remembered`       TINYINT     DEFAULT NULL,
    `created_at`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_next` (`user_id`, `next_review_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='艾宾浩斯复习计划';

-- =========================
-- 社交
-- =========================
CREATE TABLE IF NOT EXISTS `friend_request` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `from_user_id` BIGINT     NOT NULL,
    `to_user_id`   BIGINT     NOT NULL,
    `status`       VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ACCEPTED/REJECTED',
    `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_to_user` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请';

CREATE TABLE IF NOT EXISTS `friend_relation` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL,
    `friend_id`   BIGINT   NOT NULL,
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系（双向各一行）';

CREATE TABLE IF NOT EXISTS `team` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(64)  NOT NULL,
    `owner_id`    BIGINT       NOT NULL,
    `goal_desc`   VARCHAR(256) DEFAULT NULL,
    `status`      TINYINT      NOT NULL DEFAULT 1,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组队打卡';

CREATE TABLE IF NOT EXISTS `team_member` (
    `id`               BIGINT   NOT NULL AUTO_INCREMENT,
    `team_id`          BIGINT   NOT NULL,
    `user_id`          BIGINT   NOT NULL,
    `finished_count`   INT      NOT NULL DEFAULT 0,
    `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_user` (`team_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组队成员';

CREATE TABLE IF NOT EXISTS `plaza_post` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT        NOT NULL,
    `anonymous`   TINYINT       NOT NULL DEFAULT 1,
    `title`       VARCHAR(128)  NOT NULL,
    `content`     TEXT,
    `template_id` BIGINT        DEFAULT NULL,
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匿名任务广场';

SET FOREIGN_KEY_CHECKS = 1;
