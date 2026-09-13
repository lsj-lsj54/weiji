package com.weiji.modules.point.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

import java.time.LocalDateTime; // 日期时间

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("reward_redemption") // 映射数据库表名
public class RewardRedemption extends BaseEntity { // 定义类 RewardRedemption，有继承

    private Long userId; // 字段 用户 ID
    private Long rewardId; // 字段 rewardId
    private String status; // 字段 状态
    private LocalDateTime lockedUntil; // 字段 lockedUntil
}
