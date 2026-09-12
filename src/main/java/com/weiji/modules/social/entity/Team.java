package com.weiji.modules.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("team")
public class Team extends BaseEntity {

    private String name;
    private Long ownerId;
    private String goalDesc;
    private Integer status;
}
