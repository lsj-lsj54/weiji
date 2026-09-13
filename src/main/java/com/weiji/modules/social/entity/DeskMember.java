package com.weiji.modules.social.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("desk_member") // 映射数据库表名
public class DeskMember extends BaseEntity { // 定义类 DeskMember，有继承

    private Long sessionId; // 字段 sessionId
    private Long userId; // 字段 用户 ID
    private Integer done; // 字段 done
    private String report; // 字段 report
}
