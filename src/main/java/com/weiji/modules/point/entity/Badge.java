package com.weiji.modules.point.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("badge")
public class Badge extends BaseEntity {

    private String code;
    private String name;
    private String description;
}
