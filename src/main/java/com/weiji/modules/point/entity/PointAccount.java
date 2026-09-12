package com.weiji.modules.point.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("point_account")
public class PointAccount extends BaseEntity {

    private Long userId;
    private Long balance;
    private Long totalEarned;
}
