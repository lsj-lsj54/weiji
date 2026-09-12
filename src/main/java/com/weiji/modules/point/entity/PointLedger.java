package com.weiji.modules.point.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("point_ledger")
public class PointLedger extends BaseEntity {

    private Long userId;
    private String ledgerNo;
    private Long changeAmount;
    private String bizType;
    private String bizId;
    private String remark;
}
