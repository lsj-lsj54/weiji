package com.weiji.modules.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_review")
public class KnowledgeReview extends BaseEntity {

    private Long userId;
    private Long noteId;
    private Integer intervalDays;
    private LocalDateTime nextReviewAt;
    private Integer remembered;
}
