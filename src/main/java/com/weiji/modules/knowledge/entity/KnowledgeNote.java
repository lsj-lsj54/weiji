package com.weiji.modules.knowledge.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("knowledge_note") // 映射数据库表名
public class KnowledgeNote extends BaseEntity { // 定义类 KnowledgeNote，有继承

    private Long userId; // 字段 用户 ID
    private Long taskId; // 字段 taskId
    private String title; // 字段 标题
    private String content; // 字段 内容
    private String mediaType; // 字段 mediaType
    private String mediaUrl; // 字段 mediaUrl
    private String mastery; // 字段 mastery
    private Integer starred; // 字段 starred
}
