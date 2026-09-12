package com.weiji.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_preference")
public class UserPreference extends BaseEntity {

    private Long userId;
    private String energyStatus;
    private String restDays;
    private String dndPeriods;
    private Integer dailyFocusMinutes;
    private Integer weeklyFocusMinutes;
}
