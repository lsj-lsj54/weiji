package com.weiji.modules.point.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("point_account") // 映射数据库表名
public class PointAccount extends BaseEntity { // 定义类 PointAccount，有继承

    private Long userId; // 字段 用户 ID
    private Long balance; // 字段 balance
    private Long totalEarned; // 字段 totalEarned
}
