package com.weiji.modules.user.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("user_goal") // 映射数据库表名
public class UserGoal extends BaseEntity { // 定义类 UserGoal，有继承

    private Long userId; // 字段 用户 ID
    private String title; // 字段 标题
    private String description; // 字段 description
    private Integer targetMinutes; // 字段 targetMinutes
    private Integer finishedMinutes; // 字段 finishedMinutes
    private Integer status; // 字段 状态
}
