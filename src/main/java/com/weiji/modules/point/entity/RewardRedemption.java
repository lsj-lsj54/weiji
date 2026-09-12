package com.weiji.modules.point.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reward_redemption")
public class RewardRedemption extends BaseEntity {

    private Long userId;
    private Long rewardId;
    private String status;
    private LocalDateTime lockedUntil;
}
