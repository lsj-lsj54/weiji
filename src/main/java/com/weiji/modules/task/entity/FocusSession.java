package com.weiji.modules.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("focus_session")
public class FocusSession extends BaseEntity {

    private Long userId;
    private Long taskId;
    private Long goalId;
    private String title;
    private String category;
    private Long clientStartTs;
    private Long clientEndTs;
    private Long serverStartTs;
    private Long serverEndTs;
    private Integer durationSeconds;
    private Integer pausedSeconds;
    private Long lastPauseTs;
    private String sourceType;
    private Integer status;
    private String remark;
    private Integer pointGranted;
}
