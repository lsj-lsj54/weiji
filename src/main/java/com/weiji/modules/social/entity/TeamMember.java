package com.weiji.modules.social.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("team_member") // 映射数据库表名
public class TeamMember extends BaseEntity { // 定义类 TeamMember，有继承

    private Long teamId; // 字段 teamId
    private Long userId; // 字段 用户 ID
    private Integer finishedCount; // 字段 finishedCount
}
