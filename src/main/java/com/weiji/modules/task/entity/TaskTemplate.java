package com.weiji.modules.task.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("task_template") // 映射数据库表名
public class TaskTemplate extends BaseEntity { // 定义类 TaskTemplate，有继承

    private Long userId; // 字段 用户 ID
    private String name; // 字段 name
    private String content; // 字段 内容
    private String category; // 字段 category
    private Integer durationMinutes; // 字段 durationMinutes
    private String sceneCode; // 字段 sceneCode
    private String sceneTags; // 字段 sceneTags
    private Integer timerTemplate; // 字段 timerTemplate
}
