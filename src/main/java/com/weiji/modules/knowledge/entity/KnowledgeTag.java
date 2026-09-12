package com.weiji.modules.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_tag")
public class KnowledgeTag extends BaseEntity {

    private Long userId;
    private String name;
}
