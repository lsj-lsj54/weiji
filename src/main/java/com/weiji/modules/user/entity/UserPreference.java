package com.weiji.modules.user.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("user_preference") // 映射数据库表名
public class UserPreference extends BaseEntity { // 定义类 UserPreference，有继承

    private Long userId; // 字段 用户 ID
    private String energyStatus; // 字段 energyStatus
    private String restDays; // 字段 restDays
    private String dndPeriods; // 字段 dndPeriods
    private Integer dailyFocusMinutes; // 字段 dailyFocusMinutes
    private Integer weeklyFocusMinutes; // 字段 weeklyFocusMinutes
}
