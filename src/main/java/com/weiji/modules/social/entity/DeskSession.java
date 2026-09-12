package com.weiji.modules.social.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiji.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("desk_session")
public class DeskSession extends BaseEntity {

    private Long ownerId;
    private Long peerId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
