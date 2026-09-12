package com.weiji.modules.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task")
public class Task extends BaseEntity {

    private Long userId;
    private Long templateId;
    private Long goalId;
    private String title;
    private String content;
    private String category;
    private Integer priority;
    private Integer durationMinutes;
    private String sceneCode;
    private String sceneTags;
    private Integer status;
    private Integer buffered;
    private LocalDate deferredTo;
    private Integer sortOrder;
    private String sourceType;
    private Long reviewNoteId;
    private Integer startDelaySeconds;
}
