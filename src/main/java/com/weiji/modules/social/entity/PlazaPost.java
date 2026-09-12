package com.weiji.modules.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("plaza_post")
public class PlazaPost extends BaseEntity {

    private Long userId;
    private Integer anonymous;
    private String title;
    private String content;
    private Long templateId;
}
