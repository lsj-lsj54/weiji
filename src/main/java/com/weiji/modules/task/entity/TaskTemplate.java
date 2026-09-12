package com.weiji.modules.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_template")
public class TaskTemplate extends BaseEntity {

    private Long userId;
    private String name;
    private String content;
    private String category;
    private Integer durationMinutes;
    private String sceneCode;
    private String sceneTags;
    private Integer timerTemplate;
}
