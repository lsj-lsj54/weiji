package com.weiji.modules.point.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reward")
public class Reward extends BaseEntity {

    private Long userId;
    private String name;
    private String level;
    private Integer pointCost;
    private Integer lockMode;
    private Integer cooldownHours;
}
