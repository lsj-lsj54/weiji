package com.weiji.modules.knowledge.entity; // 数据库实体包

import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus
import com.weiji.common.entity.BaseEntity; // 本仓类 BaseEntity
import lombok.Data; // Lombok 样板代码生成
import lombok.EqualsAndHashCode; // Lombok 样板代码生成

@Data // Lombok：getter/setter/equals/hashCode
@EqualsAndHashCode(callSuper = true) // Lombok 相等性，含父类字段
@TableName("knowledge_note_tag") // 映射数据库表名
public class KnowledgeNoteTag extends BaseEntity { // 定义类 KnowledgeNoteTag，有继承

    private Long noteId; // 字段 noteId
    private Long tagId; // 字段 tagId
}
