package com.weiji.modules.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_note")
public class KnowledgeNote extends BaseEntity {

    private Long userId;
    private Long taskId;
    private String title;
    private String content;
    private String mediaType;
    private String mediaUrl;
    private String mastery;
    private Integer starred;
}
