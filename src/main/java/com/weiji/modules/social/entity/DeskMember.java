package com.weiji.modules.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("desk_member")
public class DeskMember extends BaseEntity {

    private Long sessionId;
    private Long userId;
    private Integer done;
    private String report;
}
