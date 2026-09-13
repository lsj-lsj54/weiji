package com.weiji.modules.knowledge.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

import java.time.LocalDateTime; // 日期时间

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("knowledge_review") // 映射数据库表名
public class KnowledgeReview extends BaseEntity { // 定义类 KnowledgeReview，有继承

    private Long userId; // 字段 用户 ID
    private Long noteId; // 字段 noteId
    private Integer intervalDays; // 字段 intervalDays
    private LocalDateTime nextReviewAt; // 字段 nextReviewAt
    private Integer remembered; // 字段 remembered
}
