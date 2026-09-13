package com.weiji.modules.task.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

import java.time.LocalDate; // 日期时间

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("task") // 映射数据库表名
public class Task extends BaseEntity { // 定义类 Task，有继承

    private Long userId; // 字段 用户 ID
    private Long templateId; // 字段 templateId
    private Long goalId; // 字段 goalId
    private String title; // 字段 标题
    private String content; // 字段 内容
    private String category; // 字段 category
    private Integer priority; // 字段 priority
    private Integer durationMinutes; // 字段 durationMinutes
    private String sceneCode; // 字段 sceneCode
    private String sceneTags; // 字段 sceneTags
    private Integer status; // 字段 状态
    private Integer buffered; // 字段 buffered
    private LocalDate deferredTo; // 字段 deferredTo
    private Integer sortOrder; // 字段 sortOrder
    private String sourceType; // 字段 sourceType
    private Long reviewNoteId; // 字段 reviewNoteId
    private Integer startDelaySeconds; // 字段 startDelaySeconds
}
