package com.weiji.modules.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("team_member")
public class TeamMember extends BaseEntity {

    private Long teamId;
    private Long userId;
    private Integer finishedCount;
}
