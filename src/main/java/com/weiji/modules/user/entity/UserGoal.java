package com.weiji.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_goal")
public class UserGoal extends BaseEntity {

    private Long userId;
    private String title;
    private String description;
    private Integer targetMinutes;
    private Integer finishedMinutes;
    private Integer status;
}
