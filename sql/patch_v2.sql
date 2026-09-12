-- 已有库升级（只执行一次）。若列已存在会报 Duplicate column，可忽略该条。
ALTER TABLE `user` ADD COLUMN `streak_days` INT NOT NULL DEFAULT 0 AFTER `status`;
ALTER TABLE `user` ADD COLUMN `last_active_date` DATE DEFAULT NULL AFTER `streak_days`;

ALTER TABLE `task` ADD COLUMN `source_type` VARCHAR(16) NOT NULL DEFAULT 'MANUAL' AFTER `sort_order`;
ALTER TABLE `task` ADD COLUMN `review_note_id` BIGINT DEFAULT NULL AFTER `source_type`;
ALTER TABLE `task` ADD COLUMN `start_delay_seconds` INT DEFAULT NULL AFTER `review_note_id`;

ALTER TABLE `focus_session` ADD COLUMN `paused_seconds` INT NOT NULL DEFAULT 0 AFTER `duration_seconds`;
ALTER TABLE `focus_session` ADD COLUMN `last_pause_ts` BIGINT DEFAULT NULL AFTER `paused_seconds`;
ALTER TABLE `focus_session` ADD COLUMN `source_type` VARCHAR(16) NOT NULL DEFAULT 'FOCUS' AFTER `last_pause_ts`;

ALTER TABLE `reward` ADD COLUMN `item_code` VARCHAR(32) DEFAULT NULL AFTER `cooldown_hours`;

ALTER TABLE `point_ledger` ADD UNIQUE KEY `uk_user_biz` (`user_id`, `biz_type`, `biz_id`);

CREATE TABLE IF NOT EXISTS `user_item` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT      NOT NULL,
    `item_code`   VARCHAR(32) NOT NULL,
    `quantity`    INT         NOT NULL DEFAULT 0,
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_item` (`user_id`, `item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `desk_session` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `owner_id`    BIGINT      NOT NULL,
    `peer_id`     BIGINT      NOT NULL,
    `status`      VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    `started_at`  DATETIME    DEFAULT NULL,
    `ended_at`    DATETIME    DEFAULT NULL,
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `desk_member` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT,
    `session_id`   BIGINT      NOT NULL,
    `user_id`      BIGINT      NOT NULL,
    `done`         TINYINT     NOT NULL DEFAULT 0,
    `report`       VARCHAR(256) DEFAULT NULL,
    `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_user` (`session_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `plaza_favorite` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL,
    `post_id`     BIGINT   NOT NULL,
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
