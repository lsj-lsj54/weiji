package com.weiji.modules.point.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("reward") // 映射数据库表名
public class Reward extends BaseEntity { // 定义类 Reward，有继承

    private Long userId; // 字段 用户 ID
    private String name; // 字段 name
    private String level; // 字段 level
    private Integer pointCost; // 字段 pointCost
    private Integer lockMode; // 字段 lockMode
    private Integer cooldownHours; // 字段 cooldownHours
    private String itemCode; // 字段 itemCode
}
