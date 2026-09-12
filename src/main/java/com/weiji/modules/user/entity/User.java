package com.weiji.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`user`")
public class User extends BaseEntity {

    private String phone;
    private String passwordHash;
    private String nickname;
    private String avatar;
    private Integer status;

    @TableLogic
    private Integer deleted;
}
