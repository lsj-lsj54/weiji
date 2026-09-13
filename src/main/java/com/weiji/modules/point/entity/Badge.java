package com.weiji.modules.point.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("badge") // 映射数据库表名
public class Badge extends BaseEntity { // 定义类 Badge，有继承

    private String code; // 字段 编码
    private String name; // 字段 name
    private String description; // 字段 description
}
