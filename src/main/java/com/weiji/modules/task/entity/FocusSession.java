package com.weiji.modules.task.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("focus_session") // 映射数据库表名
public class FocusSession extends BaseEntity { // 定义类 FocusSession，有继承

    private Long userId; // 字段 用户 ID
    private Long taskId; // 字段 taskId
    private Long goalId; // 字段 goalId
    private String title; // 字段 标题
    private String category; // 字段 category
    private Long clientStartTs; // 字段 clientStartTs
    private Long clientEndTs; // 字段 clientEndTs
    private Long serverStartTs; // 字段 serverStartTs
    private Long serverEndTs; // 字段 serverEndTs
    private Integer durationSeconds; // 字段 durationSeconds
    private Integer pausedSeconds; // 字段 pausedSeconds
    private Long lastPauseTs; // 字段 lastPauseTs
    private String sourceType; // 字段 sourceType
    private Integer status; // 字段 状态
    private String remark; // 字段 remark
    private Integer pointGranted; // 字段 pointGranted
}
