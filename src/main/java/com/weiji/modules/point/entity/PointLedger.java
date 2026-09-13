package com.weiji.modules.point.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("point_ledger") // 映射数据库表名
public class PointLedger extends BaseEntity { // 定义类 PointLedger，有继承

    private Long userId; // 字段 用户 ID
    private String ledgerNo; // 字段 ledgerNo
    private Long changeAmount; // 字段 changeAmount
    private String bizType; // 字段 bizType
    private String bizId; // 字段 bizId
    private String remark; // 字段 remark
}
