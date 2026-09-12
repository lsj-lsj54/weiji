package com.weiji.modules.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("friend_request")
public class FriendRequest extends BaseEntity {

    private Long fromUserId;
    private Long toUserId;
    private String status;
}
