package com.weiji.modules.social.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

import java.time.LocalDateTime; // 日期时间

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("desk_session") // 映射数据库表名
public class DeskSession extends BaseEntity { // 定义类 DeskSession，有继承

    private Long ownerId; // 字段 ownerId
    private Long peerId; // 字段 peerId
    private String status; // 字段 状态
    private LocalDateTime startedAt; // 字段 startedAt
    private LocalDateTime endedAt; // 字段 endedAt
}
