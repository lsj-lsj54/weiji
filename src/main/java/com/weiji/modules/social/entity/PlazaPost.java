package com.weiji.modules.social.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("plaza_post") // 映射数据库表名
public class PlazaPost extends BaseEntity { // 定义类 PlazaPost，有继承

    private Long userId; // 字段 用户 ID
    private Integer anonymous; // 字段 anonymous
    private String title; // 字段 标题
    private String content; // 字段 内容
    private Long templateId; // 字段 templateId
}
