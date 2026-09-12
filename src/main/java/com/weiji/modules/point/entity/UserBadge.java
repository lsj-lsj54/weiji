package com.weiji.modules.point.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_badge")
public class UserBadge extends BaseEntity {

    private Long userId;
    private Long badgeId;
}
